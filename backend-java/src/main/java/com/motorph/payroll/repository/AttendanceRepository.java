package com.motorph.payroll.repository;

import com.motorph.payroll.document.AttendanceDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * AttendanceRepository.java - Data Access Object for attendance records
 *
 * Like EmployeeRepository, this extends JpaRepository for the basic CRUD operations.
 * For more complex queries (like "find by date range"), we write our own JPQL
 * using @Query.
 *
 * JPQL (Java Persistence Query Language) is similar to SQL but works with
 * Java class names and field names instead of table/column names.
 *
 * JPQL example:
 *   "SELECT a FROM AttendanceDocument a WHERE a.employeeId = ?1"
 *   → This refers to AttendanceDocument.employeeId, not the "employee_id" column directly
 *
 * Author: Group 5 - MO-IT101
 */
@Repository
public interface AttendanceRepository extends JpaRepository<AttendanceDocument, String> {

    // Get all attendance records for a specific date
    List<AttendanceDocument> findByDate(String date);

    // Get all attendance records for one employee (all time)
    List<AttendanceDocument> findByEmployeeId(String employeeId);

    /*
     * findActiveClockIn() - Find an open clock-in record (employee hasn't clocked out yet)
     * Used to check if an employee is currently clocked in before allowing clock-out
     */
    @Query("SELECT a FROM AttendanceDocument a WHERE a.employeeId = ?1 AND a.date = ?2 AND a.clockOut IS NULL")
    Optional<AttendanceDocument> findActiveClockIn(String employeeId, String date);

    /*
     * findByEmployeeIdAndDateRange() - Get attendance records for payroll processing
     * Returns all records for an employee between startDate and endDate (inclusive)
     */
    @Query("SELECT a FROM AttendanceDocument a WHERE a.employeeId = ?1 AND a.date >= ?2 AND a.date <= ?3")
    List<AttendanceDocument> findByEmployeeIdAndDateRange(String employeeId, String startDate, String endDate);

    // Get all attendance records (any employee) in a date range
    @Query("SELECT a FROM AttendanceDocument a WHERE a.date >= ?1 AND a.date <= ?2")
    List<AttendanceDocument> findByDateRange(String startDate, String endDate);

    // Find records for one employee where the date starts with a given prefix (e.g. "2024-01")
    // Used for monthly attendance views
    @Query("SELECT a FROM AttendanceDocument a WHERE a.employeeId = ?1 AND a.date LIKE ?2")
    List<AttendanceDocument> findByEmployeeIdAndDatePrefix(String employeeId, String prefix);

    // Count how many attendance records exist for a specific date (dashboard stats)
    long countByDate(String date);
}
