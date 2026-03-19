from typing import Dict, Any
from dataclasses import dataclass  # dataclass is a convenient way to create simple classes


# =============================================================================
# DATA CLASS FOR DEDUCTION BREAKDOWN
# =============================================================================
# @dataclass automatically generates __init__, __repr__, etc.
# It's a simpler way to create classes that mainly hold data
# =============================================================================
@dataclass
class DeductionBreakdown:
    """
    Data class to hold deduction amounts.
    Using @dataclass decorator automatically creates:
    - __init__ method
    - __repr__ method
    - __eq__ method
    This is a Pythonic way to create simple data-holding classes.
    """
    sss: float              # SSS contribution amount
    philhealth: float       # PhilHealth contribution amount
    pagibig: float          # Pag-IBIG contribution amount
    withholding_tax: float  # Withholding tax amount
    total_deductions: float # Sum of all deductions


# =============================================================================
# DEDUCTION CALCULATOR CLASS
# =============================================================================
class DeductionCalculator:
    """
    SERVICE CLASS: Handles all payroll deduction calculations
    
    OOP CONCEPTS DEMONSTRATED:
    --------------------------
    1. SINGLE RESPONSIBILITY PRINCIPLE (SRP):
       - This class has ONE responsibility: calculate deductions
       - It doesn't handle employees, attendance, or payroll processing
       - Each method handles ONE specific type of deduction
    
    2. ENCAPSULATION:
       - All tax tables and calculation logic are contained within this class
       - External code just calls calculate_all_deductions() without knowing the details
    
    References:
    - 2024 SSS Contribution Table
    - 2024 PhilHealth Premium Rates
    - Pag-IBIG Contribution Guidelines
    - TRAIN Law (Tax Reform for Acceleration and Inclusion)
    """
    
    # =========================================================================
    # CLASS CONSTANTS - SSS CONTRIBUTION TABLE (2024)
    # =========================================================================
    # Format: (salary_ceiling, employee_contribution)
    # If salary <= ceiling, use that contribution amount
    # =========================================================================
    SSS_TABLE = [
        # (Salary Ceiling, Employee Contribution)
        (4250, 180.00),
        (4750, 202.50),
        (5250, 225.00),
        (5750, 247.50),
        (6250, 270.00),
        (6750, 292.50),
        (7250, 315.00),
        (7750, 337.50),
        (8250, 360.00),
        (8750, 382.50),
        (9250, 405.00),
        (9750, 427.50),
        (10250, 450.00),
        (10750, 472.50),
        (11250, 495.00),
        (11750, 517.50),
        (12250, 540.00),
        (12750, 562.50),
        (13250, 585.00),
        (13750, 607.50),
        (14250, 630.00),
        (14750, 652.50),
        (15250, 675.00),
        (15750, 697.50),
        (16250, 720.00),
        (16750, 742.50),
        (17250, 765.00),
        (17750, 787.50),
        (18250, 810.00),
        (18750, 832.50),
        (19250, 855.00),
        (19750, 877.50),
        (20250, 900.00),
        (20750, 922.50),
        (21250, 945.00),
        (21750, 967.50),
        (22250, 990.00),
        (22750, 1012.50),
        (23250, 1035.00),
        (23750, 1057.50),
        (24250, 1080.00),
        (24750, 1102.50),
        (25250, 1125.00),
        (25750, 1147.50),
        (26250, 1170.00),
        (26750, 1192.50),
        (27250, 1215.00),
        (27750, 1237.50),
        (28250, 1260.00),
        (28750, 1282.50),
        (29250, 1305.00),
        (float('inf'), 1350.00)  # Maximum contribution for any salary above 29250
    ]
    
    # =========================================================================
    # CLASS CONSTANTS - PHILHEALTH RATES (2024)
    # =========================================================================
    PHILHEALTH_RATE = 0.05      # 5% of monthly salary
    PHILHEALTH_MIN = 500.00     # Minimum total premium
    PHILHEALTH_MAX = 5000.00    # Maximum total premium
    
    # =========================================================================
    # CLASS CONSTANTS - PAG-IBIG RATES
    # =========================================================================
    PAGIBIG_RATE_LOW = 0.01     # 1% for salary <= 1500
    PAGIBIG_RATE_HIGH = 0.02    # 2% for salary > 1500
    PAGIBIG_MAX = 100.00        # Maximum employee contribution
    
    # =========================================================================
    # CLASS CONSTANTS - TRAIN LAW TAX BRACKETS (2024)
    # =========================================================================
    # Format: (monthly_ceiling, monthly_floor, base_tax, tax_rate_on_excess)
    # Tax = base_tax + (taxable_income - floor) * rate
    # =========================================================================
    TAX_BRACKETS = [
        # Monthly amounts (Annual / 12)
        # (Ceiling, Floor, Base Tax, Rate on Excess)
        (20833, 0, 0, 0),                  # Up to 250K/year: EXEMPT
        (33333, 20833, 0, 0.15),           # 250K-400K/year: 15% of excess
        (66667, 33333, 1875, 0.20),        # 400K-800K/year: 1875 + 20% of excess
        (166667, 66667, 8541.67, 0.25),    # 800K-2M/year: 8541.67 + 25% of excess
        (666667, 166667, 33541.67, 0.30),  # 2M-8M/year: 33541.67 + 30% of excess
        (float('inf'), 666667, 183541.67, 0.35)  # Over 8M/year: 35% of excess
    ]
    
    def __init__(self):
        """
        Initialize the DeductionCalculator.
        Currently doesn't need any instance variables, but the __init__ is
        included for consistency and future expansion.
        """
        pass  # No initialization needed, but method is here for OOP structure
    
    # =========================================================================
    # SSS CALCULATION
    # =========================================================================
    def calculate_sss(self, gross_salary: float) -> float:
        """
        Calculate SSS (Social Security System) contribution.
        
        How SSS works:
        - SSS uses a bracket/table system
        - Find the salary bracket, use that contribution amount
        - The contribution is a fixed amount based on salary range
        
        Parameters:
            gross_salary (float): Monthly gross salary
            
        Returns:
            float: SSS employee contribution
            
        Example:
            If salary is 15,000, contribution is 675.00
        """
        # Handle edge case: no salary = no contribution
        if gross_salary <= 0:
            return 0.0
        
        # Find the correct bracket in the SSS table
        for ceiling, contribution in self.SSS_TABLE:
            if gross_salary <= ceiling:
                return contribution
        
        # If somehow above all brackets, return maximum
        return self.SSS_TABLE[-1][1]
    
    # =========================================================================
    # PHILHEALTH CALCULATION
    # =========================================================================
    def calculate_philhealth(self, gross_salary: float) -> float:
        """
        Calculate PhilHealth (Health Insurance) contribution.
        
        How PhilHealth works:
        - Premium is 5% of monthly salary
        - Total premium has minimum (500) and maximum (5000) caps
        - Employee pays HALF of the total premium (employer pays other half)
        
        Parameters:
            gross_salary (float): Monthly gross salary
            
        Returns:
            float: PhilHealth employee contribution (half of total premium)
        """
        # Handle edge case
        if gross_salary <= 0:
            return 0.0
        
        # Calculate total premium (5% of salary)
        total_premium = gross_salary * self.PHILHEALTH_RATE
        
        # Apply minimum and maximum caps
        # max() ensures we don't go below minimum
        # min() ensures we don't go above maximum
        total_premium = max(self.PHILHEALTH_MIN, min(total_premium, self.PHILHEALTH_MAX))
        
        # Employee pays half of total premium
        employee_share = total_premium / 2
        
        return round(employee_share, 2)
    
    # =========================================================================
    # PAG-IBIG CALCULATION
    # =========================================================================
    def calculate_pagibig(self, gross_salary: float) -> float:
        """
        Calculate Pag-IBIG (Housing Fund) contribution.
        
        How Pag-IBIG works:
        - If salary <= 1500: contribute 1%
        - If salary > 1500: contribute 2%
        - Maximum contribution is capped at 100
        
        Parameters:
            gross_salary (float): Monthly gross salary
            
        Returns:
            float: Pag-IBIG employee contribution
        """
        # Handle edge case
        if gross_salary <= 0:
            return 0.0
        
        # Determine rate based on salary
        if gross_salary <= 1500:
            contribution = gross_salary * self.PAGIBIG_RATE_LOW  # 1%
        else:
            contribution = gross_salary * self.PAGIBIG_RATE_HIGH  # 2%
        
        # Apply maximum cap
        contribution = min(round(contribution, 2), self.PAGIBIG_MAX)
        
        return contribution
    
    # =========================================================================
    # WITHHOLDING TAX CALCULATION
    # =========================================================================
    def calculate_withholding_tax(self, gross_salary: float, total_deductions: float) -> float:
        """
        Calculate Withholding Tax using TRAIN Law brackets.
        
        How Withholding Tax works:
        1. Taxable Income = Gross Salary - Mandatory Deductions (SSS, PhilHealth, Pag-IBIG)
        2. Find the tax bracket for the taxable income
        3. Tax = Base Tax + (Excess over floor × Rate)
        
        TRAIN Law Features:
        - First 250,000/year (20,833/month) is TAX EXEMPT
        - Progressive rates: higher income = higher rate
        
        Parameters:
            gross_salary (float): Monthly gross salary
            total_deductions (float): Total mandatory deductions (SSS + PhilHealth + Pag-IBIG)
            
        Returns:
            float: Withholding tax amount
        """
        # Calculate taxable income (gross minus mandatory deductions)
        taxable_income = gross_salary - total_deductions
        
        # Handle edge case: no taxable income = no tax
        if taxable_income <= 0:
            return 0.0
        
        # Find the correct tax bracket
        for ceiling, floor, base_tax, rate in self.TAX_BRACKETS:
            if taxable_income <= ceiling:
                # Calculate excess over the floor
                excess = taxable_income - floor
                # Tax = base tax + (excess × rate)
                tax = base_tax + (excess * rate)
                return round(max(0, tax), 2)
        
        return 0.0
    
    # =========================================================================
    # MAIN CALCULATION METHOD - Calculates ALL deductions
    # =========================================================================
    def calculate_all_deductions(self, gross_salary: float) -> Dict[str, Any]:
        """
        Calculate ALL deductions for a given gross salary.
        
        This is the main method that external code should call.
        It orchestrates all the individual calculation methods.
        
        Process:
        1. Calculate SSS
        2. Calculate PhilHealth
        3. Calculate Pag-IBIG
        4. Sum mandatory deductions
        5. Calculate Withholding Tax (after mandatory deductions)
        6. Calculate total deductions and net salary
        
        Parameters:
            gross_salary (float): Monthly gross salary
            
        Returns:
            dict: Complete breakdown of all deductions and net salary
        """
        # Step 1-3: Calculate individual mandatory deductions
        sss = self.calculate_sss(gross_salary)
        philhealth = self.calculate_philhealth(gross_salary)
        pagibig = self.calculate_pagibig(gross_salary)
        
        # Step 4: Sum mandatory deductions (needed for tax calculation)
        mandatory_deductions = sss + philhealth + pagibig
        
        # Step 5: Calculate withholding tax AFTER mandatory deductions
        withholding_tax = self.calculate_withholding_tax(gross_salary, mandatory_deductions)
        
        # Step 6: Calculate totals
        total_deductions = mandatory_deductions + withholding_tax
        net_salary = gross_salary - total_deductions
        
        # Return complete breakdown
        return {
            'gross_salary': round(gross_salary, 2),
            'deductions': {
                'sss': round(sss, 2),
                'philhealth': round(philhealth, 2),
                'pagibig': round(pagibig, 2),
                'withholding_tax': round(withholding_tax, 2),
                'total': round(total_deductions, 2)
            },
            'net_salary': round(net_salary, 2)
        }
    
    # =========================================================================
    # HELPER METHOD - Returns deductions as a dataclass
    # =========================================================================
    def get_deduction_breakdown(self, gross_salary: float) -> DeductionBreakdown:
        """
        Get deduction breakdown as a DeductionBreakdown dataclass.
        
        This is an alternative to calculate_all_deductions() that returns
        a structured dataclass instead of a dictionary.
        
        Parameters:
            gross_salary (float): Monthly gross salary
            
        Returns:
            DeductionBreakdown: Dataclass with all deduction amounts
        """
        result = self.calculate_all_deductions(gross_salary)
        deductions = result['deductions']
        
        return DeductionBreakdown(
            sss=deductions['sss'],
            philhealth=deductions['philhealth'],
            pagibig=deductions['pagibig'],
            withholding_tax=deductions['withholding_tax'],
            total_deductions=deductions['total']
        )


# =============================================================================
# END OF MODULE
# =============================================================================
# Summary:
# - DeductionCalculator demonstrates SINGLE RESPONSIBILITY PRINCIPLE
# - Each method has ONE job: calculate ONE type of deduction
# - All Philippine payroll deduction rules are ENCAPSULATED in this class
# - External code doesn't need to know HOW deductions are calculated
# =============================================================================
