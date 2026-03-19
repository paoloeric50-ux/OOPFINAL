package com.motorph.payroll.model;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SERVICE CLASS: Attendance Tracker
 *
 * OOP CONCEPTS DEMONSTRATED:
 * 1. SINGLE RESPONSIBILITY: Only handles attendance logic
 * 2. ENCAPSULATION: All attendance calculation logic is in one place
 */
@Component
public class AttendanceTracker {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public Map<String, Object> clockIn(String employeeId, String timestamp) {
        String now = timestamp != null ? timestamp : Instant.now().toString();
        String date = ZonedDateTime.parse(now, DateTimeFormatter.ISO_DATE_TIME)
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(DATE_FMT);

        Map<String, Object> record = new HashMap<>();
        record.put("id", UUID.randomUUID().toString());
        record.put("employee_id", employeeId);
        record.put("date", date);
        record.put("clock_in", now);
        record.put("clock_out", null);
        record.put("hours_worked", 0.0);
        record.put("status", "present");
        record.put("notes", "");
        return record;
    }

    public Map<String, Object> clockOut(Map<String, Object> record, String timestamp) {
        String now = timestamp != null ? timestamp : Instant.now().toString();

        String clockInStr = (String) record.get("clock_in");
        double hoursWorked = 0.0;

        try {
            Instant clockInTime = Instant.parse(clockInStr);
            Instant clockOutTime = Instant.parse(now);
            long minutes = ChronoUnit.MINUTES.between(clockInTime, clockOutTime);
            hoursWorked = Math.round((minutes / 60.0) * 100.0) / 100.0;
        } catch (Exception e) {
            hoursWorked = 0.0;
        }

        record.put("clock_out", now);
        record.put("hours_worked", hoursWorked);

        if (hoursWorked >= 8.0) {
            record.put("status", "present");
        } else if (hoursWorked >= 4.0) {
            record.put("status", "half_day");
        } else {
            record.put("status", "present");
        }

        return record;
    }

    public Map<String, Object> calculatePeriodSummary(List<Map<String, Object>> records) {
        int totalDays = records.size();
        int presentDays = 0, lateDays = 0, absentDays = 0, halfDays = 0;
        double totalHours = 0;

        for (Map<String, Object> r : records) {
            String status = (String) r.getOrDefault("status", "");
            Object hw = r.getOrDefault("hours_worked", 0.0);
            double hours = hw instanceof Number ? ((Number) hw).doubleValue() : 0.0;

            if ("present".equals(status) || "late".equals(status)) presentDays++;
            if ("late".equals(status)) lateDays++;
            if ("absent".equals(status)) absentDays++;
            if ("half_day".equals(status)) halfDays++;
            totalHours += hours;
        }

        double avgHours = presentDays > 0 ? totalHours / presentDays : 0;

        Map<String, Object> summary = new HashMap<>();
        summary.put("total_days", totalDays);
        summary.put("present_days", presentDays);
        summary.put("late_days", lateDays);
        summary.put("absent_days", absentDays);
        summary.put("half_days", halfDays);
        summary.put("total_hours_worked", Math.round(totalHours * 100.0) / 100.0);
        summary.put("average_hours_per_day", Math.round(avgHours * 100.0) / 100.0);
        return summary;
    }

    public Map<String, Object> getMonthlyAttendance(String employeeId, int year, int month,
                                                     List<Map<String, Object>> records) {
        String monthPrefix = String.format("%d-%02d", year, month);

        List<Map<String, Object>> filtered = records.stream()
                .filter(r -> employeeId.equals(r.get("employee_id")) &&
                        r.getOrDefault("date", "").toString().startsWith(monthPrefix))
                .toList();

        Map<String, Object> summary = calculatePeriodSummary(filtered);
        summary.put("employee_id", employeeId);
        summary.put("year", year);
        summary.put("month", month);
        return summary;
    }
}
