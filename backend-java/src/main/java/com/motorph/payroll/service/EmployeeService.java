package com.motorph.payroll.service;

import com.motorph.payroll.document.EmployeeDocument;
import com.motorph.payroll.dto.EmployeeCreateDto;
import com.motorph.payroll.dto.EmployeeUpdateDto;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.EmployeeFactory;
import com.motorph.payroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

/*
 * EmployeeService.java - Business logic layer for employee management
 *
 * This service handles all operations on employees: creating, reading,
 * updating, and deleting (CRUD). It acts as the bridge between the
 * controllers (which handle HTTP requests) and the repository (which
 * handles database access).
 *
 * The service also uses EmployeeFactory to create the correct Employee
 * subclass (FullTimeEmployee, PartTimeEmployee, or ContractEmployee)
 * to make sure business rules are applied correctly for each type.
 *
 * Author: Group 5 - MO-IT101
 */
@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmployeeFactory employeeFactory;

    /*
     * createEmployee() - Add a new employee to the system
     * Steps:
     * 1. Check that the employee ID isn't already taken
     * 2. Convert the DTO to a data map
     * 3. Use EmployeeFactory to create the right type (polymorphism)
     * 4. Save to the database
     * 5. Return the saved employee data
     */
    public Map<String, Object> createEmployee(EmployeeCreateDto dto) {
        // reject duplicate employee IDs
        employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .ifPresent(e -> { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID already exists"); });

        // build the data map with all required fields
        Map<String, Object> empData = dtoToMap(dto);
        empData.put("id",         UUID.randomUUID().toString());
        empData.put("status",     "active");
        empData.put("created_at", Instant.now().toString());
        empData.put("updated_at", Instant.now().toString());

        // factory creates the right subclass and validates the data
        Employee employee = employeeFactory.createEmployee(empData);
        Map<String, Object> finalData = employee.toDict();

        // convert and persist to database
        EmployeeDocument doc = mapToDocument(finalData);
        employeeRepository.save(doc);

        return documentToMap(doc);
    }

    /*
     * getEmployees() - Retrieve employees with optional filters
     * Can filter by: employee type, status, and/or department
     * If no filters are provided, all employees are returned
     */
    public List<Map<String, Object>> getEmployees(String employeeType, String status, String department) {
        List<EmployeeDocument> docs;

        // use the most specific query available to reduce database work
        if (employeeType != null && status != null) {
            docs = employeeRepository.findByEmployeeTypeAndStatus(employeeType, status);
        } else if (status != null && department != null) {
            docs = employeeRepository.findByDepartmentAndStatus(department, status);
        } else if (status != null) {
            docs = employeeRepository.findByStatus(status);
        } else {
            docs = employeeRepository.findAll();
        }

        // apply remaining filters in memory if needed
        if (department != null && status == null) {
            String dept = department;
            docs = docs.stream().filter(d -> dept.equals(d.getDepartment())).toList();
        }
        if (employeeType != null && status == null) {
            String et = employeeType;
            docs = docs.stream().filter(d -> et.equals(d.getEmployeeType())).toList();
        }

        // convert each document to a plain map before returning
        return docs.stream().map(this::documentToMap).toList();
    }

    // Get one employee by their internal UUID
    public Map<String, Object> getEmployee(String id) {
        return findByDocId(id);
    }

    /*
     * updateEmployee() - Patch an employee's details
     * Only updates fields that are not null in the DTO (partial update).
     * This means you can send just the fields you want to change.
     */
    public Map<String, Object> updateEmployee(String id, EmployeeUpdateDto dto) {
        EmployeeDocument doc = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        // only update fields that were actually sent in the request
        if (dto.getFirstName() != null) doc.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) doc.setLastName(dto.getLastName());
        if (dto.getEmail() != null) doc.setEmail(dto.getEmail());
        if (dto.getDepartment() != null) doc.setDepartment(dto.getDepartment());
        if (dto.getPosition() != null) doc.setPosition(dto.getPosition());
        if (dto.getStatus() != null) doc.setStatus(dto.getStatus());
        if (dto.getBasicSalary() != null) doc.setBasicSalary(dto.getBasicSalary());
        if (dto.getHourlyRate() != null) doc.setHourlyRate(dto.getHourlyRate());
        if (dto.getDailyRate() != null) doc.setDailyRate(dto.getDailyRate());
        if (dto.getHoursPerWeek() != null) doc.setHoursPerWeek(dto.getHoursPerWeek());
        if (dto.getContractEndDate() != null) doc.setContractEndDate(dto.getContractEndDate());
        doc.setUpdatedAt(Instant.now().toString());

        employeeRepository.save(doc);
        return documentToMap(doc);
    }

    // Remove an employee record permanently from the database
    public Map<String, Object> deleteEmployee(String id) {
        EmployeeDocument doc = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        employeeRepository.delete(doc);
        return Map.of("message", "Employee deleted successfully");
    }

    // Internal helper — find by UUID and throw 404 if not found
    private Map<String, Object> findByDocId(String id) {
        return employeeRepository.findById(id)
                .map(this::documentToMap)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    // Convert EmployeeCreateDto fields into a flat Map for the factory
    private Map<String, Object> dtoToMap(EmployeeCreateDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("employee_id",   dto.getEmployeeId());
        m.put("first_name",    dto.getFirstName());
        m.put("last_name",     dto.getLastName());
        m.put("email",         dto.getEmail());
        m.put("department",    dto.getDepartment());
        m.put("position",      dto.getPosition());
        m.put("employee_type", dto.getEmployeeType());
        m.put("date_hired",    dto.getDateHired());
        m.put("basic_salary",  dto.getBasicSalary());
        m.put("hourly_rate",   dto.getHourlyRate());
        m.put("daily_rate",    dto.getDailyRate());
        if (dto.getHoursPerWeek() != null)    m.put("hours_per_week",    dto.getHoursPerWeek());
        if (dto.getContractEndDate() != null) m.put("contract_end_date", dto.getContractEndDate());
        return m;
    }

    // Convert a plain Map to an EmployeeDocument (for saving to DB)
    private EmployeeDocument mapToDocument(Map<String, Object> data) {
        EmployeeDocument doc = new EmployeeDocument();
        doc.setId((String) data.get("id"));
        doc.setEmployeeId((String) data.get("employee_id"));
        doc.setFirstName((String) data.get("first_name"));
        doc.setLastName((String) data.get("last_name"));
        doc.setEmail((String) data.get("email"));
        doc.setDepartment((String) data.get("department"));
        doc.setPosition((String) data.get("position"));
        doc.setEmployeeType((String) data.get("employee_type"));
        doc.setDateHired((String) data.get("date_hired"));
        doc.setStatus((String) data.getOrDefault("status", "active"));
        doc.setBasicSalary(toDouble(data.getOrDefault("basic_salary", 0.0)));
        doc.setHourlyRate(toDouble(data.getOrDefault("hourly_rate", 0.0)));
        doc.setDailyRate(toDouble(data.getOrDefault("daily_rate", 0.0)));
        if (data.get("hours_per_week") != null) doc.setHoursPerWeek(toDouble(data.get("hours_per_week")));
        doc.setContractEndDate((String) data.get("contract_end_date"));
        doc.setCreatedAt((String) data.get("created_at"));
        doc.setUpdatedAt((String) data.get("updated_at"));
        return doc;
    }

    /*
     * documentToMap() - Convert EmployeeDocument to a Map for JSON response
     * Public so that other services (like PayrollService) can reuse this.
     */
    public Map<String, Object> documentToMap(EmployeeDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",                doc.getId());
        m.put("employee_id",       doc.getEmployeeId());
        m.put("first_name",        doc.getFirstName());
        m.put("last_name",         doc.getLastName());
        m.put("full_name",         doc.getFirstName() + " " + doc.getLastName());
        m.put("email",             doc.getEmail());
        m.put("department",        doc.getDepartment());
        m.put("position",          doc.getPosition());
        m.put("employee_type",     doc.getEmployeeType());
        m.put("date_hired",        doc.getDateHired());
        m.put("status",            doc.getStatus());
        m.put("basic_salary",      doc.getBasicSalary());
        m.put("hourly_rate",       doc.getHourlyRate());
        m.put("daily_rate",        doc.getDailyRate());
        m.put("hours_per_week",    doc.getHoursPerWeek());
        m.put("contract_end_date", doc.getContractEndDate());
        m.put("created_at",        doc.getCreatedAt());
        m.put("updated_at",        doc.getUpdatedAt());
        return m;
    }

    // Helper to safely convert any value to a double (handles null, Integer, String, etc.)
    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
