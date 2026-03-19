package com.motorph.payroll.model;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/*
 * DeductionCalculator.java - Handles all salary deductions
 *
 * This class computes the 4 mandatory Philippine deductions:
 *   1. SSS - Social Security System
 *   2. PhilHealth - health insurance
 *   3. Pag-IBIG (HDMF) - housing fund
 *   4. Withholding Tax - based on TRAIN Law (2024)
 *
 * I put all the tax tables and formulas here so that the rest
 * of the code doesn't have to worry about the math.
 *
 * Author: Group 5 - MO-IT101
 */
@Component
public class DeductionCalculator {

    /*
     * SSS Contribution Table (2024 rates)
     * Format: { salary ceiling, employee contribution }
     * You look up the row where gross salary <= ceiling, then use that contribution amount.
     */
    private static final double[][] SSS_TABLE = {
        {4250,  180.00},  {4750,  202.50},  {5250,  225.00},  {5750,  247.50},
        {6250,  270.00},  {6750,  292.50},  {7250,  315.00},  {7750,  337.50},
        {8250,  360.00},  {8750,  382.50},  {9250,  405.00},  {9750,  427.50},
        {10250, 450.00},  {10750, 472.50},  {11250, 495.00},  {11750, 517.50},
        {12250, 540.00},  {12750, 562.50},  {13250, 585.00},  {13750, 607.50},
        {14250, 630.00},  {14750, 652.50},  {15250, 675.00},  {15750, 697.50},
        {16250, 720.00},  {16750, 742.50},  {17250, 765.00},  {17750, 787.50},
        {18250, 810.00},  {18750, 832.50},  {19250, 855.00},  {19750, 877.50},
        {20250, 900.00},  {20750, 922.50},  {21250, 945.00},  {21750, 967.50},
        {22250, 990.00},  {22750, 1012.50}, {23250, 1035.00}, {23750, 1057.50},
        {24250, 1080.00}, {24750, 1102.50}, {25250, 1125.00}, {25750, 1147.50},
        {26250, 1170.00}, {26750, 1192.50}, {27250, 1215.00}, {27750, 1237.50},
        {28250, 1260.00}, {28750, 1282.50}, {29250, 1305.00},
        {Double.MAX_VALUE, 1350.00}  // highest bracket, max SSS is 1350
    };

    // Look up SSS contribution from the table above
    public double calculateSss(double grossSalary) {
        for (double[] bracket : SSS_TABLE) {
            if (grossSalary <= bracket[0]) {
                return bracket[1];  // return the contribution for that bracket
            }
        }
        return 1350.00;  // fallback to max if nothing matched
    }

    /*
     * PhilHealth = 5% of gross salary, employee pays half (2.5%)
     * Min employee share: 250  (when salary is below 10,000)
     * Max employee share: 2,500 (when salary is above 100,000)
     */
    public double calculatePhilhealth(double grossSalary) {
        double premium       = grossSalary * 0.05;
        double employeeShare = premium / 2.0;
        return Math.min(2500.00, Math.max(250.00, employeeShare));
    }

    /*
     * Pag-IBIG is capped at PHP 100 per month
     * Rate is 1% if salary <= 1500, otherwise 2%
     */
    public double calculatePagibig(double grossSalary) {
        double rate = grossSalary <= 1500 ? 0.01 : 0.02;
        return Math.min(100.00, grossSalary * rate);
    }

    /*
     * Withholding Tax - based on TRAIN Law (Republic Act 10963)
     * We annualize the monthly salary first, then compute tax, then divide by 12
     * Salaries up to PHP 250,000/year are TAX FREE
     */
    public double calculateWithholdingTax(double grossSalary) {
        double annualSalary = grossSalary * 12;

        if (annualSalary <= 250_000) {
            return 0.0;  // no tax for low earners
        } else if (annualSalary <= 400_000) {
            return ((annualSalary - 250_000) * 0.15) / 12;
        } else if (annualSalary <= 800_000) {
            return (22_500 + (annualSalary - 400_000) * 0.20) / 12;
        } else if (annualSalary <= 2_000_000) {
            return (102_500 + (annualSalary - 800_000) * 0.25) / 12;
        } else if (annualSalary <= 8_000_000) {
            return (402_500 + (annualSalary - 2_000_000) * 0.30) / 12;
        } else {
            return (2_202_500 + (annualSalary - 8_000_000) * 0.35) / 12;
        }
    }

    // Compute all 4 deductions at once and return them together
    public Map<String, Object> calculateAllDeductions(double grossSalary) {
        double sss            = calculateSss(grossSalary);
        double philhealth     = calculatePhilhealth(grossSalary);
        double pagibig        = calculatePagibig(grossSalary);
        double withholdingTax = calculateWithholdingTax(grossSalary);
        double totalDeductions = sss + philhealth + pagibig + withholdingTax;
        double netSalary       = grossSalary - totalDeductions;

        // put all deductions into a nested map
        Map<String, Object> deductionsMap = new HashMap<>();
        deductionsMap.put("sss",             round2(sss));
        deductionsMap.put("philhealth",       round2(philhealth));
        deductionsMap.put("pagibig",          round2(pagibig));
        deductionsMap.put("withholding_tax",  round2(withholdingTax));
        deductionsMap.put("total",            round2(totalDeductions));

        Map<String, Object> result = new HashMap<>();
        result.put("gross_salary", round2(grossSalary));
        result.put("deductions",   deductionsMap);
        result.put("net_salary",   round2(netSalary));
        return result;
    }

    // Helper to round to 2 decimal places (for pesos and centavos)
    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
