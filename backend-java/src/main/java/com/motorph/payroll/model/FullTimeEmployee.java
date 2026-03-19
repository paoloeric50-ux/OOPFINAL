package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * SUBCLASS 1: FULL-TIME EMPLOYEE
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. INHERITANCE: extends Employee (IS-A relationship)
 * 2. POLYMORPHISM: Overrides computeSalary() with monthly salary calculation
 * 3. ENCAPSULATION: Protected subclass-specific attributes
 */
public class FullTimeEmployee extends Employee {

    // Full-time specific attributes
    protected int hoursPerDay;    // Standard hours per day
    protected int daysPerMonth;   // Standard working days per month

    public FullTimeEmployee(Map<String, Object> data) {
        super(data);  // Call parent constructor - INHERITANCE
        this.hoursPerDay = 8;
        this.daysPerMonth = 22;
    }

    /**
     * POLYMORPHISM: Monthly salary-based calculation
     * Calculates based on actual days worked + overtime
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        double monthlySalary = getBasicSalary();

        if (daysWorked > 0) {
            double dailyRate = monthlySalary / daysPerMonth;
            double basePay = dailyRate * Math.min(daysWorked, daysPerMonth);

            double standardHours = daysWorked * hoursPerDay;
            if (hoursWorked > standardHours) {
                double overtimeHours = hoursWorked - standardHours;
                double hourlyRate = dailyRate / hoursPerDay;
                double overtimePay = overtimeHours * hourlyRate * 1.25;
                return basePay + overtimePay;
            }
            return basePay;
        }
        return monthlySalary;
    }

    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        double dailyRate = getBasicSalary() / daysPerMonth;
        double hourlyRate = dailyRate / hoursPerDay;

        double basePay, overtimeHours, overtimePay;

        if (daysWorked > 0) {
            basePay = dailyRate * Math.min(daysWorked, daysPerMonth);
            double standardHours = daysWorked * hoursPerDay;
            overtimeHours = Math.max(0, hoursWorked - standardHours);
            overtimePay = overtimeHours * hourlyRate * 1.25;
        } else {
            basePay = getBasicSalary();
            overtimeHours = 0;
            overtimePay = 0;
        }

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type", "full_time");
        breakdown.put("monthly_salary", getBasicSalary());
        breakdown.put("daily_rate", Math.round(dailyRate * 100.0) / 100.0);
        breakdown.put("hourly_rate", Math.round(hourlyRate * 100.0) / 100.0);
        breakdown.put("days_worked", daysWorked > 0 ? daysWorked : daysPerMonth);
        breakdown.put("hours_worked", hoursWorked > 0 ? hoursWorked : (daysPerMonth * hoursPerDay));
        breakdown.put("base_pay", Math.round(basePay * 100.0) / 100.0);
        breakdown.put("overtime_hours", overtimeHours);
        breakdown.put("overtime_pay", Math.round(overtimePay * 100.0) / 100.0);
        breakdown.put("gross_salary", Math.round((basePay + overtimePay) * 100.0) / 100.0);
        return breakdown;
    }

    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("hours_per_day", hoursPerDay);
        data.put("days_per_month", daysPerMonth);
        return data;
    }

    public int getHoursPerDay() { return hoursPerDay; }
    public int getDaysPerMonth() { return daysPerMonth; }
}
