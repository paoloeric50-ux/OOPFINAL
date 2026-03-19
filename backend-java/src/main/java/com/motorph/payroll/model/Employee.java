package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/*
 * Employee.java - Abstract base class for all employees
 *
 * This is the parent class that all employee types inherit from.
 * It uses all four OOP concepts:
 * - Abstraction: can't make an Employee directly, must use a subclass
 * - Encapsulation: basicSalary is private, use getter/setter to access it
 * - Inheritance: FullTime, PartTime, Contract all extend this
 * - Polymorphism: computeSalary() is abstract so each subclass does it differently
 *
 * Author: Group 5 - MO-IT101
 */
public abstract class Employee {

    // protected = subclasses can use these but outside classes can't directly
    protected String id;
    protected String employeeId;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String department;
    protected String position;
    protected String employeeType;  // "full-time", "part-time", or "contract"
    protected String dateHired;
    protected String status;        // "active" or "inactive"
    protected String createdAt;
    protected String updatedAt;

    // private = only this class can touch it directly (encapsulation in action)
    private double basicSalary;

    // Constructor - takes a Map of data from the database
    public Employee(Map<String, Object> data) {
        this.id           = (String) data.getOrDefault("id", java.util.UUID.randomUUID().toString());
        this.employeeId   = (String) data.get("employee_id");
        this.firstName    = (String) data.get("first_name");
        this.lastName     = (String) data.get("last_name");
        this.email        = (String) data.get("email");
        this.department   = (String) data.get("department");
        this.position     = (String) data.get("position");
        this.employeeType = (String) data.get("employee_type");
        this.dateHired    = (String) data.get("date_hired");
        this.status       = (String) data.getOrDefault("status", "active");
        this.basicSalary  = toDouble(data.getOrDefault("basic_salary", 0.0));
        this.createdAt    = (String) data.getOrDefault("created_at", java.time.Instant.now().toString());
        this.updatedAt    = (String) data.getOrDefault("updated_at", java.time.Instant.now().toString());
    }

    // Getter for basicSalary - we don't expose the field directly
    public double getBasicSalary() {
        return basicSalary;
    }

    // Setter with validation - salary can't be negative
    public void setBasicSalary(double salary) {
        if (salary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.basicSalary = salary;
    }

    // Regular getters for all the other fields
    public String getId()           { return id; }
    public String getEmployeeId()   { return employeeId; }
    public String getFirstName()    { return firstName; }
    public String getLastName()     { return lastName; }
    public String getEmail()        { return email; }
    public String getDepartment()   { return department; }
    public String getPosition()     { return position; }
    public String getEmployeeType() { return employeeType; }
    public String getDateHired()    { return dateHired; }
    public String getStatus()       { return status; }
    public void   setStatus(String status)       { this.status = status; }
    public String getCreatedAt()    { return createdAt; }
    public String getUpdatedAt()    { return updatedAt; }
    public void   setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    // Helper to get the full name in one go
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ABSTRACT METHODS - every subclass MUST implement these (that's the whole point)
    // This is polymorphism - same method name, different behavior depending on the type
    public abstract double computeSalary(double hoursWorked, int daysWorked);
    public abstract Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked);

    // Overloaded version - if no attendance data, just use zeros
    public double computeSalary() {
        return computeSalary(0, 0);
    }

    // Convert this employee object to a Map so we can return it as JSON
    public Map<String, Object> toDict() {
        Map<String, Object> data = new HashMap<>();
        data.put("id",            id);
        data.put("employee_id",   employeeId);
        data.put("first_name",    firstName);
        data.put("last_name",     lastName);
        data.put("full_name",     getFullName());
        data.put("email",         email);
        data.put("department",    department);
        data.put("position",      position);
        data.put("employee_type", employeeType);
        data.put("date_hired",    dateHired);
        data.put("status",        status);
        data.put("basic_salary",  basicSalary);
        data.put("created_at",    createdAt);
        data.put("updated_at",    updatedAt);
        return data;
    }

    // Helper to safely convert any value to double (handles nulls and strings)
    protected static double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    // Helper to safely convert any value to int
    protected static int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }
}
