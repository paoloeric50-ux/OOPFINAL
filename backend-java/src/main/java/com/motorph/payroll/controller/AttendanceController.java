package com.motorph.payroll.controller;

import com.motorph.payroll.dto.AttendanceClockInDto;
import com.motorph.payroll.dto.AttendanceClockOutDto;
import com.motorph.payroll.service.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired private AttendanceService attendanceService;

    @PostMapping("/clock-in")
    public Map<String, Object> clockIn(@RequestBody AttendanceClockInDto dto) {
        return attendanceService.clockIn(dto);
    }

    @PostMapping("/clock-out")
    public Map<String, Object> clockOut(@RequestBody AttendanceClockOutDto dto) {
        return attendanceService.clockOut(dto);
    }

    @GetMapping
    public List<Map<String, Object>> getAttendance(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return attendanceService.getAttendance(employeeId, date, startDate, endDate);
    }

    @GetMapping("/today")
    public List<Map<String, Object>> getTodayAttendance() {
        return attendanceService.getTodayAttendance();
    }

    @GetMapping("/summary/{employeeId}")
    public Map<String, Object> getAttendanceSummary(
            @PathVariable String employeeId,
            @RequestParam int year,
            @RequestParam int month) {
        return attendanceService.getAttendanceSummary(employeeId, year, month);
    }
}
