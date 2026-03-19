package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * SUBCLASS 2: PART-TIME EMPLOYEE
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. INHERITANCE: extends Employee
 * 2. POLYMORPHISM: computeSalary() calculates based on HOURLY RATE
 */
public class PartTimeEmployee extends Employee {

    protected double hourlyRate;
    protected double hoursPerWeek;

    public PartTimeEmployee(Map<String, Object> data) {
        super(data);
        this.hourlyRate = toDouble(data.getOrDefault("hourly_rate", 0.0));
        this.hoursPerWeek = toDouble(data.getOrDefault("hours_per_week", 20.0));
    }

    /**
     * POLYMORPHISM: Hourly-rate-based salary calculation
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        if (hoursWorked > 0) {
            return hourlyRate * hoursWorked;
        }
        // Default: expected monthly hours
        double monthlyHours = hoursPerWeek * 4;
        return hourlyRate * monthlyHours;
    }

    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        double effectiveHours = hoursWorked > 0 ? hoursWorked : hoursPerWeek * 4;
        double totalPay = hourlyRate * effectiveHours;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type", "part_time");
        breakdown.put("hourly_rate", Math.round(hourlyRate * 100.0) / 100.0);
        breakdown.put("hours_per_week", hoursPerWeek);
        breakdown.put("hours_worked", effectiveHours);
        breakdown.put("days_worked", daysWorked);
        breakdown.put("gross_salary", Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("base_pay", Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("overtime_hours", 0);
        breakdown.put("overtime_pay", 0);
        return breakdown;
    }

    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("hourly_rate", hourlyRate);
        data.put("hours_per_week", hoursPerWeek);
        return data;
    }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Hourly rate cannot be negative");
        this.hourlyRate = rate;
    }
    public double getHoursPerWeek() { return hoursPerWeek; }
}
