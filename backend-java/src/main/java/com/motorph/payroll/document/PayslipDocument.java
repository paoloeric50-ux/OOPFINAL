package com.motorph.payroll.document;

import com.motorph.payroll.config.MapJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Map;

/*
 * PayslipDocument.java - JPA Entity that maps to the "payslips" table
 *
 * Stores the result of a payroll run. Each row is one payslip for one employee
 * for one pay period.
 *
 * The earnings and deductions fields are stored as JSON strings in the DB,
 * and converted back to Map<String, Object> when loaded.
 * This is handled by MapJsonConverter.
 *
 * Example earnings JSON: {"basic_pay": 15000.0, "overtime_pay": 500.0, "gross_pay": 15500.0}
 * Example deductions JSON: {"sss": 450.0, "philhealth": 387.5, "pagibig": 100.0, ...}
 *
 * Author: Group 5 - MO-IT101
 */
@Entity
@Table(name = "payslips")
public class PayslipDocument {

    // Primary key — UUID generated at payroll processing time
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    // Which employee this payslip belongs to
    @Column(name = "employee_id")
    private String employeeId;

    @Column(name = "employee_name")
    private String employeeName;

    // "full_time", "part_time", or "contract"
    @Column(name = "employee_type")
    private String employeeType;

    @Column(name = "department")
    private String department;

    // Column renamed to avoid SQL keyword conflict
    @Column(name = "position_name")
    private String position;

    // The pay period this payslip covers (e.g. "2024-01-01" to "2024-01-15")
    @Column(name = "pay_period_start")
    private String payPeriodStart;

    @Column(name = "pay_period_end")
    private String payPeriodEnd;

    @Column(name = "days_worked")
    private int daysWorked;

    @Column(name = "hours_worked")
    private double hoursWorked;

    // Earnings breakdown stored as JSON (basic_pay, overtime_pay, gross_pay, etc.)
    // MapJsonConverter converts this Map to a JSON string for storage
    @Convert(converter = MapJsonConverter.class)
    @Column(name = "earnings_json", length = 4096)
    private Map<String, Object> earnings;

    // Deductions breakdown stored as JSON (sss, philhealth, pagibig, withholding_tax, total)
    @Convert(converter = MapJsonConverter.class)
    @Column(name = "deductions_json", length = 4096)
    private Map<String, Object> deductions;

    // Final take-home pay after all deductions
    @Column(name = "net_pay")
    private double netPay;

    // "generated" or "paid" — column renamed to avoid SQL keyword conflict
    @Column(name = "status_value")
    private String status;

    // When this payslip was created (ISO-8601 timestamp)
    @Column(name = "generated_at")
    private String generatedAt;

    // Default no-arg constructor required by JPA
    public PayslipDocument() {}

    // Standard getters and setters
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
    public void setPayPeriodStart(String payPeriodStart) { this.payPeriodStart = payPeriodStart; }
    public String getPayPeriodEnd() { return payPeriodEnd; }
    public void setPayPeriodEnd(String payPeriodEnd) { this.payPeriodEnd = payPeriodEnd; }
    public int getDaysWorked() { return daysWorked; }
    public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }
    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }
    public Map<String, Object> getEarnings() { return earnings; }
    public void setEarnings(Map<String, Object> earnings) { this.earnings = earnings; }
    public Map<String, Object> getDeductions() { return deductions; }
    public void setDeductions(Map<String, Object> deductions) { this.deductions = deductions; }
    public double getNetPay() { return netPay; }
    public void setNetPay(double netPay) { this.netPay = netPay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
}
