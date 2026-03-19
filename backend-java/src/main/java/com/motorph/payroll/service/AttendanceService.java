package com.motorph.payroll.service;

import com.motorph.payroll.document.AttendanceDocument;
import com.motorph.payroll.dto.AttendanceClockInDto;
import com.motorph.payroll.dto.AttendanceClockOutDto;
import com.motorph.payroll.model.AttendanceTracker;
import com.motorph.payroll.repository.AttendanceRepository;
import com.motorph.payroll.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceService {

    @Autowired private AttendanceRepository attendanceRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private AttendanceTracker attendanceTracker;

    public Map<String, Object> clockIn(AttendanceClockInDto dto) {
        employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));

        String today = Instant.now().atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        attendanceRepository.findActiveClockIn(dto.getEmployeeId(), today)
                .ifPresent(r -> { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already clocked in for today"); });

        Map<String, Object> record = attendanceTracker.clockIn(dto.getEmployeeId(), dto.getTimestamp());
        AttendanceDocument doc = mapToDocument(record);
        attendanceRepository.save(doc);
        return documentToMap(doc);
    }

    public Map<String, Object> clockOut(AttendanceClockOutDto dto) {
        AttendanceDocument doc = attendanceRepository.findById(dto.getRecordId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance record not found"));

        if (doc.getClockOut() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already clocked out");
        }

        Map<String, Object> record = documentToMap(doc);
        Map<String, Object> updated = attendanceTracker.clockOut(record, dto.getTimestamp());

        doc.setClockOut((String) updated.get("clock_out"));
        doc.setHoursWorked(toDouble(updated.getOrDefault("hours_worked", 0.0)));
        doc.setStatus((String) updated.get("status"));
        attendanceRepository.save(doc);
        return documentToMap(doc);
    }

    public List<Map<String, Object>> getAttendance(String employeeId, String date,
                                                    String startDate, String endDate) {
        List<AttendanceDocument> docs;

        if (employeeId != null && date != null) {
            docs = attendanceRepository.findByEmployeeId(employeeId).stream()
                    .filter(a -> date.equals(a.getDate())).toList();
        } else if (employeeId != null && startDate != null && endDate != null) {
            docs = attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate);
        } else if (employeeId != null) {
            docs = attendanceRepository.findByEmployeeId(employeeId);
        } else if (date != null) {
            docs = attendanceRepository.findByDate(date);
        } else if (startDate != null && endDate != null) {
            docs = attendanceRepository.findByDateRange(startDate, endDate);
        } else {
            docs = attendanceRepository.findAll();
        }

        return docs.stream().map(this::documentToMap).toList();
    }

    public List<Map<String, Object>> getTodayAttendance() {
        String today = Instant.now().atZone(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return attendanceRepository.findByDate(today).stream().map(this::documentToMap).toList();
    }

    public Map<String, Object> getAttendanceSummary(String employeeId, int year, int month) {
        String monthPrefix = String.format("%d-%02d", year, month);
        List<Map<String, Object>> records = attendanceRepository
                .findByEmployeeIdAndDatePrefix(employeeId, monthPrefix + "%")
                .stream().map(this::documentToMap).toList();
        return attendanceTracker.getMonthlyAttendance(employeeId, year, month, records);
    }

    private AttendanceDocument mapToDocument(Map<String, Object> data) {
        AttendanceDocument doc = new AttendanceDocument();
        doc.setId((String) data.get("id"));
        doc.setEmployeeId((String) data.get("employee_id"));
        doc.setDate((String) data.get("date"));
        doc.setClockIn((String) data.get("clock_in"));
        doc.setClockOut((String) data.get("clock_out"));
        doc.setHoursWorked(toDouble(data.getOrDefault("hours_worked", 0.0)));
        doc.setStatus((String) data.get("status"));
        doc.setNotes((String) data.getOrDefault("notes", ""));
        return doc;
    }

    public Map<String, Object> documentToMap(AttendanceDocument doc) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", doc.getId());
        m.put("employee_id", doc.getEmployeeId());
        m.put("date", doc.getDate());
        m.put("clock_in", doc.getClockIn());
        m.put("clock_out", doc.getClockOut());
        m.put("hours_worked", doc.getHoursWorked());
        m.put("status", doc.getStatus());
        m.put("notes", doc.getNotes());
        return m;
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        if (val instanceof Number) return ((Number) val).doubleValue();
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return 0.0; }
    }
}
