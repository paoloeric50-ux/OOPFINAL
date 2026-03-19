package com.motorph.payroll.service;

import com.motorph.payroll.document.EmployeeDocument;
import com.motorph.payroll.model.Employee;
import com.motorph.payroll.model.EmployeeFactory;
import com.motorph.payroll.repository.AttendanceRepository;
import com.motorph.payroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DashboardService {

    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private EmployeeFactory employeeFactory;
    @Autowired private EmployeeService employeeService;
    @Autowired private MongoTemplate mongoTemplate;

    public Map<String, Object> getDashboardStats() {
        long totalEmployees = employeeRepository.countByStatus("active");
        long fullTime = employeeRepository.countByStatusAndEmployeeType("active", "full_time");
        long partTime = employeeRepository.countByStatusAndEmployeeType("active", "part_time");
        long contract = employeeRepository.countByStatusAndEmployeeType("active", "contract");

        // Recent employees
        Query recentQuery = new Query(Criteria.where("status").is("active"))
                .with(Sort.by(Sort.Direction.DESC, "created_at"))
                .limit(5);
        List<Map<String, Object>> recentEmployees = mongoTemplate.find(recentQuery, EmployeeDocument.class)
                .stream().map(employeeService::documentToMap).toList();

        // Monthly payroll estimate
        List<EmployeeDocument> allActive = employeeRepository.findByStatus("active");
        double totalMonthlyPayroll = 0;
        for (EmployeeDocument doc : allActive) {
            try {
                Employee emp = employeeFactory.createEmployee(employeeService.documentToMap(doc));
                totalMonthlyPayroll += emp.computeSalary();
            } catch (Exception ignored) {}
        }

        // Today's attendance
        String today = Instant.now().atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        long todayAttendance = attendanceRepository.countByDate(today);

        Map<String, Object> employeeTypes = new HashMap<>();
        employeeTypes.put("full_time", fullTime);
        employeeTypes.put("part_time", partTime);
        employeeTypes.put("contract", contract);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total_employees", totalEmployees);
        stats.put("employee_types", employeeTypes);
        stats.put("monthly_payroll", Math.round(totalMonthlyPayroll * 100.0) / 100.0);
        stats.put("today_attendance", todayAttendance);
        stats.put("recent_employees", recentEmployees);
        return stats;
    }
}
