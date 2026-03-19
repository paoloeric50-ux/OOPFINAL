package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/*
 * FullTimeEmployee.java - Subclass for full-time employees
 *
 * This inherits from Employee and overrides the computeSalary() method
 * to calculate salary based on monthly rate + overtime pay.
 *
 * Overtime is 1.25x the regular hourly rate (standard Philippine labor law).
 *
 * Author: Group 5 - MO-IT101
 */
public class FullTimeEmployee extends Employee {

    // Full-time employees work 8 hours a day, 22 days a month by default
    protected int hoursPerDay;
    protected int daysPerMonth;

    // Constructor calls super() first to set all the common fields
    public FullTimeEmployee(Map<String, Object> data) {
        super(data);
        this.hoursPerDay   = 8;
        this.daysPerMonth  = 22;
    }

    /*
     * computeSalary() - overrides the abstract method from Employee
     * This is POLYMORPHISM - same method, different formula for full-time
     *
     * If days worked is given, compute based on actual attendance.
     * If no attendance data, just return the full monthly salary.
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        double monthlySalary = getBasicSalary();

        if (daysWorked > 0) {
            // daily rate = monthly salary divided by working days
            double dailyRate  = monthlySalary / daysPerMonth;
            double basePay    = dailyRate * Math.min(daysWorked, daysPerMonth);

            // check if employee worked more hours than expected
            double standardHours = daysWorked * hoursPerDay;
            if (hoursWorked > standardHours) {
                double overtimeHours = hoursWorked - standardHours;
                double hourlyRate    = dailyRate / hoursPerDay;
                double overtimePay   = overtimeHours * hourlyRate * 1.25; // OT rate is 125%
                return basePay + overtimePay;
            }
            return basePay;
        }

        // no attendance record, return full monthly salary
        return monthlySalary;
    }

    /*
     * getSalaryBreakdown() - returns a detailed breakdown of how the salary was computed
     * Useful for showing the employee their payslip details
     */
    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        double dailyRate  = getBasicSalary() / daysPerMonth;
        double hourlyRate = dailyRate / hoursPerDay;

        double basePay, overtimeHours, overtimePay;

        if (daysWorked > 0) {
            basePay       = dailyRate * Math.min(daysWorked, daysPerMonth);
            double standardHours = daysWorked * hoursPerDay;
            overtimeHours = Math.max(0, hoursWorked - standardHours);
            overtimePay   = overtimeHours * hourlyRate * 1.25;
        } else {
            // no attendance data, use the full monthly salary as base
            basePay       = getBasicSalary();
            overtimeHours = 0;
            overtimePay   = 0;
        }

        // put all the data into a map to return
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type",  "full_time");
        breakdown.put("monthly_salary", getBasicSalary());
        breakdown.put("daily_rate",     Math.round(dailyRate  * 100.0) / 100.0);
        breakdown.put("hourly_rate",    Math.round(hourlyRate * 100.0) / 100.0);
        breakdown.put("days_worked",    daysWorked  > 0 ? daysWorked  : daysPerMonth);
        breakdown.put("hours_worked",   hoursWorked > 0 ? hoursWorked : (daysPerMonth * hoursPerDay));
        breakdown.put("base_pay",       Math.round(basePay       * 100.0) / 100.0);
        breakdown.put("overtime_hours", overtimeHours);
        breakdown.put("overtime_pay",   Math.round(overtimePay   * 100.0) / 100.0);
        breakdown.put("gross_salary",   Math.round((basePay + overtimePay) * 100.0) / 100.0);
        return breakdown;
    }

    // Override toDict to also include the full-time-specific fields
    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("hours_per_day",  hoursPerDay);
        data.put("days_per_month", daysPerMonth);
        return data;
    }

    public int getHoursPerDay()  { return hoursPerDay; }
    public int getDaysPerMonth() { return daysPerMonth; }
}
