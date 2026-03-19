package com.motorph.payroll.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * PayrollProcessor.java - The main payroll coordinator
 *
 * This class uses COMPOSITION to bring together:
 *   - DeductionCalculator (handles tax/deduction math)
 *   - AttendanceTracker (calculates hours worked)
 *   - EmployeeFactory (creates the right employee type)
 *
 * It shows POLYMORPHISM by calling employee.computeSalary() which
 * does different things depending on if it's FullTime, PartTime, or Contract.
 *
 * This is the "orchestrator" - it coordinates all the parts to process payroll.
 *
 * Author: Group 5 - MO-IT101
 */
@Component
public class PayrollProcessor {

    // COMPOSITION: we have-a DeductionCalculator (not extend)
    private final DeductionCalculator deductionCalculator;
    private final AttendanceTracker attendanceTracker;
    private final EmployeeFactory employeeFactory;

    @Autowired
    public PayrollProcessor(DeductionCalculator deductionCalculator,
                            AttendanceTracker attendanceTracker,
                            EmployeeFactory employeeFactory) {
        this.deductionCalculator   = deductionCalculator;
        this.attendanceTracker     = attendanceTracker;
        this.employeeFactory       = employeeFactory;
    }

    /*
     * processPayroll() - Process one employee's payroll
     *
     * Steps:
     * 1. Create the right employee type (polymorphism)
     * 2. Get attendance summary for the pay period
     * 3. Compute salary based on employee type (polymorphism again)
     * 4. Calculate all deductions
     * 5. Create a payslip with all the details
     */
    public Payslip processPayroll(Map<String, Object> employeeData,
                                  List<Map<String, Object>> attendanceRecords,
                                  String payPeriodStart, String payPeriodEnd) {

        // POLYMORPHISM: Factory creates the right subclass
        Employee employee = employeeFactory.createEmployee(employeeData);

        // Get how many days/hours the employee worked this period
        Map<String, Object> attendance = attendanceTracker.calculatePeriodSummary(attendanceRecords);
        int    daysWorked  = (int)    attendance.getOrDefault("present_days", 0);
        double hoursWorked = toDouble(attendance.getOrDefault("total_hours_worked", 0.0));

        // POLYMORPHISM: Each employee type calculates salary differently
        Map<String, Object> breakdown = employee.getSalaryBreakdown(hoursWorked, daysWorked);

        double basicPay     = toDouble(breakdown.getOrDefault("base_pay", 0.0));
        double overtimeHours = toDouble(breakdown.getOrDefault("overtime_hours", 0.0));
        double overtimePay  = toDouble(breakdown.getOrDefault("overtime_pay", 0.0));
        double grossPay     = toDouble(breakdown.getOrDefault("gross_salary", basicPay + overtimePay));

        // Get all deductions
        Map<String, Object> deductionResult = deductionCalculator.calculateAllDeductions(grossPay);
        @SuppressWarnings("unchecked")
        Map<String, Object> deductions = (Map<String, Object>) deductionResult.get("deductions");

        double sss            = toDouble(deductions.getOrDefault("sss", 0.0));
        double philhealth     = toDouble(deductions.getOrDefault("philhealth", 0.0));
        double pagibig        = toDouble(deductions.getOrDefault("pagibig", 0.0));
        double withholdingTax = toDouble(deductions.getOrDefault("withholding_tax", 0.0));
        double totalDeductions = toDouble(deductions.getOrDefault("total", 0.0));
        double netPay         = grossPay - totalDeductions;

        // Build the payslip object with all the details
        Payslip payslip = new Payslip();
        payslip.setId(UUID.randomUUID().toString());
        payslip.setEmployeeId(employee.getId());
        payslip.setEmployeeName(employee.getFullName());
        payslip.setEmployeeType(employee.getEmployeeType());
        payslip.setDepartment(employee.getDepartment());
        payslip.setPosition(employee.getPosition());
        payslip.setPayPeriodStart(payPeriodStart);
        payslip.setPayPeriodEnd(payPeriodEnd);
        payslip.setDaysWorked(daysWorked);
        payslip.setHoursWorked(hoursWorked);
        payslip.setBasicPay(round2(basicPay));
        payslip.setOvertimeHours(overtimeHours);
        payslip.setOvertimePay(round2(overtimePay));
        payslip.setGrossPay(round2(grossPay));
        payslip.setSss(round2(sss));
        payslip.setPhilhealth(round2(philhealth));
        payslip.setPagibig(round2(pagibig));
        payslip.setWithholdingTax(round2(withholdingTax));
        payslip.setTotalDeductions(round2(totalDeductions));
        payslip.setNetPay(round2(netPay));
        payslip.setStatus("generated");
        payslip.setGeneratedAt(Instant.now().toString());
        return payslip;
    }

    /*
     * processBatchPayroll() - Process payroll for multiple employees at once
     * Calls processPayroll() for each employee and collects all payslips
     */
    public List<Payslip> processBatchPayroll(List<Map<String, Object>> employees,
                                              Map<String, List<Map<String, Object>>> allAttendance,
                                              String payPeriodStart, String payPeriodEnd) {
        List<Payslip> payslips = new ArrayList<>();
        for (Map<String, Object> emp : employees) {
            try {
                String empId = (String) emp.get("id");
                List<Map<String, Object>> attendance = allAttendance.getOrDefault(empId, List.of());
                Payslip payslip = processPayroll(emp, attendance, payPeriodStart, payPeriodEnd);
                payslips.add(payslip);
            } catch (Exception e) {
                // If something goes wrong for one employee, skip and continue with the rest
            }
        }
        return payslips;
    }

    // Quick estimate of what an employee would earn (no attendance data)
    public Map<String, Object> calculateQuickEstimate(Map<String, Object> employeeData) {
        Employee employee = employeeFactory.createEmployee(employeeData);
        Map<String, Object> breakdown = employee.getSalaryBreakdown(0, 0);
        double grossPay = toDouble(breakdown.getOrDefault("gross_salary", 0.0));
        Map<String, Object> deductionResult = deductionCalculator.calculateAllDeductions(grossPay);

        Map<String, Object> result = new HashMap<>(breakdown);
        result.putAll(deductionResult);
        result.put("employee_id",   employee.getId());
        result.put("employee_name", employee.getFullName());
        return result;
    }

    // Summarize payroll run - total gross, deductions, net across all employees
    public Map<String, Object> getPayrollSummary(List<Payslip> payslips) {
        double totalGross = 0, totalDeductions = 0, totalNet = 0;
        Map<String, Map<String, Object>> byType = new HashMap<>();

        for (Payslip p : payslips) {
            totalGross       += p.getGrossPay();
            totalDeductions  += p.getTotalDeductions();
            totalNet         += p.getNetPay();

            // Group by employee type
            String type = p.getEmployeeType();
            byType.computeIfAbsent(type, k -> {
                Map<String, Object> m = new HashMap<>();
                m.put("count",       0);
                m.put("total_gross", 0.0);
                m.put("total_net",   0.0);
                return m;
            });
            Map<String, Object> typeData = byType.get(type);
            typeData.put("count",       (int) typeData.get("count") + 1);
            typeData.put("total_gross", round2((double) typeData.get("total_gross") + p.getGrossPay()));
            typeData.put("total_net",   round2((double) typeData.get("total_net") + p.getNetPay()));
        }

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_employees",   payslips.size());
        summary.put("total_gross",       round2(totalGross));
        summary.put("total_deductions",  round2(totalDeductions));
        summary.put("total_net",         round2(totalNet));
        summary.put("by_type",           byType);
        return summary;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
