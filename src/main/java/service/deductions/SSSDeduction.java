package service.deductions;

import java.util.*;
import java.util.logging.Level;

public class SSSDeduction extends DeductionService {
    
    // ========== SSS CONTRIBUTION TABLE 2024 ==========
    // Format: {Range Start, Range End, Monthly Salary Credit, Employee Contribution, Employer Contribution}
    private static final double[][] SSS_TABLE = {
        {0, 3250, 3250, 135.00, 225.00},
        {3250, 3750, 3500, 157.50, 262.50},
        {3750, 4250, 4000, 180.00, 300.00},
        {4250, 4750, 4500, 202.50, 337.50},
        {4750, 5250, 5000, 225.00, 375.00},
        {5250, 5750, 5500, 247.50, 412.50},
        {5750, 6250, 6000, 270.00, 450.00},
        {6250, 6750, 6500, 292.50, 487.50},
        {6750, 7250, 7000, 315.00, 525.00},
        {7250, 7750, 7500, 337.50, 562.50},
        {7750, 8250, 8000, 360.00, 600.00},
        {8250, 8750, 8500, 382.50, 637.50},
        {8750, 9250, 9000, 405.00, 675.00},
        {9250, 9750, 9500, 427.50, 712.50},
        {9750, 10250, 10000, 450.00, 750.00},
        {10250, 10750, 10500, 472.50, 787.50},
        {10750, 11250, 11000, 495.00, 825.00},
        {11250, 11750, 11500, 517.50, 862.50},
        {11750, 12250, 12000, 540.00, 900.00},
        {12250, 12750, 12500, 562.50, 937.50},
        {12750, 13250, 13000, 585.00, 975.00},
        {13250, 13750, 13500, 607.50, 1012.50},
        {13750, 14250, 14000, 630.00, 1050.00},
        {14250, 14750, 14500, 652.50, 1087.50},
        {14750, 15250, 15000, 675.00, 1125.00},
        {15250, 15750, 15500, 697.50, 1162.50},
        {15750, 16250, 16000, 720.00, 1200.00},
        {16250, 16750, 16500, 742.50, 1237.50},
        {16750, 17250, 17000, 765.00, 1275.00},
        {17250, 17750, 17500, 787.50, 1312.50},
        {17750, 18250, 18000, 810.00, 1350.00},
        {18250, 18750, 18500, 832.50, 1387.50},
        {18750, 19250, 19000, 855.00, 1425.00},
        {19250, 19750, 19500, 877.50, 1462.50},
        {19750, 20250, 20000, 900.00, 1500.00},
        {20250, 20750, 20500, 922.50, 1537.50},
        {20750, 21250, 21000, 945.00, 1575.00},
        {21250, 21750, 21500, 967.50, 1612.50},
        {21750, 22250, 22000, 990.00, 1650.00},
        {22250, 22750, 22500, 1012.50, 1687.50},
        {22750, 23250, 23000, 1035.00, 1725.00},
        {23250, 23750, 23500, 1057.50, 1762.50},
        {23750, 24250, 24000, 1080.00, 1800.00},
        {24250, 24750, 24500, 1102.50, 1837.50},
        {24750, Double.MAX_VALUE, 24750, 1125.00, 1875.00}
    };
    
    private static final double MINIMUM_SALARY = 1000.0;
    private static final String NAME = "SSS Contribution";
    private static final String DESCRIPTION = "Social Security System monthly contribution";
    
    // ========== CONSTRUCTOR ==========
    
    public SSSDeduction() {
        LOGGER.fine("SSSDeduction initialized with " + SSS_TABLE.length + " brackets");
    }
    
    // ========== IMPLEMENTATION ==========
    
    @Override
    public double calculate(double grossSalary) {
        if (grossSalary < MINIMUM_SALARY) {
            LOGGER.fine("Salary below minimum, returning 0");
            return 0.0;
        }
        
        for (double[] bracket : SSS_TABLE) {
            if (grossSalary >= bracket[0] && grossSalary < bracket[1]) {
                double employeeShare = bracket[3]; // Employee contribution
                LOGGER.fine(String.format("Salary: %.2f, Bracket: %.0f-%.0f, Employee Share: %.2f",
                    grossSalary, bracket[0], bracket[1], employeeShare));
                return round(employeeShare);
            }
        }
        
        // Maximum bracket
        double maxShare = SSS_TABLE[SSS_TABLE.length - 1][3];
        LOGGER.fine("Salary above maximum bracket, using max contribution: " + maxShare);
        return round(maxShare);
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
        return MINIMUM_SALARY;
    }
    
    @Override
    public double getMaximumDeduction() {
        return SSS_TABLE[SSS_TABLE.length - 1][3];
    }
    
    /**
     * Get employer share (for reporting)
     */
    public double getEmployerShare(double grossSalary) {
        for (double[] bracket : SSS_TABLE) {
            if (grossSalary >= bracket[0] && grossSalary < bracket[1]) {
                return round(bracket[4]); // Employer contribution
            }
        }
        return round(SSS_TABLE[SSS_TABLE.length - 1][4]);
    }
    
    /**
     * Get total contribution (employee + employer)
     */
    public double getTotalContribution(double grossSalary) {
        return calculate(grossSalary) + getEmployerShare(grossSalary);
    }
    
    /**
     * Get salary bracket for given salary
     */
    public Map<String, Object> getBracketInfo(double grossSalary) {
        for (double[] bracket : SSS_TABLE) {
            if (grossSalary >= bracket[0] && grossSalary < bracket[1]) {
                Map<String, Object> info = new HashMap<>();
                info.put("rangeStart", bracket[0]);
                info.put("rangeEnd", bracket[1] == Double.MAX_VALUE ? "Above" : bracket[1]);
                info.put("salaryCredit", bracket[2]);
                info.put("employeeShare", bracket[3]);
                info.put("employerShare", bracket[4]);
                info.put("totalContribution", bracket[3] + bracket[4]);
                return info;
            }
        }
        return null;
    }
}