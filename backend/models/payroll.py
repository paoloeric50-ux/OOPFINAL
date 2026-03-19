from typing import Dict, Any, List, Optional
from datetime import datetime, timezone
from dataclasses import dataclass
from pydantic import BaseModel, Field
import uuid

# Import our other OOP classes
from .employee import Employee, create_employee  # For employee handling and factory
from .deductions import DeductionCalculator      # For calculating deductions
from .attendance import AttendanceTracker        # For attendance summaries


# =============================================================================
# PYDANTIC MODEL - For validating payslip data
# =============================================================================
class PayslipBase(BaseModel):
    """Pydantic model for payslip data validation"""
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    employee_id: str
    pay_period_start: str
    pay_period_end: str
    
    # Earnings section
    basic_pay: float
    overtime_pay: float = 0.0
    gross_pay: float
    
    # Deductions section
    sss: float = 0.0
    philhealth: float = 0.0
    pagibig: float = 0.0
    withholding_tax: float = 0.0
    total_deductions: float = 0.0
    
    # Final amount
    net_pay: float
    
    # Status tracking
    status: str = "generated"  # generated, approved, paid
    generated_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    approved_at: Optional[str] = None
    paid_at: Optional[str] = None


# =============================================================================
# DATA CLASS - Payslip
# =============================================================================
@dataclass
class Payslip:
    """
    Data class representing a complete payslip.
    Contains all information needed for an employee's pay stub.
    """
    # Identifiers
    id: str
    employee_id: str
    employee_name: str
    employee_type: str
    department: str
    position: str
    
    # Pay period
    pay_period_start: str
    pay_period_end: str
    
    # Attendance data
    days_worked: int
    hours_worked: float
    
    # Earnings breakdown
    basic_pay: float
    overtime_hours: float
    overtime_pay: float
    gross_pay: float
    
    # Deductions breakdown
    sss: float
    philhealth: float
    pagibig: float
    withholding_tax: float
    total_deductions: float
    
    # Final amount
    net_pay: float
    
    # Status
    status: str
    generated_at: str
    
    def to_dict(self) -> Dict[str, Any]:
        """Convert payslip to dictionary for storage/display"""
        return {
            'id': self.id,
            'employee_id': self.employee_id,
            'employee_name': self.employee_name,
            'employee_type': self.employee_type,
            'department': self.department,
            'position': self.position,
            'pay_period_start': self.pay_period_start,
            'pay_period_end': self.pay_period_end,
            'days_worked': self.days_worked,
            'hours_worked': self.hours_worked,
            'earnings': {
                'basic_pay': self.basic_pay,
                'overtime_hours': self.overtime_hours,
                'overtime_pay': self.overtime_pay,
                'gross_pay': self.gross_pay
            },
            'deductions': {
                'sss': self.sss,
                'philhealth': self.philhealth,
                'pagibig': self.pagibig,
                'withholding_tax': self.withholding_tax,
                'total': self.total_deductions
            },
            'net_pay': self.net_pay,
            'status': self.status,
            'generated_at': self.generated_at
        }


# =============================================================================
# PAYROLL PROCESSOR - Main Service Class
# =============================================================================
class PayrollProcessor:
    """
    CENTRAL SERVICE CLASS: Orchestrates the entire payroll process
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. COMPOSITION ("HAS-A" relationship):
       - PayrollProcessor HAS-A DeductionCalculator
       - PayrollProcessor HAS-A AttendanceTracker
       Unlike inheritance ("IS-A"), composition means we USE other classes
       as PARTS of this class. This is often preferred over inheritance
       because it's more flexible.
       
    2. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
       - This class's ONE responsibility is to COORDINATE payroll processing
       - It doesn't calculate deductions itself (DeductionCalculator does that)
       - It doesn't track attendance itself (AttendanceTracker does that)
       - It doesn't calculate salary itself (Employee subclasses do that)
       
    3. POLYMORPHISM in action:
       - When we call employee.compute_salary(), different employee types
         calculate salary differently. PayrollProcessor doesn't need to know
         the details - it just calls the method and gets the right result.
    
    Why use Composition?
    - More flexible than inheritance
    - Easy to swap components (e.g., use a different DeductionCalculator)
    - Clear separation of concerns
    - Follows the principle: "Favor composition over inheritance"
    """
    
    def __init__(self):
        """
        Initialize the PayrollProcessor with its component services.
        
        COMPOSITION: We create instances of other classes and store them
        as attributes. This is the "HAS-A" relationship.
        """
        # =====================================================================
        # COMPOSITION - Creating component instances
        # =====================================================================
        # PayrollProcessor HAS-A DeductionCalculator
        self._deduction_calculator = DeductionCalculator()
        
        # PayrollProcessor HAS-A AttendanceTracker
        self._attendance_tracker = AttendanceTracker()
        
        # Note: We use underscore prefix (_) to indicate these are internal
        # components that shouldn't be directly accessed from outside
    
    # =========================================================================
    # PROPERTIES - Controlled access to component services
    # =========================================================================
    @property
    def deduction_calculator(self) -> DeductionCalculator:
        """
        Getter for DeductionCalculator component.
        Provides access to deduction calculations if needed externally.
        """
        return self._deduction_calculator
    
    @property
    def attendance_tracker(self) -> AttendanceTracker:
        """
        Getter for AttendanceTracker component.
        Provides access to attendance tracking if needed externally.
        """
        return self._attendance_tracker
    
    # =========================================================================
    # MAIN PAYROLL PROCESSING METHOD
    # =========================================================================
    def process_payroll(self, employee_data: Dict[str, Any], 
                        attendance_records: List[Dict[str, Any]],
                        pay_period_start: str,
                        pay_period_end: str) -> Payslip:
        """
        Process payroll for a single employee.
        
        This is the MAIN METHOD that orchestrates the entire payroll calculation.
        It demonstrates how COMPOSITION works - we use our component services
        to do their specialized jobs.
        
        Process Flow:
        1. Create Employee object (POLYMORPHISM - factory creates right subclass)
        2. Get attendance summary (using AttendanceTracker component)
        3. Calculate salary (POLYMORPHISM - each employee type calculates differently)
        4. Calculate deductions (using DeductionCalculator component)
        5. Create and return payslip
        
        Parameters:
            employee_data (dict): Employee information from database
            attendance_records (list): Attendance records for the pay period
            pay_period_start (str): Start date of pay period (YYYY-MM-DD)
            pay_period_end (str): End date of pay period (YYYY-MM-DD)
            
        Returns:
            Payslip: Complete payslip with all calculations
        """
        # =====================================================================
        # STEP 1: Create Employee Object (FACTORY PATTERN + POLYMORPHISM)
        # =====================================================================
        # The create_employee factory function returns the appropriate subclass
        # (FullTimeEmployee, PartTimeEmployee, or ContractEmployee)
        # based on the employee_type in the data
        employee = create_employee(employee_data)
        
        # =====================================================================
        # STEP 2: Calculate Attendance Summary (using COMPOSITION)
        # =====================================================================
        # We use our AttendanceTracker component to calculate attendance
        attendance_summary = self._attendance_tracker.calculate_period_summary(attendance_records)
        
        days_worked = attendance_summary['present_days']
        hours_worked = attendance_summary['total_hours_worked']
        
        # =====================================================================
        # STEP 3: Calculate Salary (POLYMORPHISM in action!)
        # =====================================================================
        # Here's where polymorphism shines!
        # We call employee.get_salary_breakdown() and it automatically uses
        # the correct calculation based on the employee type:
        # - FullTimeEmployee: monthly salary with overtime
        # - PartTimeEmployee: hourly rate × hours
        # - ContractEmployee: daily rate × days
        # We don't need to check the type - the right method is called!
        salary_breakdown = employee.get_salary_breakdown(hours_worked, days_worked)
        
        gross_pay = salary_breakdown['gross_salary']
        basic_pay = salary_breakdown['base_pay']
        overtime_pay = salary_breakdown.get('overtime_pay', 0)
        overtime_hours = salary_breakdown.get('overtime_hours', 0)
        
        # =====================================================================
        # STEP 4: Calculate Deductions (using COMPOSITION)
        # =====================================================================
        # We use our DeductionCalculator component to calculate all deductions
        deduction_result = self._deduction_calculator.calculate_all_deductions(gross_pay)
        deductions = deduction_result['deductions']
        
        # =====================================================================
        # STEP 5: Create and Return Payslip
        # =====================================================================
        payslip = Payslip(
            id=str(uuid.uuid4()),
            employee_id=employee.id,
            employee_name=employee.full_name,  # Using computed property from Employee
            employee_type=employee.employee_type,
            department=employee.department,
            position=employee.position,
            pay_period_start=pay_period_start,
            pay_period_end=pay_period_end,
            days_worked=days_worked,
            hours_worked=hours_worked,
            basic_pay=basic_pay,
            overtime_hours=overtime_hours,
            overtime_pay=overtime_pay,
            gross_pay=gross_pay,
            sss=deductions['sss'],
            philhealth=deductions['philhealth'],
            pagibig=deductions['pagibig'],
            withholding_tax=deductions['withholding_tax'],
            total_deductions=deductions['total'],
            net_pay=deduction_result['net_salary'],
            status='generated',
            generated_at=datetime.now(timezone.utc).isoformat()
        )
        
        return payslip
    
    # =========================================================================
    # BATCH PAYROLL PROCESSING
    # =========================================================================
    def process_batch_payroll(self, employees_data: List[Dict[str, Any]],
                              all_attendance: Dict[str, List[Dict[str, Any]]],
                              pay_period_start: str,
                              pay_period_end: str) -> List[Payslip]:
        """
        Process payroll for multiple employees at once.
        
        This method shows how we can scale our single-employee processing
        to handle many employees efficiently.
        
        Parameters:
            employees_data (list): List of employee data dictionaries
            all_attendance (dict): Dictionary mapping employee_id to their attendance records
            pay_period_start (str): Start of pay period
            pay_period_end (str): End of pay period
            
        Returns:
            list: List of Payslip objects for all processed employees
        """
        payslips = []
        
        # Process each employee
        for emp_data in employees_data:
            emp_id = emp_data.get('id')
            
            # Get this employee's attendance records (empty list if none)
            attendance = all_attendance.get(emp_id, [])
            
            try:
                # Use our main processing method
                payslip = self.process_payroll(
                    emp_data, attendance, pay_period_start, pay_period_end
                )
                payslips.append(payslip)
            except Exception as e:
                # Log error but continue with other employees
                # In production, we'd use proper logging
                print(f"Error processing payroll for {emp_id}: {e}")
                continue
        
        return payslips
    
    # =========================================================================
    # QUICK ESTIMATE (Without Attendance Data)
    # =========================================================================
    def calculate_quick_estimate(self, employee_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        Calculate a quick payroll estimate without actual attendance data.
        
        Useful for:
        - Giving employees an idea of their expected pay
        - Quick calculations for budgeting
        - Testing the system
        
        Parameters:
            employee_data (dict): Employee information
            
        Returns:
            dict: Estimated payroll breakdown with a note that it's an estimate
        """
        # Create employee object
        employee = create_employee(employee_data)
        
        # Get salary breakdown using default (full period) values
        salary_breakdown = employee.get_salary_breakdown()
        gross_pay = salary_breakdown['gross_salary']
        
        # Calculate deductions
        deduction_result = self._deduction_calculator.calculate_all_deductions(gross_pay)
        
        return {
            'employee_id': employee.id,
            'employee_name': employee.full_name,
            'employee_type': employee.employee_type,
            'salary_breakdown': salary_breakdown,
            'deductions': deduction_result['deductions'],
            'gross_pay': gross_pay,
            'net_pay': deduction_result['net_salary'],
            'note': 'This is an ESTIMATE based on standard working period'
        }
    
    # =========================================================================
    # PAYROLL SUMMARY
    # =========================================================================
    def get_payroll_summary(self, payslips: List[Payslip]) -> Dict[str, Any]:
        """
        Generate summary statistics for a batch of payslips.
        
        Useful for:
        - Management reports
        - Dashboard statistics
        - Payroll auditing
        
        Parameters:
            payslips (list): List of Payslip objects
            
        Returns:
            dict: Summary including totals and breakdown by employee type
        """
        # Handle empty list
        if not payslips:
            return {
                'total_employees': 0,
                'total_gross': 0,
                'total_deductions': 0,
                'total_net': 0,
                'by_type': {}
            }
        
        # Calculate totals
        total_gross = sum(p.gross_pay for p in payslips)
        total_deductions = sum(p.total_deductions for p in payslips)
        total_net = sum(p.net_pay for p in payslips)
        
        # Group statistics by employee type
        by_type = {}
        for payslip in payslips:
            emp_type = payslip.employee_type
            
            # Initialize if first of this type
            if emp_type not in by_type:
                by_type[emp_type] = {
                    'count': 0,
                    'total_gross': 0,
                    'total_net': 0
                }
            
            # Add to running totals
            by_type[emp_type]['count'] += 1
            by_type[emp_type]['total_gross'] += payslip.gross_pay
            by_type[emp_type]['total_net'] += payslip.net_pay
        
        return {
            'total_employees': len(payslips),
            'total_gross': round(total_gross, 2),
            'total_deductions': round(total_deductions, 2),
            'total_net': round(total_net, 2),
            'by_type': by_type
        }


# =============================================================================
# END OF MODULE
# =============================================================================
# Summary of OOP Concepts:
# 
# 1. COMPOSITION ("HAS-A"):
#    - PayrollProcessor HAS-A DeductionCalculator
#    - PayrollProcessor HAS-A AttendanceTracker
#    - This is different from inheritance ("IS-A")
#    - Composition is more flexible and follows "favor composition over inheritance"
#
# 2. POLYMORPHISM:
#    - When we call employee.compute_salary() or employee.get_salary_breakdown()
#    - The correct method is called based on the actual employee type
#    - FullTimeEmployee, PartTimeEmployee, ContractEmployee all have different implementations
#
# 3. SINGLE RESPONSIBILITY:
#    - PayrollProcessor coordinates, doesn't do everything itself
#    - DeductionCalculator calculates deductions
#    - AttendanceTracker tracks attendance
#    - Employee subclasses calculate salaries
#
# 4. FACTORY PATTERN:
#    - create_employee() creates the right Employee subclass
#    - PayrollProcessor doesn't need to know about employee types
# =============================================================================
