package com.motorph.payroll.dto;

public class AttendanceClockInDto {
    private String employeeId;
    private String timestamp;

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
