package com.motorph.payroll.model;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * FACTORY PATTERN
 *
 * Creates the appropriate Employee subclass based on employee_type.
 * Callers don't need to know which concrete class to instantiate.
 */
@Component
public class EmployeeFactory {

    public Employee createEmployee(Map<String, Object> data) {
        String employeeType = (String) data.getOrDefault("employee_type", "full_time");

        return switch (employeeType) {
            case "full_time" -> new FullTimeEmployee(data);
            case "part_time" -> new PartTimeEmployee(data);
            case "contract" -> new ContractEmployee(data);
            default -> throw new IllegalArgumentException("Unknown employee type: " + employeeType);
        };
    }
}
