from fastapi import FastAPI, APIRouter, HTTPException, Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from dotenv import load_dotenv
from starlette.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
import os
import logging
from pathlib import Path
from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any
import uuid
from datetime import datetime, timezone, timedelta
import jwt
import bcrypt

from models import (
    Employee, FullTimeEmployee, PartTimeEmployee, ContractEmployee,
    PayrollProcessor, Payslip,
    DeductionCalculator,
    AttendanceTracker, AttendanceRecord
)
from models.employee import create_employee, EmployeeBase
from models.attendance import AttendanceRecordBase
from models.payroll import PayslipBase

ROOT_DIR = Path(__file__).parent
load_dotenv(ROOT_DIR / '.env', override=True)

# MongoDB connection
mongo_url = os.environ['MONGO_URL']
client = AsyncIOMotorClient(mongo_url)
db = client[os.environ['DB_NAME']]

# JWT Config
JWT_SECRET = os.environ.get('JWT_SECRET', 'motorph-secret-key-2024')
JWT_ALGORITHM = 'HS256'
JWT_EXPIRATION_HOURS = 24

# Create the main app
app = FastAPI(title="MotorPH OOP Payroll System", version="2.0.0")

# Create a router with the /api prefix
api_router = APIRouter(prefix="/api")

# Security
security = HTTPBearer()

# Initialize services (OOP - Composition)
payroll_processor = PayrollProcessor()
deduction_calculator = DeductionCalculator()
attendance_tracker = AttendanceTracker()

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)


# ==================== PYDANTIC MODELS ====================

class UserCreate(BaseModel):
    email: str
    password: str
    first_name: str
    last_name: str
    role: str = "user"  # user, admin, hr


class UserLogin(BaseModel):
    email: str
    password: str


class UserResponse(BaseModel):
    id: str
    email: str
    first_name: str
    last_name: str
    role: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse


class EmployeeCreate(BaseModel):
    employee_id: str
    first_name: str
    last_name: str
    email: str
    department: str
    position: str
    employee_type: str  # full_time, part_time, contract
    date_hired: str
    basic_salary: float = 0.0
    hourly_rate: float = 0.0
    daily_rate: float = 0.0
    hours_per_week: Optional[float] = None
    contract_end_date: Optional[str] = None


class EmployeeUpdate(BaseModel):
    first_name: Optional[str] = None
    last_name: Optional[str] = None
    email: Optional[str] = None
    department: Optional[str] = None
    position: Optional[str] = None
    status: Optional[str] = None
    basic_salary: Optional[float] = None
    hourly_rate: Optional[float] = None
    daily_rate: Optional[float] = None
    hours_per_week: Optional[float] = None
    contract_end_date: Optional[str] = None


class AttendanceClockIn(BaseModel):
    employee_id: str
    timestamp: Optional[str] = None


class AttendanceClockOut(BaseModel):
    record_id: str
    timestamp: Optional[str] = None


class PayrollProcessRequest(BaseModel):
    employee_ids: Optional[List[str]] = None  # None means all employees
    pay_period_start: str
    pay_period_end: str


class DeductionCalculateRequest(BaseModel):
    gross_salary: float


# ==================== AUTHENTICATION ====================

def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')


def verify_password(password: str, hashed: str) -> bool:
    return bcrypt.checkpw(password.encode('utf-8'), hashed.encode('utf-8'))


def create_token(user_id: str, email: str, role: str) -> str:
    payload = {
        'user_id': user_id,
        'email': email,
        'role': role,
        'exp': datetime.now(timezone.utc) + timedelta(hours=JWT_EXPIRATION_HOURS)
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


async def get_current_user(credentials: HTTPAuthorizationCredentials = Depends(security)):
    try:
        token = credentials.credentials
        payload = jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])
        user = await db.users.find_one({'id': payload['user_id']}, {'_id': 0})
        if not user:
            raise HTTPException(status_code=401, detail="User not found")
        return user
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="Token expired")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="Invalid token")


# ==================== AUTH ROUTES ====================

@api_router.post("/auth/register", response_model=TokenResponse)
async def register(user_data: UserCreate):
    # Check if user exists
    existing = await db.users.find_one({'email': user_data.email})
    if existing:
        raise HTTPException(status_code=400, detail="Email already registered")
    
    user_id = str(uuid.uuid4())
    user = {
        'id': user_id,
        'email': user_data.email,
        'password_hash': hash_password(user_data.password),
        'first_name': user_data.first_name,
        'last_name': user_data.last_name,
        'role': user_data.role,
        'created_at': datetime.now(timezone.utc).isoformat()
    }
    
    await db.users.insert_one(user)
    
    token = create_token(user_id, user_data.email, user_data.role)
    
    return TokenResponse(
        access_token=token,
        user=UserResponse(
            id=user_id,
            email=user_data.email,
            first_name=user_data.first_name,
            last_name=user_data.last_name,
            role=user_data.role
        )
    )


@api_router.post("/auth/login", response_model=TokenResponse)
async def login(credentials: UserLogin):
    user = await db.users.find_one({'email': credentials.email}, {'_id': 0})
    if not user:
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    if not verify_password(credentials.password, user['password_hash']):
        raise HTTPException(status_code=401, detail="Invalid credentials")
    
    token = create_token(user['id'], user['email'], user['role'])
    
    return TokenResponse(
        access_token=token,
        user=UserResponse(
            id=user['id'],
            email=user['email'],
            first_name=user['first_name'],
            last_name=user['last_name'],
            role=user['role']
        )
    )


@api_router.get("/auth/me", response_model=UserResponse)
async def get_me(current_user: dict = Depends(get_current_user)):
    return UserResponse(
        id=current_user['id'],
        email=current_user['email'],
        first_name=current_user['first_name'],
        last_name=current_user['last_name'],
        role=current_user['role']
    )


# ==================== EMPLOYEE ROUTES ====================

@api_router.post("/employees", response_model=Dict[str, Any])
async def create_employee_api(employee_data: EmployeeCreate, current_user: dict = Depends(get_current_user)):
    # Check for duplicate employee_id
    existing = await db.employees.find_one({'employee_id': employee_data.employee_id})
    if existing:
        raise HTTPException(status_code=400, detail="Employee ID already exists")
    
    emp_dict = employee_data.model_dump()
    emp_dict['id'] = str(uuid.uuid4())
    emp_dict['status'] = 'active'
    emp_dict['created_at'] = datetime.now(timezone.utc).isoformat()
    emp_dict['updated_at'] = datetime.now(timezone.utc).isoformat()
    
    # Create employee object to validate and get computed properties
    employee = create_employee(emp_dict)
    final_data = employee.to_dict()
    
    await db.employees.insert_one(final_data)
    
    # Return the inserted document without MongoDB's _id field
    created_employee = await db.employees.find_one({'id': emp_dict['id']}, {'_id': 0})
    return created_employee


@api_router.get("/employees", response_model=List[Dict[str, Any]])
async def get_employees(
    employee_type: Optional[str] = None,
    status: Optional[str] = None,
    department: Optional[str] = None,
    current_user: dict = Depends(get_current_user)
):
    query = {}
    if employee_type:
        query['employee_type'] = employee_type
    if status:
        query['status'] = status
    if department:
        query['department'] = department
    
    employees = await db.employees.find(query, {'_id': 0}).to_list(1000)
    return employees


@api_router.get("/employees/{employee_id}", response_model=Dict[str, Any])
async def get_employee(employee_id: str, current_user: dict = Depends(get_current_user)):
    employee = await db.employees.find_one({'id': employee_id}, {'_id': 0})
    if not employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    return employee


@api_router.put("/employees/{employee_id}", response_model=Dict[str, Any])
async def update_employee(
    employee_id: str, 
    update_data: EmployeeUpdate,
    current_user: dict = Depends(get_current_user)
):
    employee = await db.employees.find_one({'id': employee_id}, {'_id': 0})
    if not employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    update_dict = {k: v for k, v in update_data.model_dump().items() if v is not None}
    update_dict['updated_at'] = datetime.now(timezone.utc).isoformat()
    
    await db.employees.update_one({'id': employee_id}, {'$set': update_dict})
    
    updated = await db.employees.find_one({'id': employee_id}, {'_id': 0})
    return updated


@api_router.delete("/employees/{employee_id}")
async def delete_employee(employee_id: str, current_user: dict = Depends(get_current_user)):
    result = await db.employees.delete_one({'id': employee_id})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Employee not found")
    return {"message": "Employee deleted successfully"}


# ==================== ATTENDANCE ROUTES ====================

@api_router.post("/attendance/clock-in", response_model=Dict[str, Any])
async def clock_in(data: AttendanceClockIn, current_user: dict = Depends(get_current_user)):
    # Check if employee exists
    employee = await db.employees.find_one({'id': data.employee_id}, {'_id': 0})
    if not employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    # Check if already clocked in today
    today = datetime.now(timezone.utc).strftime('%Y-%m-%d')
    existing = await db.attendance.find_one({
        'employee_id': data.employee_id,
        'date': today,
        'clock_out': None
    })
    if existing:
        raise HTTPException(status_code=400, detail="Already clocked in for today")
    
    record = attendance_tracker.clock_in(data.employee_id, data.timestamp)
    await db.attendance.insert_one(record)
    
    # Return the inserted record without MongoDB's _id field
    created_record = await db.attendance.find_one({'id': record['id']}, {'_id': 0})
    return created_record


@api_router.post("/attendance/clock-out", response_model=Dict[str, Any])
async def clock_out(data: AttendanceClockOut, current_user: dict = Depends(get_current_user)):
    record = await db.attendance.find_one({'id': data.record_id}, {'_id': 0})
    if not record:
        raise HTTPException(status_code=404, detail="Attendance record not found")
    
    if record.get('clock_out'):
        raise HTTPException(status_code=400, detail="Already clocked out")
    
    updated_record = attendance_tracker.clock_out(record, data.timestamp)
    await db.attendance.update_one({'id': data.record_id}, {'$set': updated_record})
    
    return updated_record


@api_router.get("/attendance", response_model=List[Dict[str, Any]])
async def get_attendance(
    employee_id: Optional[str] = None,
    date: Optional[str] = None,
    start_date: Optional[str] = None,
    end_date: Optional[str] = None,
    current_user: dict = Depends(get_current_user)
):
    query = {}
    if employee_id:
        query['employee_id'] = employee_id
    if date:
        query['date'] = date
    if start_date and end_date:
        query['date'] = {'$gte': start_date, '$lte': end_date}
    
    records = await db.attendance.find(query, {'_id': 0}).sort('date', -1).to_list(1000)
    return records


@api_router.get("/attendance/today", response_model=List[Dict[str, Any]])
async def get_today_attendance(current_user: dict = Depends(get_current_user)):
    today = datetime.now(timezone.utc).strftime('%Y-%m-%d')
    records = await db.attendance.find({'date': today}, {'_id': 0}).to_list(1000)
    return records


@api_router.get("/attendance/summary/{employee_id}", response_model=Dict[str, Any])
async def get_attendance_summary(
    employee_id: str,
    year: int,
    month: int,
    current_user: dict = Depends(get_current_user)
):
    records = await db.attendance.find({
        'employee_id': employee_id,
        'date': {'$regex': f'^{year}-{month:02d}'}
    }, {'_id': 0}).to_list(100)
    
    summary = attendance_tracker.get_monthly_attendance(employee_id, year, month, records)
    return summary


# ==================== PAYROLL ROUTES ====================

@api_router.post("/payroll/process", response_model=List[Dict[str, Any]])
async def process_payroll(request: PayrollProcessRequest, current_user: dict = Depends(get_current_user)):
    # Get employees
    query = {}
    if request.employee_ids:
        query['id'] = {'$in': request.employee_ids}
    query['status'] = 'active'
    
    employees = await db.employees.find(query, {'_id': 0}).to_list(1000)
    
    if not employees:
        raise HTTPException(status_code=404, detail="No active employees found")
    
    # Get attendance for the pay period
    all_attendance = {}
    for emp in employees:
        records = await db.attendance.find({
            'employee_id': emp['id'],
            'date': {'$gte': request.pay_period_start, '$lte': request.pay_period_end}
        }, {'_id': 0}).to_list(100)
        all_attendance[emp['id']] = records
    
    # Process payroll using PayrollProcessor (OOP)
    payslips = payroll_processor.process_batch_payroll(
        employees, all_attendance, request.pay_period_start, request.pay_period_end
    )
    
    # Store payslips
    payslip_dicts = []
    for ps in payslips:
        ps_dict = ps.to_dict()
        await db.payslips.insert_one(ps_dict)
        # Get the inserted payslip without MongoDB's _id field
        created_payslip = await db.payslips.find_one({'id': ps_dict['id']}, {'_id': 0})
        payslip_dicts.append(created_payslip)
    
    return payslip_dicts


@api_router.get("/payroll/payslips", response_model=List[Dict[str, Any]])
async def get_payslips(
    employee_id: Optional[str] = None,
    pay_period_start: Optional[str] = None,
    pay_period_end: Optional[str] = None,
    current_user: dict = Depends(get_current_user)
):
    query = {}
    if employee_id:
        query['employee_id'] = employee_id
    if pay_period_start:
        query['pay_period_start'] = {'$gte': pay_period_start}
    if pay_period_end:
        query['pay_period_end'] = {'$lte': pay_period_end}
    
    payslips = await db.payslips.find(query, {'_id': 0}).sort('generated_at', -1).to_list(1000)
    return payslips


@api_router.get("/payroll/payslip/{payslip_id}", response_model=Dict[str, Any])
async def get_payslip(payslip_id: str, current_user: dict = Depends(get_current_user)):
    payslip = await db.payslips.find_one({'id': payslip_id}, {'_id': 0})
    if not payslip:
        raise HTTPException(status_code=404, detail="Payslip not found")
    return payslip


@api_router.post("/payroll/estimate", response_model=Dict[str, Any])
async def estimate_payroll(employee_id: str, current_user: dict = Depends(get_current_user)):
    employee = await db.employees.find_one({'id': employee_id}, {'_id': 0})
    if not employee:
        raise HTTPException(status_code=404, detail="Employee not found")
    
    estimate = payroll_processor.calculate_quick_estimate(employee)
    return estimate


@api_router.get("/payroll/summary", response_model=Dict[str, Any])
async def get_payroll_summary(
    pay_period_start: str,
    pay_period_end: str,
    current_user: dict = Depends(get_current_user)
):
    payslips_data = await db.payslips.find({
        'pay_period_start': pay_period_start,
        'pay_period_end': pay_period_end
    }, {'_id': 0}).to_list(1000)
    
    # Convert to Payslip objects for summary calculation
    payslips = []
    for ps in payslips_data:
        payslips.append(Payslip(
            id=ps['id'],
            employee_id=ps['employee_id'],
            employee_name=ps.get('employee_name', ''),
            employee_type=ps.get('employee_type', ''),
            department=ps.get('department', ''),
            position=ps.get('position', ''),
            pay_period_start=ps['pay_period_start'],
            pay_period_end=ps['pay_period_end'],
            days_worked=ps.get('days_worked', 0),
            hours_worked=ps.get('hours_worked', 0),
            basic_pay=ps.get('earnings', {}).get('basic_pay', 0),
            overtime_hours=ps.get('earnings', {}).get('overtime_hours', 0),
            overtime_pay=ps.get('earnings', {}).get('overtime_pay', 0),
            gross_pay=ps.get('earnings', {}).get('gross_pay', 0),
            sss=ps.get('deductions', {}).get('sss', 0),
            philhealth=ps.get('deductions', {}).get('philhealth', 0),
            pagibig=ps.get('deductions', {}).get('pagibig', 0),
            withholding_tax=ps.get('deductions', {}).get('withholding_tax', 0),
            total_deductions=ps.get('deductions', {}).get('total', 0),
            net_pay=ps.get('net_pay', 0),
            status=ps.get('status', 'generated'),
            generated_at=ps.get('generated_at', '')
        ))
    
    summary = payroll_processor.get_payroll_summary(payslips)
    return summary


# ==================== DEDUCTION ROUTES ====================

@api_router.post("/deductions/calculate", response_model=Dict[str, Any])
async def calculate_deductions(request: DeductionCalculateRequest):
    result = deduction_calculator.calculate_all_deductions(request.gross_salary)
    return result


@api_router.get("/deductions/tables")
async def get_deduction_tables():
    """Get deduction calculation tables (for reference/documentation)"""
    return {
        'sss': {
            'description': 'SSS Contribution Table (2024)',
            'brackets': len(deduction_calculator.SSS_TABLE),
            'min_contribution': 180.00,
            'max_contribution': 1350.00
        },
        'philhealth': {
            'description': 'PhilHealth Premium (2024)',
            'rate': '5% of salary',
            'min_premium': 500.00,
            'max_premium': 5000.00,
            'employee_share': '50%'
        },
        'pagibig': {
            'description': 'Pag-IBIG Fund Contribution',
            'rate_low': '1% for salary <= 1500',
            'rate_high': '2% for salary > 1500',
            'max_contribution': 100.00
        },
        'withholding_tax': {
            'description': 'TRAIN Law Tax Table (2024)',
            'brackets': [
                {'range': 'Up to ₱250,000/year', 'rate': 'Exempt'},
                {'range': '₱250,001 - ₱400,000', 'rate': '15% of excess over ₱250K'},
                {'range': '₱400,001 - ₱800,000', 'rate': '₱22,500 + 20% of excess over ₱400K'},
                {'range': '₱800,001 - ₱2,000,000', 'rate': '₱102,500 + 25% of excess over ₱800K'},
                {'range': '₱2,000,001 - ₱8,000,000', 'rate': '₱402,500 + 30% of excess over ₱2M'},
                {'range': 'Over ₱8,000,000', 'rate': '₱2,202,500 + 35% of excess over ₱8M'}
            ]
        }
    }


# ==================== DASHBOARD ROUTES ====================

@api_router.get("/dashboard/stats", response_model=Dict[str, Any])
async def get_dashboard_stats(current_user: dict = Depends(get_current_user)):
    # Employee counts
    total_employees = await db.employees.count_documents({'status': 'active'})
    full_time = await db.employees.count_documents({'status': 'active', 'employee_type': 'full_time'})
    part_time = await db.employees.count_documents({'status': 'active', 'employee_type': 'part_time'})
    contract = await db.employees.count_documents({'status': 'active', 'employee_type': 'contract'})
    
    # Recent employees
    recent_employees = await db.employees.find(
        {'status': 'active'}, {'_id': 0}
    ).sort('created_at', -1).limit(5).to_list(5)
    
    # Calculate monthly payroll estimate
    employees = await db.employees.find({'status': 'active'}, {'_id': 0}).to_list(1000)
    total_monthly_payroll = 0
    
    for emp in employees:
        try:
            employee_obj = create_employee(emp)
            salary = employee_obj.compute_salary()
            total_monthly_payroll += salary
        except Exception:
            continue
    
    # Today's attendance
    today = datetime.now(timezone.utc).strftime('%Y-%m-%d')
    today_attendance = await db.attendance.count_documents({'date': today})
    
    return {
        'total_employees': total_employees,
        'employee_types': {
            'full_time': full_time,
            'part_time': part_time,
            'contract': contract
        },
        'monthly_payroll': round(total_monthly_payroll, 2),
        'today_attendance': today_attendance,
        'recent_employees': recent_employees
    }


# ==================== OOP CONCEPTS ROUTES ====================

@api_router.get("/oop/class-hierarchy")
async def get_class_hierarchy():
    """Get the OOP class hierarchy information for visualization"""
    return {
        'classes': [
            {
                'name': 'Employee',
                'type': 'abstract_class',
                'access': 'public',
                'description': 'Parent class for all employee types',
                'attributes': [
                    {'name': 'id', 'access': 'protected', 'type': 'str'},
                    {'name': 'employee_id', 'access': 'protected', 'type': 'str'},
                    {'name': 'first_name', 'access': 'protected', 'type': 'str'},
                    {'name': 'last_name', 'access': 'protected', 'type': 'str'},
                    {'name': 'email', 'access': 'protected', 'type': 'str'},
                    {'name': 'department', 'access': 'protected', 'type': 'str'},
                    {'name': 'position', 'access': 'protected', 'type': 'str'},
                    {'name': 'basic_salary', 'access': 'private', 'type': 'float'},
                ],
                'methods': [
                    {'name': 'compute_salary()', 'access': 'public', 'type': 'abstract'},
                    {'name': 'get_salary_breakdown()', 'access': 'public', 'type': 'abstract'},
                    {'name': 'to_dict()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'full_name', 'access': 'public', 'type': 'property'},
                ]
            },
            {
                'name': 'FullTimeEmployee',
                'type': 'class',
                'extends': 'Employee',
                'access': 'public',
                'description': 'Full-time employees with monthly salary',
                'attributes': [
                    {'name': 'hours_per_day', 'access': 'protected', 'type': 'int'},
                    {'name': 'days_per_month', 'access': 'protected', 'type': 'int'},
                ],
                'methods': [
                    {'name': 'compute_salary()', 'access': 'public', 'type': 'override', 
                     'description': 'Calculates monthly salary with overtime'},
                    {'name': 'get_salary_breakdown()', 'access': 'public', 'type': 'override'},
                ]
            },
            {
                'name': 'PartTimeEmployee',
                'type': 'class',
                'extends': 'Employee',
                'access': 'public',
                'description': 'Part-time employees with hourly rate',
                'attributes': [
                    {'name': 'hourly_rate', 'access': 'protected', 'type': 'float'},
                    {'name': 'hours_per_week', 'access': 'protected', 'type': 'float'},
                ],
                'methods': [
                    {'name': 'compute_salary()', 'access': 'public', 'type': 'override',
                     'description': 'Calculates salary based on hours worked'},
                    {'name': 'get_salary_breakdown()', 'access': 'public', 'type': 'override'},
                ]
            },
            {
                'name': 'ContractEmployee',
                'type': 'class',
                'extends': 'Employee',
                'access': 'public',
                'description': 'Contract employees with daily rate',
                'attributes': [
                    {'name': 'daily_rate', 'access': 'protected', 'type': 'float'},
                    {'name': 'contract_end_date', 'access': 'protected', 'type': 'str'},
                ],
                'methods': [
                    {'name': 'compute_salary()', 'access': 'public', 'type': 'override',
                     'description': 'Calculates salary based on days worked'},
                    {'name': 'get_salary_breakdown()', 'access': 'public', 'type': 'override'},
                ]
            },
            {
                'name': 'PayrollProcessor',
                'type': 'service_class',
                'access': 'public',
                'description': 'Central payroll processing orchestrator',
                'relationships': [
                    {'type': 'composition', 'target': 'DeductionCalculator'},
                    {'type': 'composition', 'target': 'AttendanceTracker'},
                    {'type': 'uses', 'target': 'Employee'}
                ],
                'methods': [
                    {'name': 'process_payroll()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'process_batch_payroll()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_quick_estimate()', 'access': 'public', 'type': 'concrete'},
                ]
            },
            {
                'name': 'DeductionCalculator',
                'type': 'service_class',
                'access': 'public',
                'description': 'Handles all payroll deduction calculations',
                'methods': [
                    {'name': 'calculate_sss()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_philhealth()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_pagibig()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_withholding_tax()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_all_deductions()', 'access': 'public', 'type': 'concrete'},
                ]
            },
            {
                'name': 'AttendanceTracker',
                'type': 'service_class',
                'access': 'public',
                'description': 'Employee attendance tracking service',
                'methods': [
                    {'name': 'clock_in()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'clock_out()', 'access': 'public', 'type': 'concrete'},
                    {'name': 'calculate_period_summary()', 'access': 'public', 'type': 'concrete'},
                ]
            }
        ],
        'relationships': [
            {'from': 'FullTimeEmployee', 'to': 'Employee', 'type': 'inheritance'},
            {'from': 'PartTimeEmployee', 'to': 'Employee', 'type': 'inheritance'},
            {'from': 'ContractEmployee', 'to': 'Employee', 'type': 'inheritance'},
            {'from': 'PayrollProcessor', 'to': 'DeductionCalculator', 'type': 'composition'},
            {'from': 'PayrollProcessor', 'to': 'AttendanceTracker', 'type': 'composition'},
            {'from': 'PayrollProcessor', 'to': 'Employee', 'type': 'dependency'},
        ],
        'oop_concepts': {
            'inheritance': {
                'description': 'FullTimeEmployee, PartTimeEmployee, and ContractEmployee inherit from Employee',
                'example': 'class FullTimeEmployee(Employee):'
            },
            'polymorphism': {
                'description': 'compute_salary() method behaves differently based on employee type',
                'example': 'Each subclass overrides compute_salary() with its own implementation'
            },
            'encapsulation': {
                'description': 'Private and protected attributes with getters/setters',
                'example': '__basic_salary is private, accessed via @property decorator'
            },
            'abstraction': {
                'description': 'Employee is an abstract class with abstract methods',
                'example': '@abstractmethod def compute_salary()'
            },
            'composition': {
                'description': 'PayrollProcessor composes DeductionCalculator and AttendanceTracker',
                'example': 'self._deduction_calculator = DeductionCalculator()'
            }
        }
    }


# ==================== HEALTH CHECK ====================

@api_router.get("/")
async def root():
    return {"message": "MotorPH OOP Payroll System API", "version": "2.0.0"}


@api_router.get("/health")
async def health_check():
    return {"status": "healthy", "timestamp": datetime.now(timezone.utc).isoformat()}


# Include the router in the main app
app.include_router(api_router)

app.add_middleware(
    CORSMiddleware,
    allow_credentials=True,
    allow_origins=os.environ.get('CORS_ORIGINS', '*').split(','),
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
async def startup_event():
    """Create indexes on startup"""
    await db.employees.create_index("employee_id", unique=True)
    await db.employees.create_index("email")
    await db.users.create_index("email", unique=True)
    await db.attendance.create_index([("employee_id", 1), ("date", 1)])
    await db.payslips.create_index([("employee_id", 1), ("pay_period_start", 1)])
    logger.info("Database indexes created")


@app.on_event("shutdown")
async def shutdown_db_client():
    client.close()
