package service.deductions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.logging.Logger;

public abstract class DeductionService implements Deductible {

    protected static final Logger LOGGER = Logger.getLogger(DeductionService.class.getName());

    // ========== ABSTRACT METHODS ==========

    @Override
    public abstract double calculate(double grossSalary);

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    // ========== COMMON METHODS ==========

    protected double round(double value) {
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public double calculateForPeriod(double grossSalary, YearMonth period) {
        return calculate(grossSalary);
    }

    @Override
    public double getMinimumSalary() {
        return 0.0;
    }

    @Override
    public double getMaximumDeduction() {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean isApplicable(double grossSalary) {
        return grossSalary >= getMinimumSalary();
    }

    @Override
    public String getFormatted(double grossSalary) {
        return String.format("₱ %,.2f", calculate(grossSalary));
    }

    @Override
    public String toString() {
        return getName() + " - " + getDescription();
    }
}