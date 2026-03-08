package model;

public class RegularEmployee extends Employee {
    // ========== PRIVATE FIELDS ==========
    private String department;
    private String section;
    private int leaveCredits;
    private double performanceRating;
    private String employmentType;
    private String shift;
    private String workLocation;
    
    // ========== CONSTANTS ==========
    private static final int DEFAULT_LEAVE_CREDITS = 15;
    private static final double DEFAULT_PERFORMANCE_RATING = 3.0;
    
    // ========== CONSTRUCTORS ==========
    
    public RegularEmployee() {
        super();
        this.leaveCredits = DEFAULT_LEAVE_CREDITS;
        this.performanceRating = DEFAULT_PERFORMANCE_RATING;
        this.employmentType = "FULL_TIME";
        this.shift = "DAY_SHIFT";
        this.workLocation = "OFFICE";
    }
    
    public RegularEmployee(String employeeId, String firstName, String lastName, String position) {
        super(employeeId, firstName, lastName, position);
        this.leaveCredits = DEFAULT_LEAVE_CREDITS;
        this.performanceRating = DEFAULT_PERFORMANCE_RATING;
        this.employmentType = "FULL_TIME";
        this.shift = "DAY_SHIFT";
        this.workLocation = "OFFICE";
    }
    
    // ========== GETTERS ==========
    
    public String getDepartment() { return department; }
    public String getSection() { return section; }
    public int getLeaveCredits() { return leaveCredits; }
    public double getPerformanceRating() { return performanceRating; }
    public String getEmploymentType() { return employmentType; }
    public String getShift() { return shift; }
    public String getWorkLocation() { return workLocation; }
    
    // ========== SETTERS WITH VALIDATION ==========
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public void setSection(String section) {
        this.section = section;
    }
    
    public void setLeaveCredits(int leaveCredits) {
        if (leaveCredits < 0) {
            throw new IllegalArgumentException("Leave credits cannot be negative");
        }
        if (leaveCredits > 30) {
            throw new IllegalArgumentException("Leave credits cannot exceed 30");
        }
        this.leaveCredits = leaveCredits;
    }
    
    public void setPerformanceRating(double performanceRating) {
        if (performanceRating < 1.0 || performanceRating > 5.0) {
            throw new IllegalArgumentException("Performance rating must be between 1.0 and 5.0");
        }
        this.performanceRating = performanceRating;
    }
    
    public void setEmploymentType(String employmentType) {
        if (employmentType == null || employmentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Employment type cannot be empty");
        }
        String type = employmentType.toUpperCase();
        if (!type.matches("FULL_TIME|PART_TIME|PROJECT_BASED")) {
            throw new IllegalArgumentException("Invalid employment type: " + employmentType);
        }
        this.employmentType = type;
    }
    
    public void setShift(String shift) {
        if (shift == null || shift.trim().isEmpty()) {
            throw new IllegalArgumentException("Shift cannot be empty");
        }
        String s = shift.toUpperCase();
        if (!s.matches("DAY_SHIFT|NIGHT_SHIFT|FLEXIBLE")) {
            throw new IllegalArgumentException("Invalid shift: " + shift);
        }
        this.shift = s;
    }
    
    public void setWorkLocation(String workLocation) {
        if (workLocation == null || workLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Work location cannot be empty");
        }
        String loc = workLocation.toUpperCase();
        if (!loc.matches("OFFICE|REMOTE|HYBRID")) {
            throw new IllegalArgumentException("Invalid work location: " + workLocation);
        }
        this.workLocation = loc;
    }
    
    // ========== BUSINESS METHODS ==========
    
    /**
     * Check if has sufficient leave credits
     */
    public boolean hasSufficientLeave(int days) {
        return leaveCredits >= days;
    }
    
    /**
     * Deduct leave credits
     */
    public void deductLeaveCredits(int days) {
        if (hasSufficientLeave(days)) {
            this.leaveCredits -= days;
        } else {
            throw new IllegalArgumentException("Insufficient leave credits");
        }
    }
    
    /**
     * Add leave credits (for leave conversion or adjustment)
     */
    public void addLeaveCredits(int days) {
        if (days < 0) {
            throw new IllegalArgumentException("Cannot add negative leave credits");
        }
        int newTotal = leaveCredits + days;
        if (newTotal > 30) {
            this.leaveCredits = 30;
        } else {
            this.leaveCredits = newTotal;
        }
    }
    
    /**
     * Get performance rating description
     */
    public String getPerformanceDescription() {
        if (performanceRating >= 4.5) return "EXCELLENT";
        if (performanceRating >= 3.5) return "GOOD";
        if (performanceRating >= 2.5) return "SATISFACTORY";
        if (performanceRating >= 1.5) return "NEEDS IMPROVEMENT";
        return "POOR";
    }
    
    /**
     * Get formatted shift
     */
    public String getFormattedShift() {
        switch (shift) {
            case "DAY_SHIFT": return "Day Shift (8:00 AM - 5:00 PM)";
            case "NIGHT_SHIFT": return "Night Shift (9:00 PM - 6:00 AM)";
            case "FLEXIBLE": return "Flexible Time";
            default: return shift;
        }
    }
    
    /**
     * Get employee summary
     */
    public String getSummary() {
        return String.format("%s | %s | Leave: %d days | Rating: %.1f - %s",
            getFormattedName(), department != null ? department : "No Department",
            leaveCredits, performanceRating, getPerformanceDescription());
    }
    
    // ========== OVERRIDDEN ABSTRACT METHODS ==========
    
    @Override
    public String getRoleName() {
        return "EMPLOYEE";
    }
    
    @Override
    public boolean canAccess(String feature) {
        switch (feature) {
            case "VIEW_OWN_RECORDS":
            case "SUBMIT_LEAVE":
            case "VIEW_OWN_PAYSLIP":
            case "TIME_TRACKING":
                return true;
            default:
                return false;
        }
    }
    
    @Override
    public DashboardType getDashboardType() {
        return DashboardType.EMPLOYEE;
    }
    
    // ========== OVERRIDDEN OBJECT METHODS ==========
    
    @Override
    public String toString() {
        return String.format("RegularEmployee[id=%s, name=%s, dept=%s, leave=%d]",
            getEmployeeId(), getFullName(), 
            department != null ? department : "N/A", 
            leaveCredits);
    }
}