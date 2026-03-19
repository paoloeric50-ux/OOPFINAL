package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * SUBCLASS 3: CONTRACT EMPLOYEE
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. INHERITANCE: extends Employee
 * 2. POLYMORPHISM: computeSalary() calculates based on DAILY RATE
 */
public class ContractEmployee extends Employee {

    protected double dailyRate;
    protected String contractEndDate;

    public ContractEmployee(Map<String, Object> data) {
        super(data);
        this.dailyRate = toDouble(data.getOrDefault("daily_rate", 0.0));
        this.contractEndDate = (String) data.get("contract_end_date");
    }

    /**
     * POLYMORPHISM: Daily-rate-based salary calculation
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        if (daysWorked > 0) {
            return dailyRate * daysWorked;
        }
        // Default: 22 working days
        return dailyRate * 22;
    }

    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        int effectiveDays = daysWorked > 0 ? daysWorked : 22;
        double totalPay = dailyRate * effectiveDays;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type", "contract");
        breakdown.put("daily_rate", Math.round(dailyRate * 100.0) / 100.0);
        breakdown.put("days_worked", effectiveDays);
        breakdown.put("hours_worked", hoursWorked);
        breakdown.put("gross_salary", Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("base_pay", Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("overtime_hours", 0);
        breakdown.put("overtime_pay", 0);
        if (contractEndDate != null) {
            breakdown.put("contract_end_date", contractEndDate);
        }
        return breakdown;
    }

    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("daily_rate", dailyRate);
        data.put("contract_end_date", contractEndDate);
        return data;
    }

    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double rate) {
        if (rate < 0) throw new IllegalArgumentException("Daily rate cannot be negative");
        this.dailyRate = rate;
    }
    public String getContractEndDate() { return contractEndDate; }
}
