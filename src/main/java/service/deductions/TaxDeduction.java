package service.deductions;

import java.util.*;

public class TaxDeduction extends DeductionService {
    
    // ========== TAX BRACKETS (TRAIN Law) ==========
    private static final TaxBracket[] TAX_BRACKETS = {
        new TaxBracket(0, 20833.33, 0, 0, "Below minimum taxable income"),
        new TaxBracket(20833.34, 33333.00, 0, 0.15, "15% over 20,833"),
        new TaxBracket(33333.01, 66667.00, 1875.00, 0.20, "20% over 33,333"),
        new TaxBracket(66667.01, 166667.00, 8541.67, 0.25, "25% over 66,667"),
        new TaxBracket(166667.01, 666667.00, 33541.67, 0.30, "30% over 166,667"),
        new TaxBracket(666667.01, Double.MAX_VALUE, 183541.67, 0.35, "35% over 666,667")
    };
    
    private static final double NON_TAXABLE_INCOME = 20833.33;
    private static final double MINIMUM_WAGE_EXEMPTION = 250000.00; // Annual
    
    private static final String NAME = "Withholding Tax";
    private static final String DESCRIPTION = "Tax on compensation income (TRAIN Law)";
    
    // ========== CONSTRUCTOR ==========
    
    public TaxDeduction() {
        LOGGER.fine("TaxDeduction initialized with " + TAX_BRACKETS.length + " brackets");
    }
    
    // ========== INNER CLASS ==========
    
    private static class TaxBracket {
        final double min;
        final double max;
        final double baseTax;
        final double rate;
        final String description;
        
        TaxBracket(double min, double max, double baseTax, double rate, String description) {
            this.min = min;
            this.max = max;
            this.baseTax = baseTax;
            this.rate = rate;
            this.description = description;
        }
        
        boolean contains(double income) {
            return income >= min && income <= max;
        }
        
        double calculateTax(double income) {
            return baseTax + ((income - (min - 0.01)) * rate);
        }
        
        @Override
        public String toString() {
            return String.format("%.2f - %.2f: %s", min, max, description);
        }
    }
    
    // ========== IMPLEMENTATION ==========
    
    @Override
    public double calculate(double taxableIncome) {
        if (taxableIncome < 0) {
            throw new IllegalArgumentException("Taxable income cannot be negative");
        }
        
        // Non-taxable threshold
        if (taxableIncome <= NON_TAXABLE_INCOME) {
            LOGGER.fine("Income below non-taxable threshold: " + taxableIncome);
            return 0.0;
        }
        
        for (TaxBracket bracket : TAX_BRACKETS) {
            if (bracket.contains(taxableIncome)) {
                double tax = bracket.calculateTax(taxableIncome);
                LOGGER.fine(String.format("Income: %.2f, Bracket: %s, Tax: %.2f",
                    taxableIncome, bracket.description, tax));
                return round(tax);
            }
        }
        
        // Should never reach here
        return 0.0;
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
        return NON_TAXABLE_INCOME;
    }
    
    /**
     * Calculate annual tax (for reference)
     */
    public double calculateAnnualTax(double annualTaxableIncome) {
        return calculate(annualTaxableIncome / 12) * 12;
    }
    
    /**
     * Get effective tax rate
     */
    public double getEffectiveTaxRate(double taxableIncome) {
        if (taxableIncome <= 0) return 0.0;
        double tax = calculate(taxableIncome);
        return (tax / taxableIncome) * 100;
    }
    
    /**
     * Get tax bracket info
     */
    public Map<String, Object> getTaxBracketInfo(double taxableIncome) {
        Map<String, Object> info = new HashMap<>();
        info.put("taxableIncome", taxableIncome);
        
        for (TaxBracket bracket : TAX_BRACKETS) {
            if (bracket.contains(taxableIncome)) {
                info.put("bracket", bracket.toString());
                info.put("baseTax", bracket.baseTax);
                info.put("rate", bracket.rate * 100 + "%");
                info.put("excessOver", bracket.min - 0.01);
                info.put("taxAmount", bracket.calculateTax(taxableIncome));
                return info;
            }
        }
        
        info.put("bracket", "No tax");
        info.put("taxAmount", 0.0);
        return info;
    }
    
    /**
     * Get all tax brackets
     */
    public List<Map<String, Object>> getAllBrackets() {
        List<Map<String, Object>> brackets = new ArrayList<>();
        
        for (TaxBracket bracket : TAX_BRACKETS) {
            Map<String, Object> b = new HashMap<>();
            b.put("min", bracket.min);
            b.put("max", bracket.max == Double.MAX_VALUE ? "Above" : bracket.max);
            b.put("baseTax", bracket.baseTax);
            b.put("rate", bracket.rate * 100 + "%");
            b.put("description", bracket.description);
            brackets.add(b);
        }
        
        return brackets;
    }
    
    /**
     * Calculate tax with dependents (for future enhancement)
     */
    public double calculateWithDependents(double taxableIncome, int dependents) {
        double exemptionPerDependent = 25000.00; // Example: 25k per dependent
        double totalExemption = dependents * exemptionPerDependent;
        double netTaxable = Math.max(0, taxableIncome - (totalExemption / 12));
        return calculate(netTaxable);
    }
}