package com.motorph.payroll.service;

import com.motorph.payroll.document.EmployeeDocument;
import com.motorph.payroll.dto.EmployeeCreateDto;
import com.motorph.payroll.dto.EmployeeUpdateDto;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.EmployeeFactory;
import com.motorph.payroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private EmployeeFactory employeeFactory;
    @Autowired private MongoTemplate mongoTemplate;

    public Map<String, Object> createEmployee(EmployeeCreateDto dto) {
        employeeRepository.findByEmployeeId(dto.getEmployeeId())
                .ifPresent(e -> { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee ID already exists"); });

        Map<String, Object> empData = dtoToMap(dto);
        empData.put("id", UUID.randomUUID().toString());
        empData.put("status", "active");
        empData.put("created_at", Instant.now().toString());
        empData.put("updated_at", Instant.now().toString());

        Employee employee = employeeFactory.createEmployee(empData);
        Map<String, Object> finalData = employee.toDict();

        EmployeeDocument doc = mapToDocument(finalData);
        employeeRepository.save(doc);

        return findByDocId(doc.getId());
    }

    public List<Map<String, Object>> getEmployees(String employeeType, String status, String department) {
        Query query = new Query();
        if (employeeType != null) query.addCriteria(Criteria.where("employee_type").is(employeeType));
        if (status != null) query.addCriteria(Criteria.where("status").is(status));
        if (department != null) query.addCriteria(Criteria.where("department").is(department));

        List<EmployeeDocument> docs = mongoTemplate.find(query, EmployeeDocument.class);
        return docs.stream().map(this::documentToMap).toList();
    }

    public Map<String, Object> getEmployee(String id) {
        return findByDocId(id);
    }

    public Map<String, Object> updateEmployee(String id, EmployeeUpdateDto dto) {
        EmployeeDocument doc = employeeRepository.findByEmployeeDocId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

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

    public Map<String, Object> deleteEmployee(String id) {
        EmployeeDocument doc = employeeRepository.findByEmployeeDocId(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        employeeRepository.delete(doc);
        return Map.of("message", "Employee deleted successfully");
    }

    private Map<String, Object> findByDocId(String id) {
        return employeeRepository.findByEmployeeDocId(id)
                .map(this::documentToMap)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private Map<String, Object> dtoToMap(EmployeeCreateDto dto) {
        Map<String, Object> m = new HashMap<>();
        m.put("employee_id", dto.getEmployeeId());
        m.put("first_name", dto.getFirstName());
        m.put("last_name", dto.getLastName());
        m.put("email", dto.getEmail());
        m.put("department", dto.getDepartment());
        m.put("position", dto.getPosition());
        m.put("employee_type", dto.getEmployeeType());
        m.put("date_hired", dto.getDateHired());
        m.put("basic_salary", dto.getBasicSalary());
        m.put("hourly_rate", dto.getHourlyRate());
        m.put("daily_rate", dto.getDailyRate());
        if (dto.getHoursPerWeek() != null) m.put("hours_per_week", dto.getHoursPerWeek());
        if (dto.getContractEndDate() != null) m.put("contract_end_date", dto.getContractEndDate());
        return m;
    }

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

    public Map<String, Object> documentToMap(EmployeeDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", doc.getId());
        m.put("employee_id", doc.getEmployeeId());
        m.put("first_name", doc.getFirstName());
        m.put("last_name", doc.getLastName());
        m.put("full_name", doc.getFirstName() + " " + doc.getLastName());
        m.put("email", doc.getEmail());
        m.put("department", doc.getDepartment());
        m.put("position", doc.getPosition());
        m.put("employee_type", doc.getEmployeeType());
        m.put("date_hired", doc.getDateHired());
        m.put("status", doc.getStatus());
        m.put("basic_salary", doc.getBasicSalary());
        m.put("hourly_rate", doc.getHourlyRate());
        m.put("daily_rate", doc.getDailyRate());
        m.put("hours_per_week", doc.getHoursPerWeek());
        m.put("contract_end_date", doc.getContractEndDate());
        m.put("created_at", doc.getCreatedAt());
        m.put("updated_at", doc.getUpdatedAt());
        return m;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
