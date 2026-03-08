package service.deductions;

import java.util.HashMap;
import java.util.Map;

public class PhilHealthDeduction extends DeductionService {
    
    // ========== PHILHEALTH CONTRIBUTION TABLE 2024 ==========
    private static final double PREMIUM_RATE = 0.045; // 4.5%
    private static final double MINIMUM_MONTHLY_PREMIUM = 450.00;
    private static final double MAXIMUM_MONTHLY_PREMIUM = 3600.00;
    private static final double EMPLOYEE_SHARE_RATIO = 0.50; // 50%
    
    private static final double MINIMUM_SALARY = 10000.00;
    private static final double MAXIMUM_SALARY = 80000.00;
    
    private static final String NAME = "PhilHealth Contribution";
    private static final String DESCRIPTION = "Philippine Health Insurance Corporation premium";
    
    // ========== CONSTRUCTOR ==========
    
    public PhilHealthDeduction() {
        LOGGER.fine("PhilHealthDeduction initialized with rate: " + (PREMIUM_RATE * 100) + "%");
    }
    
    // ========== IMPLEMENTATION ==========
    
    @Override
    public double calculate(double grossSalary) {
        if (grossSalary <= 0) {
            return 0.0;
        }
        
        double monthlyPremium;
        
        if (grossSalary <= MINIMUM_SALARY) {
            monthlyPremium = MINIMUM_MONTHLY_PREMIUM;
        } else if (grossSalary >= MAXIMUM_SALARY) {
            monthlyPremium = MAXIMUM_MONTHLY_PREMIUM;
        } else {
            monthlyPremium = grossSalary * PREMIUM_RATE;
        }
        
        // Employee share is 50% of total premium
        double employeeShare = monthlyPremium * EMPLOYEE_SHARE_RATIO;
        
        LOGGER.fine(String.format("Salary: %.2f, Total Premium: %.2f, Employee Share: %.2f",
            grossSalary, monthlyPremium, employeeShare));
        
        return round(employeeShare);
    }
    
    @Override
    public String getName() {
        return NAME;
    }
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    @Override
    public double getMinimumSalary() {
        return 0;
    }
    
    @Override
    public double getMaximumDeduction() {
        return MAXIMUM_MONTHLY_PREMIUM * EMPLOYEE_SHARE_RATIO;
    }
    
    /**
     * Get employer share (for reporting)
     */
    public double getEmployerShare(double grossSalary) {
        return calculate(grossSalary); // Equal share
    }
    
    /**
     * Get total premium (employee + employer)
     */
    public double getTotalPremium(double grossSalary) {
        return calculate(grossSalary) * 2; // Double the employee share
    }
    
    /**
     * Get premium breakdown
     */
    public Map<String, Object> getBreakdown(double grossSalary) {
        Map<String, Object> breakdown = new HashMap<>();
        
        double employeeShare = calculate(grossSalary);
        double employerShare = employeeShare;
        double totalPremium = employeeShare + employerShare;
        
        breakdown.put("grossSalary", grossSalary);
        breakdown.put("monthlyPremium", totalPremium);
        breakdown.put("employeeShare", employeeShare);
        breakdown.put("employerShare", employerShare);
        breakdown.put("premiumRate", PREMIUM_RATE * 100 + "%");
        
        if (grossSalary <= MINIMUM_SALARY) {
            breakdown.put("basis", "Minimum premium applied");
        } else if (grossSalary >= MAXIMUM_SALARY) {
            breakdown.put("basis", "Maximum premium applied");
        } else {
            breakdown.put("basis", "Computed as " + (PREMIUM_RATE * 100) + "% of salary");
        }
        
        return breakdown;
    }
    
    /**
     * Get salary cap info
     */
    public Map<String, Double> getSalaryCaps() {
        Map<String, Double> caps = new HashMap<>();
        caps.put("minimumSalary", MINIMUM_SALARY);
        caps.put("maximumSalary", MAXIMUM_SALARY);
        caps.put("minimumPremium", MINIMUM_MONTHLY_PREMIUM);
        caps.put("maximumPremium", MAXIMUM_MONTHLY_PREMIUM);
        return caps;
    }
}