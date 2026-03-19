package com.motorph.payroll.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

@Document(collection = "payslips")
public class PayslipDocument {

    @Id
    private String mongoId;

    @Field("id")
    private String id;

    @Field("employee_id")
    private String employeeId;

    @Field("employee_name")
    private String employeeName;

    @Field("employee_type")
    private String employeeType;

    @Field("department")
    private String department;

    @Field("position")
    private String position;

    @Field("pay_period_start")
    private String payPeriodStart;

    @Field("pay_period_end")
    private String payPeriodEnd;

    @Field("days_worked")
    private int daysWorked;

    @Field("hours_worked")
    private double hoursWorked;

    @Field("earnings")
    private Map<String, Object> earnings;

    @Field("deductions")
    private Map<String, Object> deductions;

    @Field("net_pay")
    private double netPay;

    @Field("status")
    private String status;

    @Field("generated_at")
    private String generatedAt;

    public PayslipDocument() {}

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }
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
