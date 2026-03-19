from typing import Dict, Any, List, Optional
from datetime import datetime, timezone, timedelta
from dataclasses import dataclass, field  # For creating simple data classes
from pydantic import BaseModel, Field  # For data validation
import uuid


# =============================================================================
# PYDANTIC MODEL - For validating attendance data from API
# =============================================================================
class AttendanceRecordBase(BaseModel):
    """
    Pydantic model for validating attendance record data.
    This ensures data coming from the API is in the correct format.
    """
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    employee_id: str                           # Which employee this record belongs to
    date: str                                  # Date in YYYY-MM-DD format
    clock_in: Optional[str] = None             # ISO datetime string when clocked in
    clock_out: Optional[str] = None            # ISO datetime string when clocked out
    hours_worked: float = 0.0                  # Calculated hours between clock in/out
    status: str = "present"                    # present, absent, late, half_day
    notes: Optional[str] = None                # Any additional notes
    created_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    updated_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).isoformat())


# =============================================================================
# DATA CLASS - For holding attendance record data
# =============================================================================
@dataclass
class AttendanceRecord:
    """
    Data class representing a single attendance record.
    
    Using @dataclass because:
    - Automatically generates __init__, __repr__, __eq__
    - Clean and simple way to hold data
    - Easy to convert to dictionary
    """
    id: str
    employee_id: str
    date: str
    clock_in: Optional[str] = None
    clock_out: Optional[str] = None
    hours_worked: float = 0.0
    status: str = "present"
    notes: Optional[str] = None
    # field(default_factory=...) is used for mutable defaults
    created_at: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    updated_at: str = field(default_factory=lambda: datetime.now(timezone.utc).isoformat())
    
    def to_dict(self) -> Dict[str, Any]:
        """
        Convert the dataclass to a dictionary.
        Useful for storing in database or returning in API response.
        """
        return {
            'id': self.id,
            'employee_id': self.employee_id,
            'date': self.date,
            'clock_in': self.clock_in,
            'clock_out': self.clock_out,
            'hours_worked': self.hours_worked,
            'status': self.status,
            'notes': self.notes,
            'created_at': self.created_at,
            'updated_at': self.updated_at
        }


# =============================================================================
# ATTENDANCE TRACKER - SERVICE CLASS
# =============================================================================
class AttendanceTracker:
    """
    SERVICE CLASS: Manages employee attendance tracking
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. SERVICE CLASS: This is a class that provides services/functionality
       rather than representing a real-world object like Employee.
       
    2. ENCAPSULATION: All attendance logic is contained within this class.
       External code doesn't need to know HOW we track attendance.
       
    3. SINGLE RESPONSIBILITY: This class ONLY handles attendance.
       It doesn't handle employees, payroll, or deductions.
    
    Responsibilities:
    - Handle clock-in operations
    - Handle clock-out operations
    - Calculate hours worked
    - Determine attendance status (present, late, absent, half_day)
    - Generate attendance summaries for payroll
    """
    
    # =========================================================================
    # CLASS CONSTANTS - Define standard work parameters
    # =========================================================================
    STANDARD_WORK_START = "09:00"      # Standard work day starts at 9 AM
    STANDARD_WORK_END = "18:00"        # Standard work day ends at 6 PM
    STANDARD_HOURS_PER_DAY = 8         # Standard 8-hour work day
    LATE_THRESHOLD_MINUTES = 15        # Minutes after start to be marked "late"
    
    def __init__(self):
        """
        Initialize the AttendanceTracker.
        No instance variables needed currently, but __init__ is included
        for proper OOP structure and future expansion.
        """
        pass
    
    # =========================================================================
    # CLOCK-IN METHOD
    # =========================================================================
    def clock_in(self, employee_id: str, timestamp: Optional[str] = None) -> Dict[str, Any]:
        """
        Record an employee's clock-in time.
        
        Process:
        1. Get the current time (or use provided timestamp)
        2. Determine if the employee is late
        3. Create an attendance record
        
        Parameters:
            employee_id (str): The employee's unique ID
            timestamp (str, optional): ISO datetime string. Defaults to current time.
            
        Returns:
            dict: The attendance record as a dictionary
            
        Example:
            tracker = AttendanceTracker()
            record = tracker.clock_in("EMP001")
            # Returns: {'id': '...', 'employee_id': 'EMP001', 'clock_in': '2024-01-15T09:05:00', ...}
        """
        # Get current time
        now = datetime.now(timezone.utc)
        
        # Use provided timestamp or current time
        clock_in_time = timestamp or now.isoformat()
        
        # Parse the clock-in time to determine if late
        if isinstance(clock_in_time, str):
            try:
                # Handle different ISO format variations
                parsed_time = datetime.fromisoformat(clock_in_time.replace('Z', '+00:00'))
            except ValueError:
                parsed_time = now
        else:
            parsed_time = now
        
        # =====================================================================
        # DETERMINE IF EMPLOYEE IS LATE
        # =====================================================================
        # Create a datetime for the standard work start time (9:00 AM)
        work_start = parsed_time.replace(hour=9, minute=0, second=0, microsecond=0)
        
        # Add the late threshold (15 minutes grace period)
        late_threshold = work_start + timedelta(minutes=self.LATE_THRESHOLD_MINUTES)
        
        # If clocked in after threshold, mark as late
        status = "present"
        if parsed_time > late_threshold:
            status = "late"
        
        # =====================================================================
        # CREATE ATTENDANCE RECORD
        # =====================================================================
        record = AttendanceRecord(
            id=str(uuid.uuid4()),       # Generate unique ID
            employee_id=employee_id,
            date=parsed_time.strftime('%Y-%m-%d'),  # Format: 2024-01-15
            clock_in=clock_in_time,
            status=status
        )
        
        return record.to_dict()
    
    # =========================================================================
    # CLOCK-OUT METHOD
    # =========================================================================
    def clock_out(self, record_data: Dict[str, Any], timestamp: Optional[str] = None) -> Dict[str, Any]:
        """
        Record an employee's clock-out time and calculate hours worked.
        
        Process:
        1. Get the clock-out time
        2. Calculate hours worked (clock_out - clock_in)
        3. Update status based on hours worked
        
        Parameters:
            record_data (dict): The existing attendance record from clock_in
            timestamp (str, optional): ISO datetime string. Defaults to current time.
            
        Returns:
            dict: The updated attendance record
            
        Raises:
            ValueError: If trying to clock out without a clock-in time
        """
        now = datetime.now(timezone.utc)
        clock_out_time = timestamp or now.isoformat()
        
        # =====================================================================
        # VALIDATE: Can't clock out without clocking in first
        # =====================================================================
        clock_in_str = record_data.get('clock_in')
        if not clock_in_str:
            raise ValueError("Cannot clock out without clock-in time")
        
        # =====================================================================
        # PARSE TIMESTAMPS
        # =====================================================================
        try:
            clock_in = datetime.fromisoformat(clock_in_str.replace('Z', '+00:00'))
            clock_out = datetime.fromisoformat(clock_out_time.replace('Z', '+00:00'))
        except ValueError as e:
            raise ValueError(f"Invalid datetime format: {e}")
        
        # =====================================================================
        # CALCULATE HOURS WORKED
        # =====================================================================
        # timedelta gives us the difference between two datetimes
        duration = clock_out - clock_in
        
        # Convert to hours (total_seconds / 3600 seconds per hour)
        hours_worked = duration.total_seconds() / 3600
        
        # Ensure non-negative and round to 2 decimal places
        hours_worked = round(max(0, hours_worked), 2)
        
        # =====================================================================
        # UPDATE STATUS BASED ON HOURS
        # =====================================================================
        status = record_data.get('status', 'present')
        
        # If worked less than 4 hours, mark as half day
        if hours_worked < 4:
            status = "half_day"
        # Note: If they were late but worked full hours, they stay marked as "late"
        
        # =====================================================================
        # UPDATE THE RECORD
        # =====================================================================
        record_data.update({
            'clock_out': clock_out_time,
            'hours_worked': hours_worked,
            'status': status,
            'updated_at': now.isoformat()
        })
        
        return record_data
    
    # =========================================================================
    # CALCULATE PERIOD SUMMARY
    # =========================================================================
    def calculate_period_summary(self, records: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Calculate attendance summary for a pay period.
        
        This method is used by PayrollProcessor to get attendance data
        for salary calculation.
        
        Parameters:
            records (list): List of attendance records for the period
            
        Returns:
            dict: Summary statistics including:
                - total_days: Number of days in the period
                - present_days: Days marked as present or late
                - late_days: Days marked as late
                - absent_days: Days marked as absent
                - half_days: Days marked as half day
                - total_hours_worked: Sum of all hours worked
                - average_hours_per_day: Average hours per present day
        """
        # Count different statuses
        total_days = len(records)
        
        # present_days includes both 'present' and 'late' (they still worked)
        present_days = sum(1 for r in records if r.get('status') in ['present', 'late'])
        
        late_days = sum(1 for r in records if r.get('status') == 'late')
        absent_days = sum(1 for r in records if r.get('status') == 'absent')
        half_days = sum(1 for r in records if r.get('status') == 'half_day')
        
        # Sum total hours worked
        total_hours = sum(r.get('hours_worked', 0) for r in records)
        
        # Calculate average (avoid division by zero)
        avg_hours = total_hours / max(present_days, 1)
        
        return {
            'total_days': total_days,
            'present_days': present_days,
            'late_days': late_days,
            'absent_days': absent_days,
            'half_days': half_days,
            'total_hours_worked': round(total_hours, 2),
            'average_hours_per_day': round(avg_hours, 2)
        }
    
    # =========================================================================
    # GET MONTHLY ATTENDANCE
    # =========================================================================
    def get_monthly_attendance(self, employee_id: str, year: int, month: int, 
                                records: List[Dict[str, Any]]) -> Dict[str, Any]:
        """
        Get monthly attendance summary for a specific employee.
        
        Parameters:
            employee_id (str): The employee's ID
            year (int): Year (e.g., 2024)
            month (int): Month number (1-12)
            records (list): All attendance records to filter from
            
        Returns:
            dict: Monthly summary with employee info and attendance stats
        """
        # Filter records for the specific employee and month
        # Date format is YYYY-MM-DD, so we check if it starts with YYYY-MM
        month_prefix = f"{year}-{month:02d}"  # Format: "2024-01" for January 2024
        
        filtered = [
            r for r in records 
            if r.get('employee_id') == employee_id and
            r.get('date', '').startswith(month_prefix)
        ]
        
        # Use the summary calculation method
        summary = self.calculate_period_summary(filtered)
        
        # Add employee and period info
        summary['employee_id'] = employee_id
        summary['year'] = year
        summary['month'] = month
        
        return summary


# =============================================================================
# END OF MODULE
# =============================================================================
# Summary:
# - AttendanceTracker is a SERVICE CLASS that provides attendance functionality
# - It demonstrates ENCAPSULATION by containing all attendance logic
# - It follows SINGLE RESPONSIBILITY by only handling attendance
# - It will be COMPOSED into PayrollProcessor for payroll calculations
# =============================================================================
