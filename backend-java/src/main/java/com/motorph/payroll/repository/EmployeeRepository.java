package com.motorph.payroll.repository;

import com.motorph.payroll.document.EmployeeDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends MongoRepository<EmployeeDocument, String> {

    @Query("{ 'id': ?0 }")
    Optional<EmployeeDocument> findByEmployeeDocId(String id);

    @Query("{ 'employee_id': ?0 }")
    Optional<EmployeeDocument> findByEmployeeId(String employeeId);

    @Query("{ 'status': ?0 }")
    List<EmployeeDocument> findByStatus(String status);

    @Query("{ 'employee_type': ?0, 'status': ?1 }")
    List<EmployeeDocument> findByEmployeeTypeAndStatus(String employeeType, String status);

    @Query("{ 'department': ?0, 'status': ?1 }")
    List<EmployeeDocument> findByDepartmentAndStatus(String department, String status);

    long countByStatus(String status);

    @Query(value = "{ 'status': ?0, 'employee_type': ?1 }", count = true)
    long countByStatusAndEmployeeType(String status, String employeeType);
}
