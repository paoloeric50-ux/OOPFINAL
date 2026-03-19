package com.motorph.payroll.controller;

import com.motorph.payroll.dto.PayrollProcessRequestDto;
import com.motorph.payroll.service.PayrollService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payroll")
public class PayrollController {

    @Autowired private PayrollService payrollService;

    @PostMapping("/process")
    public List<Map<String, Object>> processPayroll(@RequestBody PayrollProcessRequestDto request) {
        return payrollService.processPayroll(request);
    }

    @GetMapping("/payslips")
    public List<Map<String, Object>> getPayslips(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String payPeriodStart,
            @RequestParam(required = false) String payPeriodEnd) {
        return payrollService.getPayslips(employeeId, payPeriodStart, payPeriodEnd);
    }

    @GetMapping("/payslip/{id}")
    public Map<String, Object> getPayslip(@PathVariable String id) {
        return payrollService.getPayslip(id);
    }

    @PostMapping("/estimate")
    public Map<String, Object> estimatePayroll(@RequestParam String employeeId) {
        return payrollService.estimatePayroll(employeeId);
    }

    @GetMapping("/summary")
    public Map<String, Object> getPayrollSummary(
            @RequestParam String payPeriodStart,
            @RequestParam String payPeriodEnd) {
        return payrollService.getPayrollSummary(payPeriodStart, payPeriodEnd);
    }
}
