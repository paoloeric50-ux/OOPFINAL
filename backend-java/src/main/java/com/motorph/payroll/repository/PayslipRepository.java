package com.motorph.payroll.repository;

import com.motorph.payroll.document.PayslipDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayslipRepository extends MongoRepository<PayslipDocument, String> {

    @Query("{ 'id': ?0 }")
    Optional<PayslipDocument> findByPayslipId(String id);

    @Query("{ 'employee_id': ?0 }")
    List<PayslipDocument> findByEmployeeId(String employeeId);

    @Query("{ 'pay_period_start': ?0, 'pay_period_end': ?1 }")
    List<PayslipDocument> findByPayPeriod(String payPeriodStart, String payPeriodEnd);

    @Query("{ 'employee_id': ?0, 'pay_period_start': { $gte: ?1 } }")
    List<PayslipDocument> findByEmployeeIdAndStartDate(String employeeId, String startDate);
}
