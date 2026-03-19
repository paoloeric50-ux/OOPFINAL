package com.motorph.payroll.model;

import org.springframework.stereotype.Component;

import java.util.Map;

/*
 * EmployeeFactory.java - Factory Pattern implementation
 *
 * This class creates the right type of Employee object based on
 * the "employee_type" field in the data.
 *
 * Why use a factory? So that the rest of the code doesn't need to
 * use "if full_time, create this... if part_time, create that..."
 * everywhere. We just call createEmployee() and it figures it out.
 *
 * This is the Factory design pattern - it hides the creation logic.
 *
 * Author: Group 5 - MO-IT101
 */
@Component
public class EmployeeFactory {

    // Creates and returns the correct Employee subclass based on type
    public Employee createEmployee(Map<String, Object> data) {
        String employeeType = (String) data.getOrDefault("employee_type", "full_time");

        // switch expression (Java 14+) - cleaner than multiple if-else
        return switch (employeeType) {
            case "full_time" -> new FullTimeEmployee(data);
            case "part_time" -> new PartTimeEmployee(data);
            case "contract"  -> new ContractEmployee(data);
            default -> throw new IllegalArgumentException("Unknown employee type: " + employeeType);
        };
    }
}
