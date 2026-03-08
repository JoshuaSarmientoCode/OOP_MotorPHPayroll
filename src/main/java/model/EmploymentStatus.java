package model;

public enum EmploymentStatus {
    REGULAR("Regular Employee", true, true, true, 1.0),
    PROBATIONARY("Probationary Employee", true, false, true, 0.9),
    CONTRACTUAL("Contractual Employee", true, false, false, 0.95),
    PART_TIME("Part-Time Employee", true, false, false, 0.5),
    RESIGNED("Resigned", false, false, false, 0.0),
    TERMINATED("Terminated", false, false, false, 0.0),
    ON_LEAVE("On Leave", false, false, false, 0.0);
    
    private final String displayName;
    private final boolean canAccessSystem;
    private final boolean canRequestAdvance;
    private final boolean canApplyForPromotion;
    private final double salaryMultiplier;
    
    EmploymentStatus(String displayName, boolean canAccessSystem, 
                     boolean canRequestAdvance, boolean canApplyForPromotion,
                     double salaryMultiplier) {
        this.displayName = displayName;
        this.canAccessSystem = canAccessSystem;
        this.canRequestAdvance = canRequestAdvance;
        this.canApplyForPromotion = canApplyForPromotion;
        this.salaryMultiplier = salaryMultiplier;
    }
    
    // ========== GETTERS ==========
    
    public String getDisplayName() { return displayName; }
    public boolean canAccessSystem() { return canAccessSystem; }
    public boolean canRequestAdvance() { return canRequestAdvance; }
    public boolean canApplyForPromotion() { return canApplyForPromotion; }
    public double getSalaryMultiplier() { return salaryMultiplier; }
    
    // ========== BUSINESS METHODS ==========
    
    /**
     * Check if employee is active (can work)
     */
    public boolean isActive() {
        return this == REGULAR || this == PROBATIONARY || 
               this == CONTRACTUAL || this == PART_TIME;
    }
    
    /**
     * Check if employee can file leave
     */
    public boolean canFileLeave() {
        return this == REGULAR || this == PROBATIONARY;
    }
    
    /**
     * Check if employee can receive benefits
     */
    public boolean hasBenefits() {
        return this == REGULAR;
    }
    
    /**
     * Get allowed actions for this status
     */
    public String[] getAllowedActions() {
        switch (this) {
            case REGULAR:
                return new String[]{"VIEW_PAYSLIP", "FILE_LEAVE", "REQUEST_OVERTIME", 
                                    "REQUEST_ADVANCE", "APPLY_PROMOTION"};
            case PROBATIONARY:
                return new String[]{"VIEW_PAYSLIP", "FILE_LEAVE"};
            case CONTRACTUAL:
                return new String[]{"VIEW_PAYSLIP"};
            case PART_TIME:
                return new String[]{"VIEW_PAYSLIP"};
            default:
                return new String[]{};
        }
    }
    
    /**
     * Get status from string (case insensitive)
     */
    public static EmploymentStatus fromString(String status) {
        if (status == null || status.trim().isEmpty()) {
            return REGULAR;
        }
        
        String upper = status.trim().toUpperCase();
        try {
            return EmploymentStatus.valueOf(upper);
        } catch (IllegalArgumentException e) {
            // Try to match by display name
            for (EmploymentStatus es : values()) {
                if (es.displayName.equalsIgnoreCase(status.trim())) {
                    return es;
                }
            }
            return REGULAR;
        }
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}