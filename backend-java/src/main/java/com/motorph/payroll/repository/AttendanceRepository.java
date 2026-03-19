package com.motorph.payroll.repository;

import com.motorph.payroll.document.AttendanceDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends MongoRepository<AttendanceDocument, String> {

    @Query("{ 'id': ?0 }")
    Optional<AttendanceDocument> findByAttendanceId(String id);

    @Query("{ 'date': ?0 }")
    List<AttendanceDocument> findByDate(String date);

    @Query("{ 'employee_id': ?0 }")
    List<AttendanceDocument> findByEmployeeId(String employeeId);

    @Query("{ 'employee_id': ?0, 'date': ?1, 'clock_out': null }")
    Optional<AttendanceDocument> findActiveClockIn(String employeeId, String date);

    @Query("{ 'employee_id': ?0, 'date': { $gte: ?1, $lte: ?2 } }")
    List<AttendanceDocument> findByEmployeeIdAndDateRange(String employeeId, String startDate, String endDate);

    @Query("{ 'date': { $gte: ?0, $lte: ?1 } }")
    List<AttendanceDocument> findByDateRange(String startDate, String endDate);

    long countByDate(String date);
}
