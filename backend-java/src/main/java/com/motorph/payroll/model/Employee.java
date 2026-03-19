package com.motorph.payroll.model;

import java.util.HashMap;
import java.util.Map;

/**
 * ABSTRACT BASE CLASS - EMPLOYEE
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. ABSTRACTION: Employee is abstract - cannot instantiate directly
 * 2. ENCAPSULATION: Private/protected attributes with getters/setters
 * 3. INHERITANCE: Subclasses extend this class
 * 4. POLYMORPHISM: Abstract methods implemented differently in each subclass
 */
public abstract class Employee {

    // PROTECTED attributes - accessible by subclasses
    protected String id;
    protected String employeeId;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected String department;
    protected String position;
    protected String employeeType;
    protected String dateHired;
    protected String status;
    protected String createdAt;
    protected String updatedAt;

    // PRIVATE attribute - only accessible through getter/setter
    private double basicSalary;

    public Employee(Map<String, Object> data) {
        this.id = (String) data.getOrDefault("id", java.util.UUID.randomUUID().toString());
        this.employeeId = (String) data.get("employee_id");
        this.firstName = (String) data.get("first_name");
        this.lastName = (String) data.get("last_name");
        this.email = (String) data.get("email");
        this.department = (String) data.get("department");
        this.position = (String) data.get("position");
        this.employeeType = (String) data.get("employee_type");
        this.dateHired = (String) data.get("date_hired");
        this.status = (String) data.getOrDefault("status", "active");
        this.basicSalary = toDouble(data.getOrDefault("basic_salary", 0.0));
        this.createdAt = (String) data.getOrDefault("created_at", java.time.Instant.now().toString());
        this.updatedAt = (String) data.getOrDefault("updated_at", java.time.Instant.now().toString());
    }

    // ENCAPSULATION: Controlled access to private basicSalary
    public double getBasicSalary() {
        return basicSalary;
    }

    public void setBasicSalary(double salary) {
        if (salary < 0) throw new IllegalArgumentException("Salary cannot be negative");
        this.basicSalary = salary;
    }

    // GETTERS for protected attributes
    public String getId() { return id; }
    public String getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    public String getEmployeeType() { return employeeType; }
    public String getDateHired() { return dateHired; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // ABSTRACT METHODS - Must be implemented by all subclasses (POLYMORPHISM)
    public abstract double computeSalary(double hoursWorked, int daysWorked);

    public abstract Map<String, Object> getSalaryBreakdown(double hoursWorked, int daysWorked);

    // Default computeSalary (no attendance data)
    public double computeSalary() {
        return computeSalary(0, 0);
    }

    public Map<String, Object> toDict() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("employee_id", employeeId);
        data.put("first_name", firstName);
        data.put("last_name", lastName);
        data.put("full_name", getFullName());
        data.put("email", email);
        data.put("department", department);
        data.put("position", position);
        data.put("employee_type", employeeType);
        data.put("date_hired", dateHired);
        data.put("status", status);
        data.put("basic_salary", basicSalary);
        data.put("created_at", createdAt);
        data.put("updated_at", updatedAt);
        return data;
    }

    protected static double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }

    protected static int toInt(Object val) {
        if (val == null) return 0;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return 0; }
    }
}
