package com.motorph.payroll.repository;

import com.motorph.payroll.document.PayslipDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PayslipRepository extends JpaRepository<PayslipDocument, String> {

    List<PayslipDocument> findByEmployeeId(String employeeId);

    List<PayslipDocument> findByPayPeriodStartAndPayPeriodEnd(String payPeriodStart, String payPeriodEnd);

    List<PayslipDocument> findByEmployeeIdAndPayPeriodStartGreaterThanEqual(String employeeId, String startDate);
}
