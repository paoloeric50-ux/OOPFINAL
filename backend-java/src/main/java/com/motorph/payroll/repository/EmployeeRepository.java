package com.motorph.payroll.repository;

import com.motorph.payroll.document.EmployeeDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
 * EmployeeRepository.java - Data Access Object (DAO) for employees
 *
 * This interface extends JpaRepository, which is part of Spring Data JPA.
 * Just by declaring it, Spring automatically generates the SQL queries for us.
 * We don't need to write any SQL — Spring reads the method names and figures
 * out what query to run.
 *
 * Examples of how Spring translates method names to SQL:
 *   findByStatus("active")      → SELECT * FROM employees WHERE status_value = 'active'
 *   countByStatus("active")     → SELECT COUNT(*) FROM employees WHERE status_value = 'active'
 *
 * The JpaRepository<EmployeeDocument, String> means:
 *   - EmployeeDocument is the entity class (maps to the employees table)
 *   - String is the type of the primary key (our UUIDs are Strings)
 *
 * We also get these methods for free from JpaRepository:
 *   save(), findById(), findAll(), deleteById(), count(), etc.
 *
 * Author: Group 5 - MO-IT101
 */
@Repository
public interface EmployeeRepository extends JpaRepository<EmployeeDocument, String> {

    // Find by visible employee ID (e.g. "EMP-001")
    Optional<EmployeeDocument> findByEmployeeId(String employeeId);

    // Get all employees with a given status ("active" or "inactive")
    List<EmployeeDocument> findByStatus(String status);

    // Filter by both employee type and status
    List<EmployeeDocument> findByEmployeeTypeAndStatus(String employeeType, String status);

    // Filter by both department and status
    List<EmployeeDocument> findByDepartmentAndStatus(String department, String status);

    // Get active employees filtered to only the given list of IDs
    // Used when processing payroll for a specific set of employees
    List<EmployeeDocument> findByStatusAndIdIn(String status, List<String> ids);

    // Get the 5 most recently added active employees (for the dashboard)
    List<EmployeeDocument> findTop5ByStatusOrderByCreatedAtDesc(String status);

    // Count queries — used for dashboard statistics
    long countByStatus(String status);
    long countByStatusAndEmployeeType(String status, String employeeType);
}
