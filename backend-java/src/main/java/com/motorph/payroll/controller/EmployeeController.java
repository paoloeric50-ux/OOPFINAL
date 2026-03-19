package com.motorph.payroll.controller;

import com.motorph.payroll.dto.EmployeeCreateDto;
import com.motorph.payroll.dto.EmployeeUpdateDto;
import com.motorph.payroll.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired private EmployeeService employeeService;

    @PostMapping
    public Map<String, Object> createEmployee(@RequestBody EmployeeCreateDto dto) {
        return employeeService.createEmployee(dto);
    }

    @GetMapping
    public List<Map<String, Object>> getEmployees(
            @RequestParam(required = false) String employeeType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        return employeeService.getEmployees(employeeType, status, department);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getEmployee(@PathVariable String id) {
        return employeeService.getEmployee(id);
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateEmployee(@PathVariable String id,
                                               @RequestBody EmployeeUpdateDto dto) {
        return employeeService.updateEmployee(id, dto);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteEmployee(@PathVariable String id) {
        return employeeService.deleteEmployee(id);
    }
}
