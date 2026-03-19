package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * DATA CLASS: Payslip
 *
 * Holds all information for one employee's pay stub.
 */
public class Payslip {

    private String id;
    private String employeeId;
    private String employeeName;
    private String employeeType;
    private String department;
    private String position;
    private String payPeriodStart;
    private String payPeriodEnd;
    private int daysWorked;
    private double hoursWorked;
    private double basicPay;
    private double overtimeHours;
    private double overtimePay;
    private double grossPay;
    private double sss;
    private double philhealth;
    private double pagibig;
    private double withholdingTax;
    private double totalDeductions;
    private double netPay;
    private String status;
    private String generatedAt;

    public Payslip() {}

    public Map<String, Object> toDict() {
        Map<String, Object> earnings = new HashMap<>();
        earnings.put("basic_pay", basicPay);
        earnings.put("overtime_hours", overtimeHours);
        earnings.put("overtime_pay", overtimePay);
        earnings.put("gross_pay", grossPay);

        Map<String, Object> deductions = new HashMap<>();
        deductions.put("sss", sss);
        deductions.put("philhealth", philhealth);
        deductions.put("pagibig", pagibig);
        deductions.put("withholding_tax", withholdingTax);
        deductions.put("total", totalDeductions);

        Map<String, Object> result = new HashMap<>();
        result.put("id", id);
        result.put("employee_id", employeeId);
        result.put("employee_name", employeeName);
        result.put("employee_type", employeeType);
        result.put("department", department);
        result.put("position", position);
        result.put("pay_period_start", payPeriodStart);
        result.put("pay_period_end", payPeriodEnd);
        result.put("days_worked", daysWorked);
        result.put("hours_worked", hoursWorked);
        result.put("earnings", earnings);
        result.put("deductions", deductions);
        result.put("net_pay", netPay);
        result.put("status", status);
        result.put("generated_at", generatedAt);
        return result;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPayPeriodStart() { return payPeriodStart; }
    public void setPayPeriodStart(String s) { this.payPeriodStart = s; }
    public String getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(String s) { this.payPeriodEnd = s; }
    public int getDaysWorked() { return daysWorked; }
    public void setDaysWorked(int d) { this.daysWorked = d; }
    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double h) { this.hoursWorked = h; }
    public double getBasicPay() { return basicPay; }
    public void setBasicPay(double b) { this.basicPay = b; }
    public double getOvertimeHours() { return overtimeHours; }
    public void setOvertimeHours(double h) { this.overtimeHours = h; }
    public double getOvertimePay() { return overtimePay; }
    public void setOvertimePay(double p) { this.overtimePay = p; }
    public double getGrossPay() { return grossPay; }
    public void setGrossPay(double g) { this.grossPay = g; }
    public double getSss() { return sss; }
    public void setSss(double s) { this.sss = s; }
    public double getPhilhealth() { return philhealth; }
    public void setPhilhealth(double p) { this.philhealth = p; }
    public double getPagibig() { return pagibig; }
    public void setPagibig(double p) { this.pagibig = p; }
    public double getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(double w) { this.withholdingTax = w; }
    public double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(double t) { this.totalDeductions = t; }
    public double getNetPay() { return netPay; }
    public void setNetPay(double n) { this.netPay = n; }
    public String getStatus() { return status; }
    public void setStatus(String s) { this.status = s; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String g) { this.generatedAt = g; }
}
