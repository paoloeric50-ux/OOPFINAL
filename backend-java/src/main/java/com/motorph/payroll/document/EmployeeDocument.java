package com.motorph.payroll.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "employees")
public class EmployeeDocument {

    @Id
    private String mongoId;

    @Field("id")
    private String id;

    @Indexed(unique = true)
    @Field("employee_id")
    private String employeeId;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("email")
    private String email;

    @Field("department")
    private String department;

    @Field("position")
    private String position;

    @Field("employee_type")
    private String employeeType;

    @Field("date_hired")
    private String dateHired;

    @Field("status")
    private String status;

    @Field("basic_salary")
    private double basicSalary;

    @Field("hourly_rate")
    private double hourlyRate;

    @Field("daily_rate")
    private double dailyRate;

    @Field("hours_per_week")
    private Double hoursPerWeek;

    @Field("contract_end_date")
    private String contractEndDate;

    @Field("hours_per_day")
    private double hoursPerDay;

    @Field("days_per_month")
    private double daysPerMonth;

    @Field("created_at")
    private String createdAt;

    @Field("updated_at")
    private String updatedAt;

    public EmployeeDocument() {}

    public String getMongoId() { return mongoId; }
    public void setMongoId(String mongoId) { this.mongoId = mongoId; }
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
