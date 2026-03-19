from abc import ABC, abstractmethod  # ABC = Abstract Base Class, needed for creating abstract classes
from typing import Optional, Dict, Any
from datetime import datetime, timezone
from pydantic import BaseModel, Field  # Pydantic is used for data validation
import uuid  # For generating unique IDs


# =============================================================================
# PYDANTIC MODEL - Used for data validation when receiving data from API
# This is separate from our OOP classes - it's just for validating input data
# =============================================================================
class EmployeeBase(BaseModel):
    """
    Pydantic model for validating employee data from API requests.
    This is NOT the same as our OOP Employee class - this is just for validation.
    """
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    employee_id: str  # Company employee ID like EMP001
    first_name: str
    last_name: str
    email: str
    department: str
    position: str
    employee_type: str  # Can be: 'full_time', 'part_time', or 'contract'
    date_hired: str
    status: str = "active"
    
    # Salary fields - different types use different fields
    basic_salary: float = 0.0   # Used by full-time employees
    hourly_rate: float = 0.0    # Used by part-time employees
    daily_rate: float = 0.0     # Used by contract employees
    
    # Optional fields for specific employee types
    hours_per_week: Optional[float] = None      # For part-time
    contract_end_date: Optional[str] = None     # For contractors
    
    created_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    updated_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())


# =============================================================================
# ABSTRACT BASE CLASS - EMPLOYEE
# =============================================================================
# This is the PARENT CLASS that all employee types will inherit from.
# It's ABSTRACT because we don't want to create Employee objects directly -
# we only want to create specific types like FullTimeEmployee, etc.
# =============================================================================
class Employee(ABC):
   
    # =========================================================================
    # CONSTRUCTOR METHOD (__init__)
    # This runs when we create a new employee object
    # =========================================================================
    def __init__(self, data: Dict[str, Any]):
    
        # PROTECTED attributes (single underscore) - accessible by subclasses
        # We use protected because child classes need to access these
        self._id = data.get('id', str(uuid.uuid4()))
        self._employee_id = data['employee_id']
        self._first_name = data['first_name']
        self._last_name = data['last_name']
        self._email = data['email']
        self._department = data['department']
        self._position = data['position']
        self._employee_type = data['employee_type']
        self._date_hired = data['date_hired']
        self._status = data.get('status', 'active')
        
        # PRIVATE attribute (double underscore) - only this class can access directly
        # We make salary private to prevent direct modification without validation
        self.__basic_salary = data.get('basic_salary', 0.0)
        
        # Timestamps for tracking when records were created/updated
        self._created_at = data.get('created_at', datetime.now(timezone.utc).isoformat())
        self._updated_at = data.get('updated_at', datetime.now(timezone.utc).isoformat())
    
    # =========================================================================
    # ENCAPSULATION: GETTER PROPERTIES
    # Properties allow controlled access to private/protected attributes
    # =========================================================================
    
    @property
    def id(self) -> str:
        return self._id
    
    @property
    def employee_id(self) -> str:
        return self._employee_id
    
    @property
    def full_name(self) -> str:
        """
        COMPUTED PROPERTY - demonstrates encapsulation
        Instead of storing full_name, we compute it from first_name and last_name
        This ensures full_name is always consistent with the individual name parts
        """
        return f"{self._first_name} {self._last_name}"
    
    @property
    def first_name(self) -> str:
        """Getter for first name"""
        return self._first_name
    
    @property
    def last_name(self) -> str:
        """Getter for last name"""
        return self._last_name
    
    @property
    def email(self) -> str:
        """Getter for email"""
        return self._email
    
    @property
    def department(self) -> str:
        """Getter for department"""
        return self._department
    
    @property
    def position(self) -> str:
        """Getter for position/job title"""
        return self._position
    
    @property
    def employee_type(self) -> str:
        """Getter for employee type (full_time, part_time, contract)"""
        return self._employee_type
    
    @property
    def date_hired(self) -> str:
        """Getter for hire date"""
        return self._date_hired
    
    @property
    def status(self) -> str:
        """Getter for employment status (active, inactive, terminated)"""
        return self._status
    
    @property
    def basic_salary(self) -> float:
        """
        Getter for basic salary - ENCAPSULATION EXAMPLE
        The __basic_salary is private, but we provide controlled access through this property
        """
        return self.__basic_salary
    
    # =========================================================================
    # ENCAPSULATION: SETTER PROPERTIES
    # Setters allow us to add validation before changing attribute values
    # =========================================================================
    
    @basic_salary.setter
    def basic_salary(self, value: float):
        """
        Setter for basic salary with VALIDATION
        This demonstrates encapsulation - we control how the salary can be changed
        """
        # Validate that salary is not negative
        if value < 0:
            raise ValueError("Salary cannot be negative")
        self.__basic_salary = value
        # Update the timestamp when salary changes
        self._updated_at = datetime.now(timezone.utc).isoformat()
    
    @status.setter
    def status(self, value: str):
        """
        Setter for status with VALIDATION
        Only allows valid status values
        """
        valid_statuses = ['active', 'inactive', 'terminated']
        if value not in valid_statuses:
            raise ValueError(f"Invalid status. Must be one of: {valid_statuses}")
        self._status = value
        self._updated_at = datetime.now(timezone.utc).isoformat()
    
    # =========================================================================
    # ABSTRACT METHODS - Must be implemented by child classes
    # This is part of ABSTRACTION - defining what methods must exist
    # =========================================================================
    
    @abstractmethod
    def compute_salary(self, hours_worked: float = 0, days_worked: int = 0) -> float:
        """
        ABSTRACT METHOD - Must be overridden by each subclass
        
        This is the key to POLYMORPHISM:
        - FullTimeEmployee calculates based on monthly salary
        - PartTimeEmployee calculates based on hourly rate
        - ContractEmployee calculates based on daily rate
        
        Each subclass provides its own implementation!
        """
        pass
    
    @abstractmethod
    def get_salary_breakdown(self, hours_worked: float = 0, days_worked: int = 0) -> Dict[str, Any]:
        """
        ABSTRACT METHOD - Returns detailed salary breakdown
        Each subclass implements this differently based on their pay structure
        """
        pass
    
    # =========================================================================
    # CONCRETE METHODS - Shared by all subclasses
    # These methods are inherited and used as-is by child classes
    # =========================================================================
    
    def to_dict(self) -> Dict[str, Any]:
        """
        Convert employee object to dictionary for storing in database.
        This is a concrete method that all subclasses inherit.
        """
        return {
            'id': self._id,
            'employee_id': self._employee_id,
            'first_name': self._first_name,
            'last_name': self._last_name,
            'email': self._email,
            'department': self._department,
            'position': self._position,
            'employee_type': self._employee_type,
            'date_hired': self._date_hired,
            'status': self._status,
            'basic_salary': self.__basic_salary,
            'created_at': self._created_at,
            'updated_at': self._updated_at
        }
    
    def __str__(self) -> str:
        """String representation of employee - for printing/debugging"""
        return f"{self.employee_type.title()}Employee({self.employee_id}: {self.full_name})"
    
    def __repr__(self) -> str:
        """Official string representation - usually same as __str__"""
        return self.__str__()


# =============================================================================
# SUBCLASS 1: FULL-TIME EMPLOYEE
# =============================================================================
# This class INHERITS from Employee and OVERRIDES the abstract methods
# Full-time employees are paid a fixed monthly salary
# =============================================================================
class FullTimeEmployee(Employee):
    """
    SUBCLASS: Full-Time Employee
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. INHERITANCE: This class inherits all attributes and methods from Employee
                    using the syntax: class FullTimeEmployee(Employee)
    
    2. POLYMORPHISM: We OVERRIDE the compute_salary() method to calculate
                     salary based on monthly rate (different from other types)
    
    Salary Calculation:
    - Base: Monthly salary divided by working days
    - Overtime: Hours beyond 8/day get 25% premium
    """
    
    def __init__(self, data: Dict[str, Any]):
        """
        Initialize a Full-Time Employee
        
        First, we call the parent class constructor using super()
        This ensures all common attributes are initialized
        """
        # Call parent class constructor - IMPORTANT for inheritance!
        super().__init__(data)
        
        # Full-time specific attributes
        self._hours_per_day = 8          # Standard work hours per day
        self._days_per_month = 22        # Standard working days per month
    
    def compute_salary(self, hours_worked: float = 0, days_worked: int = 0) -> float:
        """
        POLYMORPHISM - This method behaves differently for full-time employees
        
        Calculation Logic:
        1. If attendance data provided: Calculate based on actual days worked
        2. If no attendance data: Return full monthly salary
        3. Overtime: Hours beyond 8/day get 25% premium
        """
        # Get monthly salary from parent class
        monthly_salary = self.basic_salary
        
        # If we have attendance data, calculate based on actual work
        if days_worked > 0:
            # Calculate daily rate from monthly salary
            daily_rate = self.basic_salary / self._days_per_month
            
            # Base pay for days worked (max is standard days per month)
            base_pay = daily_rate * min(days_worked, self._days_per_month)
            
            # Calculate overtime if applicable
            standard_hours = days_worked * self._hours_per_day
            if hours_worked > standard_hours:
                overtime_hours = hours_worked - standard_hours
                hourly_rate = daily_rate / self._hours_per_day
                # Overtime premium is 25% extra
                overtime_pay = overtime_hours * hourly_rate * 1.25
                return base_pay + overtime_pay
            
            return base_pay
        
        # If no attendance data, return full monthly salary
        return monthly_salary
    
    def get_salary_breakdown(self, hours_worked: float = 0, days_worked: int = 0) -> Dict[str, Any]:
        """
        Returns detailed salary breakdown for full-time employee
        This helps in generating itemized payslips
        """
        # Calculate rates
        daily_rate = self.basic_salary / self._days_per_month
        hourly_rate = daily_rate / self._hours_per_day
        
        # Calculate based on attendance or use defaults
        if days_worked > 0:
            base_pay = daily_rate * min(days_worked, self._days_per_month)
            standard_hours = days_worked * self._hours_per_day
            overtime_hours = max(0, hours_worked - standard_hours)
            overtime_pay = overtime_hours * hourly_rate * 1.25
        else:
            base_pay = self.basic_salary
            overtime_hours = 0
            overtime_pay = 0
        
        return {
            'employee_type': 'full_time',
            'monthly_salary': self.basic_salary,
            'daily_rate': round(daily_rate, 2),
            'hourly_rate': round(hourly_rate, 2),
            'days_worked': days_worked if days_worked > 0 else self._days_per_month,
            'hours_worked': hours_worked if hours_worked > 0 else self._days_per_month * self._hours_per_day,
            'base_pay': round(base_pay, 2),
            'overtime_hours': overtime_hours,
            'overtime_pay': round(overtime_pay, 2),
            'gross_salary': round(base_pay + overtime_pay, 2)
        }
    
    def to_dict(self) -> Dict[str, Any]:
        """
        OVERRIDE parent's to_dict to include full-time specific fields
        We call super().to_dict() to get parent data, then add our own
        """
        data = super().to_dict()  # Get parent class data first
        data['hours_per_day'] = self._hours_per_day
        data['days_per_month'] = self._days_per_month
        return data


# =============================================================================
# SUBCLASS 2: PART-TIME EMPLOYEE
# =============================================================================
# Part-time employees are paid based on hourly rate
# =============================================================================
class PartTimeEmployee(Employee):
    """
    SUBCLASS: Part-Time Employee
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. INHERITANCE: Inherits from Employee
    2. POLYMORPHISM: compute_salary() calculates based on HOURLY RATE
                     (different from full-time's monthly calculation)
    
    Salary Calculation:
    - Simple: hourly_rate × hours_worked
    """
    
    def __init__(self, data: Dict[str, Any]):
        """Initialize a Part-Time Employee"""
        super().__init__(data)  # Call parent constructor
        
        # Part-time specific attributes
        self._hourly_rate = data.get('hourly_rate', 0.0)
        self._hours_per_week = data.get('hours_per_week', 20)  # Default 20 hours/week
    
    @property
    def hourly_rate(self) -> float:
        """Getter for hourly rate"""
        return self._hourly_rate
    
    @hourly_rate.setter
    def hourly_rate(self, value: float):
        """Setter for hourly rate with validation"""
        if value < 0:
            raise ValueError("Hourly rate cannot be negative")
        self._hourly_rate = value
    
    @property
    def hours_per_week(self) -> float:
        """Getter for expected hours per week"""
        return self._hours_per_week
    
    def compute_salary(self, hours_worked: float = 0, days_worked: int = 0) -> float:
        """
        POLYMORPHISM - Part-time salary is hourly rate × hours worked
        
        This is DIFFERENT from FullTimeEmployee.compute_salary()!
        Same method name, different behavior = POLYMORPHISM
        """
        if hours_worked > 0:
            return self._hourly_rate * hours_worked
        
        # If no hours specified, estimate based on expected weekly hours
        # Assuming 4 weeks per month
        return self._hourly_rate * self._hours_per_week * 4
    
    def get_salary_breakdown(self, hours_worked: float = 0, days_worked: int = 0) -> Dict[str, Any]:
        """Returns detailed salary breakdown for part-time employee"""
        actual_hours = hours_worked if hours_worked > 0 else self._hours_per_week * 4
        gross = self._hourly_rate * actual_hours
        
        return {
            'employee_type': 'part_time',
            'hourly_rate': self._hourly_rate,
            'hours_per_week': self._hours_per_week,
            'hours_worked': actual_hours,
            'days_worked': days_worked,
            'base_pay': round(gross, 2),
            'overtime_hours': 0,  # Part-time doesn't have overtime concept
            'overtime_pay': 0,
            'gross_salary': round(gross, 2)
        }
    
    def to_dict(self) -> Dict[str, Any]:
        """Override to include part-time specific fields"""
        data = super().to_dict()
        data['hourly_rate'] = self._hourly_rate
        data['hours_per_week'] = self._hours_per_week
        return data


# =============================================================================
# SUBCLASS 3: CONTRACT EMPLOYEE
# =============================================================================
# Contract employees are paid based on daily rate
# =============================================================================
class ContractEmployee(Employee):
    """
    SUBCLASS: Contract Employee
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. INHERITANCE: Inherits from Employee
    2. POLYMORPHISM: compute_salary() calculates based on DAILY RATE
    
    Salary Calculation:
    - Simple: daily_rate × days_worked
    """
    
    def __init__(self, data: Dict[str, Any]):
        """Initialize a Contract Employee"""
        super().__init__(data)  # Call parent constructor
        
        # Contract-specific attributes
        self._daily_rate = data.get('daily_rate', 0.0)
        self._contract_end_date = data.get('contract_end_date')  # When contract expires
    
    @property
    def daily_rate(self) -> float:
        """Getter for daily rate"""
        return self._daily_rate
    
    @daily_rate.setter
    def daily_rate(self, value: float):
        """Setter for daily rate with validation"""
        if value < 0:
            raise ValueError("Daily rate cannot be negative")
        self._daily_rate = value
    
    @property
    def contract_end_date(self) -> Optional[str]:
        """Getter for contract end date"""
        return self._contract_end_date
    
    def compute_salary(self, hours_worked: float = 0, days_worked: int = 0) -> float:
        """
        POLYMORPHISM - Contract salary is daily rate × days worked
        
        Third different implementation of the same method!
        """
        if days_worked > 0:
            return self._daily_rate * days_worked
        
        # Default: Full month (22 working days)
        return self._daily_rate * 22
    
    def get_salary_breakdown(self, hours_worked: float = 0, days_worked: int = 0) -> Dict[str, Any]:
        """Returns detailed salary breakdown for contract employee"""
        actual_days = days_worked if days_worked > 0 else 22
        gross = self._daily_rate * actual_days
        
        return {
            'employee_type': 'contract',
            'daily_rate': self._daily_rate,
            'days_worked': actual_days,
            'hours_worked': hours_worked if hours_worked > 0 else actual_days * 8,
            'contract_end_date': self._contract_end_date,
            'base_pay': round(gross, 2),
            'overtime_hours': 0,
            'overtime_pay': 0,
            'gross_salary': round(gross, 2)
        }
    
    def to_dict(self) -> Dict[str, Any]:
        """Override to include contract-specific fields"""
        data = super().to_dict()
        data['daily_rate'] = self._daily_rate
        data['contract_end_date'] = self._contract_end_date
        return data


# =============================================================================
# FACTORY FUNCTION
# =============================================================================
# This is a FACTORY PATTERN - it creates the right type of object based on input
# =============================================================================
def create_employee(data: Dict[str, Any]) -> Employee:
    """
    FACTORY PATTERN - Creates the appropriate Employee subclass
    
    Why use a factory?
    - The caller doesn't need to know which specific class to use
    - We can add new employee types without changing calling code
    - Centralizes object creation logic
    
    Parameters:
        data (dict): Employee data including 'employee_type' field
        
    Returns:
        Employee: An instance of the appropriate subclass
        
    Raises:
        ValueError: If employee_type is not recognized
    """
    employee_type = data.get('employee_type', 'full_time')
    
    # Based on type, create the appropriate subclass
    if employee_type == 'full_time':
        return FullTimeEmployee(data)
    elif employee_type == 'part_time':
        return PartTimeEmployee(data)
    elif employee_type == 'contract':
        return ContractEmployee(data)
    else:
        raise ValueError(f"Unknown employee type: {employee_type}")


# =============================================================================
# END OF MODULE
# =============================================================================
# Summary of OOP Concepts in this file:
# 1. INHERITANCE: FullTimeEmployee, PartTimeEmployee, ContractEmployee extend Employee
# 2. POLYMORPHISM: compute_salary() has different implementations in each subclass
# 3. ENCAPSULATION: Private (__) and protected (_) attributes with property getters/setters
# 4. ABSTRACTION: Employee is abstract with abstract methods
# 5. FACTORY PATTERN: create_employee() creates the right subclass
# =============================================================================
