# Employee classes - demonstrates INHERITANCE and POLYMORPHISM
from .employee import (
    Employee,              # Abstract parent class
    FullTimeEmployee,      # Subclass - monthly salary
    PartTimeEmployee,      # Subclass - hourly rate
    ContractEmployee,      # Subclass - daily rate
    create_employee        # Factory function
)

# Payroll classes - demonstrates COMPOSITION
from .payroll import (
    PayrollProcessor,      # Main orchestrator class
    Payslip               # Data class for payslip
)

# Deduction calculator - demonstrates SINGLE RESPONSIBILITY
from .deductions import DeductionCalculator

# Attendance tracker - demonstrates SERVICE CLASS pattern
from .attendance import (
    AttendanceTracker,     # Service class
    AttendanceRecord       # Data class
)

__all__ = [
    # Employee hierarchy
    'Employee',
    'FullTimeEmployee',
    'PartTimeEmployee', 
    'ContractEmployee',
    'create_employee',
    
    # Payroll
    'PayrollProcessor',
    'Payslip',
    
    # Deductions
    'DeductionCalculator',
    
    # Attendance
    'AttendanceTracker',
    'AttendanceRecord'
]
