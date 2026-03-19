package com.motorph.payroll.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/*
 * EmployeeDocument.java - JPA Entity that maps to the "employees" table in the database
 *
 * This class is different from the Employee model class in the model/ folder.
 * - Employee (model)         → OOP class used for business logic and salary computation
 * - EmployeeDocument (this)  → Database entity class used only for saving/loading data
 *
 * The separation is on purpose: we don't want to mix database concerns with
 * business logic — that's called the "separation of concerns" principle.
 *
 * Notes on column naming:
 * - "position_name" instead of "position" — "position" is a reserved SQL keyword
 * - "status_value" instead of "status" — "status" is a reserved SQL keyword in H2
 *
 * Author: Group 5 - MO-IT101
 */
@Entity
@Table(name = "employees")
public class EmployeeDocument {

    // Primary key — stored as a UUID string (e.g. "a1b2c3d4-...")
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private String id;

    // The employee's visible ID (e.g. "EMP-001")
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "department")
    private String department;

    // Column renamed to avoid SQL keyword conflict
    @Column(name = "position_name")
    private String position;

    // "full_time", "part_time", or "contract"
    @Column(name = "employee_type")
    private String employeeType;

    @Column(name = "date_hired")
    private String dateHired;

    // "active" or "inactive" — column renamed to avoid SQL keyword conflict
    @Column(name = "status_value")
    private String status;

    // Monthly salary — used for full-time employees
    @Column(name = "basic_salary")
    private double basicSalary;

    // Hourly rate — used for part-time employees
    @Column(name = "hourly_rate")
    private double hourlyRate;

    // Daily rate — used for contract employees
    @Column(name = "daily_rate")
    private double dailyRate;

    // Expected hours per week — for part-time employees
    @Column(name = "hours_per_week")
    private Double hoursPerWeek;

    // When the contract ends — for contract employees only
    @Column(name = "contract_end_date")
    private String contractEndDate;

    // Work schedule fields for full-time employees
    @Column(name = "hours_per_day")
    private double hoursPerDay;

    @Column(name = "days_per_month")
    private double daysPerMonth;

    // Timestamps stored as ISO-8601 strings
    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    // Default no-arg constructor required by JPA
    public EmployeeDocument() {}

    // Standard getters and setters below
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getEmployeeType() { return employeeType; }
    public void setEmployeeType(String employeeType) { this.employeeType = employeeType; }
    public String getDateHired() { return dateHired; }
    public void setDateHired(String dateHired) { this.dateHired = dateHired; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    public Double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Double hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public String getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(String contractEndDate) { this.contractEndDate = contractEndDate; }
    public double getHoursPerDay() { return hoursPerDay; }
    public void setHoursPerDay(double hoursPerDay) { this.hoursPerDay = hoursPerDay; }
    public double getDaysPerMonth() { return daysPerMonth; }
    public void setDaysPerMonth(double daysPerMonth) { this.daysPerMonth = daysPerMonth; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
