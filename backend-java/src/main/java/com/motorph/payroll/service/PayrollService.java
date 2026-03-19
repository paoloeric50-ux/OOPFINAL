package com.motorph.payroll.service;

import com.motorph.payroll.document.AttendanceDocument;
import com.motorph.payroll.document.EmployeeDocument;
import com.motorph.payroll.document.PayslipDocument;
import com.motorph.payroll.dto.PayrollProcessRequestDto;
import com.motorph.payroll.model.Payslip;
import com.motorph.payroll.model.PayrollProcessor;
import com.motorph.payroll.repository.AttendanceRepository;
import com.motorph.payroll.repository.EmployeeRepository;
import com.motorph.payroll.repository.PayslipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class PayrollService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PayslipRepository payslipRepository;
    @Autowired private PayrollProcessor payrollProcessor;
    @Autowired private EmployeeService employeeService;
    @Autowired private MongoTemplate mongoTemplate;

    public List<Map<String, Object>> processPayroll(PayrollProcessRequestDto request) {
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("active"));
        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            query.addCriteria(Criteria.where("id").in(request.getEmployeeIds()));
        }

        List<EmployeeDocument> employeeDocs = mongoTemplate.find(query, EmployeeDocument.class);
        if (employeeDocs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active employees found");
        }

        List<Map<String, Object>> employees = employeeDocs.stream()
                .map(employeeService::documentToMap).toList();

        Map<String, List<Map<String, Object>>> allAttendance = new HashMap<>();
        for (EmployeeDocument emp : employeeDocs) {
            List<Map<String, Object>> records = attendanceRepository
                    .findByEmployeeIdAndDateRange(emp.getId(),
                            request.getPayPeriodStart(), request.getPayPeriodEnd())
                    .stream().map(this::attendanceToMap).toList();
            allAttendance.put(emp.getId(), records);
        }

        List<Payslip> payslips = payrollProcessor.processBatchPayroll(
                employees, allAttendance, request.getPayPeriodStart(), request.getPayPeriodEnd());

        List<Map<String, Object>> result = new ArrayList<>();
        for (Payslip ps : payslips) {
            Map<String, Object> psDict = ps.toDict();
            PayslipDocument doc = payslipToDocument(ps);
            payslipRepository.save(doc);
            result.add(psDict);
        }
        return result;
    }

    public List<Map<String, Object>> getPayslips(String employeeId, String payPeriodStart, String payPeriodEnd) {
        Query query = new Query();
        if (employeeId != null) query.addCriteria(Criteria.where("employee_id").is(employeeId));
        if (payPeriodStart != null) query.addCriteria(Criteria.where("pay_period_start").gte(payPeriodStart));
        if (payPeriodEnd != null) query.addCriteria(Criteria.where("pay_period_end").lte(payPeriodEnd));

        return mongoTemplate.find(query, PayslipDocument.class)
                .stream().map(this::payslipDocToMap).toList();
    }

    public Map<String, Object> getPayslip(String id) {
        return payslipRepository.findByPayslipId(id)
                .map(this::payslipDocToMap)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payslip not found"));
    }

    public Map<String, Object> estimatePayroll(String employeeId) {
        EmployeeDocument emp = employeeRepository.findByEmployeeDocId(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        return payrollProcessor.calculateQuickEstimate(employeeService.documentToMap(emp));
    }

    public Map<String, Object> getPayrollSummary(String payPeriodStart, String payPeriodEnd) {
        List<PayslipDocument> docs = payslipRepository.findByPayPeriod(payPeriodStart, payPeriodEnd);

        List<Payslip> payslips = docs.stream().map(doc -> {
            Payslip ps = new Payslip();
            ps.setId(doc.getId());
            ps.setEmployeeId(doc.getEmployeeId());
            ps.setEmployeeName(doc.getEmployeeName());
            ps.setEmployeeType(doc.getEmployeeType());
            ps.setDepartment(doc.getDepartment());
            ps.setPosition(doc.getPosition());
            ps.setPayPeriodStart(doc.getPayPeriodStart());
            ps.setPayPeriodEnd(doc.getPayPeriodEnd());
            ps.setDaysWorked(doc.getDaysWorked());
            ps.setHoursWorked(doc.getHoursWorked());
            Map<String, Object> earnings = doc.getEarnings() != null ? doc.getEarnings() : new HashMap<>();
            Map<String, Object> deductions = doc.getDeductions() != null ? doc.getDeductions() : new HashMap<>();
            ps.setBasicPay(toDouble(earnings.getOrDefault("basic_pay", 0.0)));
            ps.setOvertimeHours(toDouble(earnings.getOrDefault("overtime_hours", 0.0)));
            ps.setOvertimePay(toDouble(earnings.getOrDefault("overtime_pay", 0.0)));
            ps.setGrossPay(toDouble(earnings.getOrDefault("gross_pay", 0.0)));
            ps.setSss(toDouble(deductions.getOrDefault("sss", 0.0)));
            ps.setPhilhealth(toDouble(deductions.getOrDefault("philhealth", 0.0)));
            ps.setPagibig(toDouble(deductions.getOrDefault("pagibig", 0.0)));
            ps.setWithholdingTax(toDouble(deductions.getOrDefault("withholding_tax", 0.0)));
            ps.setTotalDeductions(toDouble(deductions.getOrDefault("total", 0.0)));
            ps.setNetPay(doc.getNetPay());
            ps.setStatus(doc.getStatus());
            ps.setGeneratedAt(doc.getGeneratedAt());
            return ps;
        }).toList();

        return payrollProcessor.getPayrollSummary(payslips);
    }

    private PayslipDocument payslipToDocument(Payslip ps) {
        Map<String, Object> psDict = ps.toDict();
        PayslipDocument doc = new PayslipDocument();
        doc.setId(ps.getId());
        doc.setEmployeeId(ps.getEmployeeId());
        doc.setEmployeeName(ps.getEmployeeName());
        doc.setEmployeeType(ps.getEmployeeType());
        doc.setDepartment(ps.getDepartment());
        doc.setPosition(ps.getPosition());
        doc.setPayPeriodStart(ps.getPayPeriodStart());
        doc.setPayPeriodEnd(ps.getPayPeriodEnd());
        doc.setDaysWorked(ps.getDaysWorked());
        doc.setHoursWorked(ps.getHoursWorked());
        @SuppressWarnings("unchecked")
        Map<String, Object> earnings = (Map<String, Object>) psDict.get("earnings");
        @SuppressWarnings("unchecked")
        Map<String, Object> deductions = (Map<String, Object>) psDict.get("deductions");
        doc.setEarnings(earnings);
        doc.setDeductions(deductions);
        doc.setNetPay(ps.getNetPay());
        doc.setStatus(ps.getStatus());
        doc.setGeneratedAt(ps.getGeneratedAt());
        return doc;
    }

    private Map<String, Object> payslipDocToMap(PayslipDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", doc.getId());
        m.put("employee_id", doc.getEmployeeId());
        m.put("employee_name", doc.getEmployeeName());
        m.put("employee_type", doc.getEmployeeType());
        m.put("department", doc.getDepartment());
        m.put("position", doc.getPosition());
        m.put("pay_period_start", doc.getPayPeriodStart());
        m.put("pay_period_end", doc.getPayPeriodEnd());
        m.put("days_worked", doc.getDaysWorked());
        m.put("hours_worked", doc.getHoursWorked());
        m.put("earnings", doc.getEarnings());
        m.put("deductions", doc.getDeductions());
        m.put("net_pay", doc.getNetPay());
        m.put("status", doc.getStatus());
        m.put("generated_at", doc.getGeneratedAt());
        return m;
    }

    private Map<String, Object> attendanceToMap(AttendanceDocument doc) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", doc.getId());
        m.put("employee_id", doc.getEmployeeId());
        m.put("date", doc.getDate());
        m.put("clock_in", doc.getClockIn());
        m.put("clock_out", doc.getClockOut());
        m.put("hours_worked", doc.getHoursWorked());
        m.put("status", doc.getStatus());
        return m;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
