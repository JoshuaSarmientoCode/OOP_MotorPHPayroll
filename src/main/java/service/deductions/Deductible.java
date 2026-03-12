package service.deductions;

import java.time.YearMonth;


public interface Deductible {

    // ========== CORE CALCULATION ==========

    double calculate(double grossSalary);

    double calculateForPeriod(double grossSalary, YearMonth period);

    // ========== IDENTITY ==========

    String getName();

    String getDescription();

    // ========== RULES & VALIDATION ==========

    double getMinimumSalary();

    double getMaximumDeduction();

    boolean isApplicable(double grossSalary);

    // ========== FORMATTING ==========

    String getFormatted(double grossSalary);
}