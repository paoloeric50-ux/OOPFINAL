package com.motorph.payroll.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class OopController {

    @GetMapping("/")
    public Map<String, Object> root() {
        return Map.of("message", "MotorPH OOP Payroll System API", "version", "2.0.0", "language", "Java Spring Boot");
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "healthy", "timestamp", Instant.now().toString());
    }

    @GetMapping("/oop/class-hierarchy")
    public Map<String, Object> getClassHierarchy() {
        Map<String, Object> result = new HashMap<>();

        result.put("classes", List.of(
            Map.of(
                "name", "Employee",
                "type", "abstract_class",
                "access", "public",
                "description", "Parent abstract class for all employee types",
                "attributes", List.of(
                    Map.of("name", "id", "access", "protected", "type", "String"),
                    Map.of("name", "employeeId", "access", "protected", "type", "String"),
                    Map.of("name", "firstName", "access", "protected", "type", "String"),
                    Map.of("name", "lastName", "access", "protected", "type", "String"),
                    Map.of("name", "email", "access", "protected", "type", "String"),
                    Map.of("name", "department", "access", "protected", "type", "String"),
                    Map.of("name", "position", "access", "protected", "type", "String"),
                    Map.of("name", "basicSalary", "access", "private", "type", "double")
                ),
                "methods", List.of(
                    Map.of("name", "computeSalary()", "access", "public", "type", "abstract"),
                    Map.of("name", "getSalaryBreakdown()", "access", "public", "type", "abstract"),
                    Map.of("name", "toDict()", "access", "public", "type", "concrete"),
                    Map.of("name", "getFullName()", "access", "public", "type", "concrete")
                )
            ),
            Map.of(
                "name", "FullTimeEmployee",
                "type", "class",
                "extends", "Employee",
                "access", "public",
                "description", "Full-time employees with monthly salary",
                "attributes", List.of(
                    Map.of("name", "hoursPerDay", "access", "protected", "type", "int"),
                    Map.of("name", "daysPerMonth", "access", "protected", "type", "int")
                ),
                "methods", List.of(
                    Map.of("name", "computeSalary()", "access", "public", "type", "override",
                           "description", "Calculates monthly salary with overtime"),
                    Map.of("name", "getSalaryBreakdown()", "access", "public", "type", "override")
                )
            ),
            Map.of(
                "name", "PartTimeEmployee",
                "type", "class",
                "extends", "Employee",
                "access", "public",
                "description", "Part-time employees with hourly rate",
                "attributes", List.of(
                    Map.of("name", "hourlyRate", "access", "protected", "type", "double"),
                    Map.of("name", "hoursPerWeek", "access", "protected", "type", "double")
                ),
                "methods", List.of(
                    Map.of("name", "computeSalary()", "access", "public", "type", "override",
                           "description", "Calculates salary based on hours worked"),
                    Map.of("name", "getSalaryBreakdown()", "access", "public", "type", "override")
                )
            ),
            Map.of(
                "name", "ContractEmployee",
                "type", "class",
                "extends", "Employee",
                "access", "public",
                "description", "Contract employees with daily rate",
                "attributes", List.of(
                    Map.of("name", "dailyRate", "access", "protected", "type", "double"),
                    Map.of("name", "contractEndDate", "access", "protected", "type", "String")
                ),
                "methods", List.of(
                    Map.of("name", "computeSalary()", "access", "public", "type", "override",
                           "description", "Calculates salary based on days worked"),
                    Map.of("name", "getSalaryBreakdown()", "access", "public", "type", "override")
                )
            ),
            Map.of(
                "name", "PayrollProcessor",
                "type", "service_class",
                "access", "public",
                "description", "Central payroll processing orchestrator",
                "relationships", List.of(
                    Map.of("type", "composition", "target", "DeductionCalculator"),
                    Map.of("type", "composition", "target", "AttendanceTracker"),
                    Map.of("type", "uses", "target", "Employee")
                ),
                "methods", List.of(
                    Map.of("name", "processPayroll()", "access", "public", "type", "concrete"),
                    Map.of("name", "processBatchPayroll()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculateQuickEstimate()", "access", "public", "type", "concrete")
                )
            ),
            Map.of(
                "name", "DeductionCalculator",
                "type", "service_class",
                "access", "public",
                "description", "Handles all payroll deduction calculations",
                "methods", List.of(
                    Map.of("name", "calculateSss()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculatePhilhealth()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculatePagibig()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculateWithholdingTax()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculateAllDeductions()", "access", "public", "type", "concrete")
                )
            ),
            Map.of(
                "name", "AttendanceTracker",
                "type", "service_class",
                "access", "public",
                "description", "Employee attendance tracking service",
                "methods", List.of(
                    Map.of("name", "clockIn()", "access", "public", "type", "concrete"),
                    Map.of("name", "clockOut()", "access", "public", "type", "concrete"),
                    Map.of("name", "calculatePeriodSummary()", "access", "public", "type", "concrete")
                )
            ),
            Map.of(
                "name", "EmployeeFactory",
                "type", "factory_class",
                "access", "public",
                "description", "Factory Pattern - creates the correct Employee subclass",
                "methods", List.of(
                    Map.of("name", "createEmployee()", "access", "public", "type", "factory")
                )
            )
        ));

        result.put("relationships", List.of(
            Map.of("from", "FullTimeEmployee", "to", "Employee", "type", "inheritance"),
            Map.of("from", "PartTimeEmployee", "to", "Employee", "type", "inheritance"),
            Map.of("from", "ContractEmployee", "to", "Employee", "type", "inheritance"),
            Map.of("from", "PayrollProcessor", "to", "DeductionCalculator", "type", "composition"),
            Map.of("from", "PayrollProcessor", "to", "AttendanceTracker", "type", "composition"),
            Map.of("from", "PayrollProcessor", "to", "Employee", "type", "dependency")
        ));

        result.put("oop_concepts", Map.of(
            "inheritance", Map.of(
                "description", "FullTimeEmployee, PartTimeEmployee, ContractEmployee extend Employee",
                "example", "public class FullTimeEmployee extends Employee"
            ),
            "polymorphism", Map.of(
                "description", "computeSalary() method behaves differently based on employee type",
                "example", "Each subclass overrides computeSalary() with its own implementation"
            ),
            "encapsulation", Map.of(
                "description", "Private and protected attributes with getters/setters",
                "example", "private double basicSalary; accessed via getBasicSalary()/setBasicSalary()"
            ),
            "abstraction", Map.of(
                "description", "Employee is an abstract class with abstract methods",
                "example", "public abstract double computeSalary(double hoursWorked, int daysWorked)"
            ),
            "composition", Map.of(
                "description", "PayrollProcessor composes DeductionCalculator and AttendanceTracker",
                "example", "private final DeductionCalculator deductionCalculator"
            )
        ));

        return result;
    }
}
