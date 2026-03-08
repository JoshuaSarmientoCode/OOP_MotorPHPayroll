package service.deductions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.logging.Logger;

public abstract class DeductionService {
    
    protected static final Logger LOGGER = Logger.getLogger(DeductionService.class.getName());
    
    // ========== ABSTRACT METHODS ==========
    
    /**
     * Calculate deduction amount
     * @param grossSalary Gross salary for calculation
     * @return Calculated deduction amount
     */
    public abstract double calculate(double grossSalary);
    
    /**
     * Get name of deduction
     */
    public abstract String getName();
    
    /**
     * Get description of deduction
     */
    public abstract String getDescription();
    
    // ========== COMMON METHODS ==========
    
    /**
     * Round to 2 decimal places (standard for currency)
     */
    protected double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    
    /**
     * Calculate with period-specific rates (for future use)
     */
    public double calculateForPeriod(double grossSalary, YearMonth period) {
        // Default implementation just uses current rates
        return calculate(grossSalary);
    }
    
    /**
     * Get minimum salary for this deduction
     */
    public double getMinimumSalary() {
        return 0.0;
    }
    
    /**
     * Get maximum deduction amount
     */
    public double getMaximumDeduction() {
        return Double.MAX_VALUE;
    }
    
    /**
     * Validate if deduction is applicable
     */
    public boolean isApplicable(double grossSalary) {
        return grossSalary >= getMinimumSalary();
    }
    
    /**
     * Get formatted deduction amount
     */
    public String getFormatted(double grossSalary) {
        return String.format("₱ %,.2f", calculate(grossSalary));
    }
    
    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }
}