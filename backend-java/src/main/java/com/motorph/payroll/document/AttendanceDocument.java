package com.motorph.payroll.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "attendance")
public class AttendanceDocument {

    @Id
    private String mongoId;

    @Field("id")
    private String id;

    @Field("employee_id")
    private String employeeId;

    @Field("date")
    private String date;

    @Field("clock_in")
    private String clockIn;

    @Field("clock_out")
    private String clockOut;

    @Field("hours_worked")
    private double hoursWorked;

    @Field("status")
    private String status;

    @Field("notes")
    private String notes;

    public AttendanceDocument() {}

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getClockIn() { return clockIn; }
    public void setClockIn(String clockIn) { this.clockIn = clockIn; }
    public String getClockOut() { return clockOut; }
    public void setClockOut(String clockOut) { this.clockOut = clockOut; }
    public double getHoursWorked() { return hoursWorked; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
