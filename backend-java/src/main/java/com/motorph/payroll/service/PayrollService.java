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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/*
 * PayrollService.java - Business logic layer for payroll processing
 *
 * This service coordinates the steps needed to run payroll:
 * 1. Fetch the list of employees to process
 * 2. Fetch attendance records for the pay period
 * 3. Hand everything to PayrollProcessor (which uses our OOP model classes)
 * 4. Save the generated payslips to the database
 * 5. Return the results
 *
 * It also handles payslip retrieval and payroll summary reports.
 *
 * Author: Group 5 - MO-IT101
 */
@Service
public class PayrollService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private PayslipRepository payslipRepository;
    @Autowired private PayrollProcessor payrollProcessor;
    @Autowired private EmployeeService employeeService;

    /*
     * processPayroll() - Main method to run payroll for a pay period
     * If specific employee IDs are given, only process those.
     * Otherwise, process all active employees.
     */
    public List<Map<String, Object>> processPayroll(PayrollProcessRequestDto request) {
        List<EmployeeDocument> employeeDocs;

        if (request.getEmployeeIds() != null && !request.getEmployeeIds().isEmpty()) {
            // process only the specified employees
            employeeDocs = employeeRepository.findByStatusAndIdIn("active", request.getEmployeeIds());
        } else {
            // process all active employees
            employeeDocs = employeeRepository.findByStatus("active");
        }

        if (employeeDocs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active employees found");
        }

        // convert documents to Maps so the model classes can use them
        List<Map<String, Object>> employees = employeeDocs.stream()
                .map(employeeService::documentToMap).toList();

        // fetch attendance records for each employee for this pay period
        Map<String, List<Map<String, Object>>> allAttendance = new HashMap<>();
        for (EmployeeDocument emp : employeeDocs) {
            List<Map<String, Object>> records = attendanceRepository
                    .findByEmployeeIdAndDateRange(emp.getId(),
                            request.getPayPeriodStart(), request.getPayPeriodEnd())
                    .stream().map(this::attendanceToMap).toList();
            allAttendance.put(emp.getId(), records);
        }

        // run the payroll using our OOP model classes (PayrollProcessor → Employee subclasses)
        List<Payslip> payslips = payrollProcessor.processBatchPayroll(
                employees, allAttendance, request.getPayPeriodStart(), request.getPayPeriodEnd());

        // save each payslip to the database and collect results to return
        List<Map<String, Object>> result = new ArrayList<>();
        for (Payslip ps : payslips) {
            Map<String, Object> psDict = ps.toDict();
            PayslipDocument doc = payslipToDocument(ps);
            payslipRepository.save(doc);
            result.add(psDict);
        }
        return result;
    }

    /*
     * getPayslips() - Retrieve payslips with optional filters
     * Can filter by employee ID and/or pay period date range
     */
    public List<Map<String, Object>> getPayslips(String employeeId, String payPeriodStart, String payPeriodEnd) {
        List<PayslipDocument> docs;

        if (employeeId != null) {
            docs = payslipRepository.findByEmployeeId(employeeId);
        } else {
            docs = payslipRepository.findAll();
        }

        // filter by pay period dates in memory if provided
        if (payPeriodStart != null) {
            String ps = payPeriodStart;
            docs = docs.stream()
                    .filter(d -> d.getPayPeriodStart() != null && d.getPayPeriodStart().compareTo(ps) >= 0)
                    .toList();
        }
        if (payPeriodEnd != null) {
            String pe = payPeriodEnd;
            docs = docs.stream()
                    .filter(d -> d.getPayPeriodEnd() != null && d.getPayPeriodEnd().compareTo(pe) <= 0)
                    .toList();
        }

        return docs.stream().map(this::payslipDocToMap).toList();
    }

    // Get a single payslip by its UUID
    public Map<String, Object> getPayslip(String id) {
        return payslipRepository.findById(id)
                .map(this::payslipDocToMap)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payslip not found"));
    }

    // Quick payroll estimate for one employee (no attendance data)
    public Map<String, Object> estimatePayroll(String employeeId) {
        EmployeeDocument emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
        return payrollProcessor.calculateQuickEstimate(employeeService.documentToMap(emp));
    }

    /*
     * getPayrollSummary() - Get totals and breakdowns for a completed pay period
     * Fetches payslips for the period and runs them through PayrollProcessor.getPayrollSummary()
     */
    public Map<String, Object> getPayrollSummary(String payPeriodStart, String payPeriodEnd) {
        List<PayslipDocument> docs = payslipRepository.findByPayPeriodStartAndPayPeriodEnd(payPeriodStart, payPeriodEnd);

        // re-hydrate Payslip model objects from the database documents
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

            // extract earnings and deductions from the stored JSON maps
            Map<String, Object> earnings   = doc.getEarnings()   != null ? doc.getEarnings()   : new HashMap<>();
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

    // Convert Payslip model object → PayslipDocument for saving to DB
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

    // Convert PayslipDocument → Map for JSON response
    private Map<String, Object> payslipDocToMap(PayslipDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",               doc.getId());
        m.put("employee_id",      doc.getEmployeeId());
        m.put("employee_name",    doc.getEmployeeName());
        m.put("employee_type",    doc.getEmployeeType());
        m.put("department",       doc.getDepartment());
        m.put("position",         doc.getPosition());
        m.put("pay_period_start", doc.getPayPeriodStart());
        m.put("pay_period_end",   doc.getPayPeriodEnd());
        m.put("days_worked",      doc.getDaysWorked());
        m.put("hours_worked",     doc.getHoursWorked());
        m.put("earnings",         doc.getEarnings());
        m.put("deductions",       doc.getDeductions());
        m.put("net_pay",          doc.getNetPay());
        m.put("status",           doc.getStatus());
        m.put("generated_at",     doc.getGeneratedAt());
        return m;
    }

    // Convert AttendanceDocument → Map (used when passing records to the model classes)
    private Map<String, Object> attendanceToMap(AttendanceDocument doc) {
        Map<String, Object> m = new HashMap<>();
        m.put("id",           doc.getId());
        m.put("employee_id",  doc.getEmployeeId());
        m.put("date",         doc.getDate());
        m.put("clock_in",     doc.getClockIn());
        m.put("clock_out",    doc.getClockOut());
        m.put("hours_worked", doc.getHoursWorked());
        m.put("status",       doc.getStatus());
        return m;
    }

    // Helper to safely convert any value to double
    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
