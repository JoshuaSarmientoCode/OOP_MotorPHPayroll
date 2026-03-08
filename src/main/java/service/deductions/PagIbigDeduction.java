package service.deductions;

import java.util.HashMap;
import java.util.Map;

public class PagIbigDeduction extends DeductionService {
    
    // ========== PAG-IBIG CONTRIBUTION TABLE 2024 ==========
    private static final double MONTHLY_COMPENSATION_CAP = 5000.00;
    private static final double LOW_RATE = 0.01; // 1% for 1,500 and below
    private static final double HIGH_RATE = 0.02; // 2% for above 1,500
    private static final double THRESHOLD = 1500.00;
    
    private static final double MAXIMUM_CONTRIBUTION = 100.00; // Maximum monthly contribution
    
    private static final String NAME = "Pag-IBIG Contribution";
    private static final String DESCRIPTION = "Home Development Mutual Fund (HDMF) contribution";
    
    // ========== CONSTRUCTOR ==========
    
    public PagIbigDeduction() {
        LOGGER.fine("PagIbigDeduction initialized with threshold: " + THRESHOLD + 
                   ", rates: " + (LOW_RATE * 100) + "%/" + (HIGH_RATE * 100) + "%");
    }
    
    // ========== IMPLEMENTATION ==========
    
    @Override
    public double calculate(double grossSalary) {
        if (grossSalary <= 0) {
            return 0.0;
        }
        
        // Cap the monthly compensation used for computation
        double cappedSalary = Math.min(grossSalary, MONTHLY_COMPENSATION_CAP);
        
        double contribution;
        
        if (cappedSalary <= THRESHOLD) {
            contribution = cappedSalary * LOW_RATE;
        } else {
            contribution = cappedSalary * HIGH_RATE;
        }
        
        // Apply maximum contribution limit
        double finalContribution = Math.min(contribution, MAXIMUM_CONTRIBUTION);
        
        LOGGER.fine(String.format("Salary: %.2f, Capped: %.2f, Contribution: %.2f, Final: %.2f",
            grossSalary, cappedSalary, contribution, finalContribution));
        
        return round(finalContribution);
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
        return MAXIMUM_CONTRIBUTION;
    }
    
    /**
     * Get employer share (for reporting)
     */
    public double getEmployerShare(double grossSalary) {
        // Employer usually matches employee contribution
        return calculate(grossSalary);
    }
    
    /**
     * Get total contribution (employee + employer)
     */
    public double getTotalContribution(double grossSalary) {
        return calculate(grossSalary) * 2;
    }
    
    /**
     * Get contribution breakdown
     */
    public Map<String, Object> getBreakdown(double grossSalary) {
        Map<String, Object> breakdown = new HashMap<>();
        
        double cappedSalary = Math.min(grossSalary, MONTHLY_COMPENSATION_CAP);
        double rate = (cappedSalary <= THRESHOLD) ? LOW_RATE : HIGH_RATE;
        double employeeShare = calculate(grossSalary);
        double employerShare = employeeShare;
        
        breakdown.put("grossSalary", grossSalary);
        breakdown.put("cappedSalary", cappedSalary);
        breakdown.put("rate", rate * 100 + "%");
        breakdown.put("employeeShare", employeeShare);
        breakdown.put("employerShare", employerShare);
        breakdown.put("totalContribution", employeeShare + employerShare);
        
        if (cappedSalary <= THRESHOLD) {
            breakdown.put("basis", "Below threshold, " + (LOW_RATE * 100) + "% rate");
        } else {
            breakdown.put("basis", "Above threshold, " + (HIGH_RATE * 100) + "% rate");
        }
        
        if (employeeShare >= MAXIMUM_CONTRIBUTION) {
            breakdown.put("note", "Maximum contribution applied");
        }
        
        return breakdown;
    }
    
    /**
     * Check if salary is below threshold
     */
    public boolean isBelowThreshold(double grossSalary) {
        double cappedSalary = Math.min(grossSalary, MONTHLY_COMPENSATION_CAP);
        return cappedSalary <= THRESHOLD;
    }
    
    /**
     * Get applicable rate
     */
    public double getApplicableRate(double grossSalary) {
        double cappedSalary = Math.min(grossSalary, MONTHLY_COMPENSATION_CAP);
        return (cappedSalary <= THRESHOLD) ? LOW_RATE : HIGH_RATE;
    }
}