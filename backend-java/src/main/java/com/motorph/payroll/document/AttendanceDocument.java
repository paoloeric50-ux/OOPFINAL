package com.motorph.payroll.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * AttendanceDocument.java - JPA Entity that maps to the "attendance" table
 *
 * Each row is one clock-in/clock-out pair for one employee on one day.
 * When an employee clocks in, a new record is created with clock_out = null.
 * When they clock out, the same record is updated with the clock-out time
 * and hours_worked is calculated automatically.
 *
 * Author: Group 5 - MO-IT101
 */
@Entity
@Table(name = "attendance")
public class AttendanceDocument {

    // Primary key — UUID string
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    // Which employee this record belongs to
    @Column(name = "employee_id")
    private String employeeId;

    // Date of attendance in "yyyy-MM-dd" format
    // Named "date_value" to avoid SQL reserved word conflict
    @Column(name = "date_value")
    private String date;

    // Clock-in timestamp stored as ISO-8601 string (e.g. "2024-01-15T08:00:00Z")
    @Column(name = "clock_in")
    private String clockIn;

    // Clock-out timestamp — null means employee hasn't clocked out yet
    @Column(name = "clock_out")
    private String clockOut;

    // Hours worked = (clock_out - clock_in) in hours, rounded to 2 decimal places
    @Column(name = "hours_worked")
    private double hoursWorked;

    // "present", "half_day", "absent", or "late"
    // Named "status_value" to avoid SQL reserved word conflict
    @Column(name = "status_value")
    private String status;

    // Optional notes (e.g. reasons for being late)
    @Column(name = "notes")
    private String notes;

    // No-arg constructor required by JPA
    public AttendanceDocument() {}

    // Standard getters and setters
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
