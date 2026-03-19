package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/*
 * PartTimeEmployee.java - Subclass for part-time employees
 *
 * Part-time employees are paid by the hour, not by monthly salary.
 * This class overrides computeSalary() to use hourly rate instead.
 *
 * Author: Group 5 - MO-IT101
 */
public class PartTimeEmployee extends Employee {

    // These two fields are specific to part-time employees only
    protected double hourlyRate;    // how much they earn per hour
    protected double hoursPerWeek;  // expected hours per week

    public PartTimeEmployee(Map<String, Object> data) {
        super(data);
        // get hourly rate from data, default to 0 if not set
        this.hourlyRate   = toDouble(data.getOrDefault("hourly_rate", 0.0));
        this.hoursPerWeek = toDouble(data.getOrDefault("hours_per_week", 20.0));
    }

    /*
     * computeSalary() - POLYMORPHISM
     * For part-time, it's just hourly rate multiplied by hours worked.
     * If no actual hours given, estimate based on expected weekly hours * 4 weeks.
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        if (hoursWorked > 0) {
            return hourlyRate * hoursWorked;
        }
        // estimate: assume 4 weeks in a month
        double monthlyHours = hoursPerWeek * 4;
        return hourlyRate * monthlyHours;
    }

    /*
     * getSalaryBreakdown() - returns salary details for the payslip
     */
    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        // use actual hours if provided, otherwise estimate
        double effectiveHours = hoursWorked > 0 ? hoursWorked : hoursPerWeek * 4;
        double totalPay       = hourlyRate * effectiveHours;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type",  "part_time");
        breakdown.put("hourly_rate",    Math.round(hourlyRate * 100.0) / 100.0);
        breakdown.put("hours_per_week", hoursPerWeek);
        breakdown.put("hours_worked",   effectiveHours);
        breakdown.put("days_worked",    daysWorked);
        breakdown.put("gross_salary",   Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("base_pay",       Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("overtime_hours", 0);  // part-time doesn't have overtime
        breakdown.put("overtime_pay",   0);
        return breakdown;
    }

    // Override toDict to include part-time fields
    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("hourly_rate",   hourlyRate);
        data.put("hours_per_week", hoursPerWeek);
        return data;
    }

    public double getHourlyRate() { return hourlyRate; }

    // Setter with validation so you can't set a negative rate
    public void setHourlyRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Hourly rate cannot be negative");
        this.hourlyRate = rate;
    }

    public double getHoursPerWeek() { return hoursPerWeek; }
}
