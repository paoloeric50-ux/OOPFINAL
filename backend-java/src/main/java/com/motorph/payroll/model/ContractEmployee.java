package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/*
 * ContractEmployee.java - Subclass for contract/project-based employees
 *
 * Contract employees are paid per day worked, not monthly or hourly.
 * They also have a contract end date.
 *
 * Author: Group 5 - MO-IT101
 */
public class ContractEmployee extends Employee {

    protected double dailyRate;         // how much they earn per day
    protected String contractEndDate;   // when the contract expires

    public ContractEmployee(Map<String, Object> data) {
        super(data);
        this.dailyRate       = toDouble(data.getOrDefault("daily_rate", 0.0));
        this.contractEndDate = (String) data.get("contract_end_date");
    }

    /*
     * computeSalary() - POLYMORPHISM
     * Contract employees just multiply daily rate by days worked.
     * If no days given, assume a standard 22-day month.
     */
    @Override
    public double computeSalary(double hoursWorked, int daysWorked) {
        if (daysWorked > 0) {
            return dailyRate * daysWorked;
        }
        // default to 22 working days if no attendance data
        return dailyRate * 22;
    }

    /*
     * getSalaryBreakdown() - returns detailed info for the payslip
     */
    @Override
    public Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked) {
        int    effectiveDays = daysWorked > 0 ? daysWorked : 22;
        double totalPay      = dailyRate * effectiveDays;

        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("employee_type",  "contract");
        breakdown.put("daily_rate",     Math.round(dailyRate * 100.0) / 100.0);
        breakdown.put("days_worked",    effectiveDays);
        breakdown.put("hours_worked",   hoursWorked);
        breakdown.put("gross_salary",   Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("base_pay",       Math.round(totalPay * 100.0) / 100.0);
        breakdown.put("overtime_hours", 0);  // contract doesn't track OT separately
        breakdown.put("overtime_pay",   0);

        // include contract end date if it exists
        if (contractEndDate != null) {
            breakdown.put("contract_end_date", contractEndDate);
        }
        return breakdown;
    }

    // Include contract-specific fields in the dict
    @Override
    public Map<String, Object> toDict() {
        Map<String, Object> data = super.toDict();
        data.put("daily_rate",        dailyRate);
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
