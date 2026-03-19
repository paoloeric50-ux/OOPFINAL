package com.motorph.payroll.controller;

import com.motorph.payroll.dto.DeductionCalculateRequestDto;
import com.motorph.payroll.model.DeductionCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/deductions")
public class DeductionController {

    @Autowired private DeductionCalculator deductionCalculator;

    @PostMapping("/calculate")
    public Map<String, Object> calculateDeductions(@RequestBody DeductionCalculateRequestDto request) {
        return deductionCalculator.calculateAllDeductions(request.getGrossSalary());
    }

    @GetMapping("/tables")
    public Map<String, Object> getDeductionTables() {
        Map<String, Object> sss = new HashMap<>();
        sss.put("description", "SSS Contribution Table (2024)");
        sss.put("brackets", 54);
        sss.put("min_contribution", 180.00);
        sss.put("max_contribution", 1350.00);

        Map<String, Object> philhealth = new HashMap<>();
        philhealth.put("description", "PhilHealth Premium (2024)");
        philhealth.put("rate", "5% of salary");
        philhealth.put("min_premium", 500.00);
        philhealth.put("max_premium", 5000.00);
        philhealth.put("employee_share", "50%");

        Map<String, Object> pagibig = new HashMap<>();
        pagibig.put("description", "Pag-IBIG Fund Contribution");
        pagibig.put("rate_low", "1% for salary <= 1500");
        pagibig.put("rate_high", "2% for salary > 1500");
        pagibig.put("max_contribution", 100.00);

        Map<String, Object> withholdingTax = new HashMap<>();
        withholdingTax.put("description", "TRAIN Law Tax Table (2024)");
        withholdingTax.put("brackets", List.of(
            Map.of("range", "Up to ₱250,000/year", "rate", "Exempt"),
            Map.of("range", "₱250,001 - ₱400,000", "rate", "15% of excess over ₱250K"),
            Map.of("range", "₱400,001 - ₱800,000", "rate", "₱22,500 + 20% of excess over ₱400K"),
            Map.of("range", "₱800,001 - ₱2,000,000", "rate", "₱102,500 + 25% of excess over ₱800K"),
            Map.of("range", "₱2,000,001 - ₱8,000,000", "rate", "₱402,500 + 30% of excess over ₱2M"),
            Map.of("range", "Over ₱8,000,000", "rate", "₱2,202,500 + 35% of excess over ₱8M")
        ));

        Map<String, Object> result = new HashMap<>();
        result.put("sss", sss);
        result.put("philhealth", philhealth);
        result.put("pagibig", pagibig);
        result.put("withholding_tax", withholdingTax);
        return result;
    }
}
