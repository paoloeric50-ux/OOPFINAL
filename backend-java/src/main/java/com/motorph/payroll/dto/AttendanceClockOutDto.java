package com.motorph.payroll.dto;

public class AttendanceClockOutDto {
    private String recordId;
    private String timestamp;

    public String getRecordId() { return recordId; }
    public void setRecordId(String recordId) { this.recordId = recordId; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
