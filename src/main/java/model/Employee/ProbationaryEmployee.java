package model.Employee;

import java.time.LocalDate;
import java.time.Period;

public class ProbationaryEmployee extends RegularEmployee {
    private LocalDate probationStartDate;
    private LocalDate probationEndDate;
    private double probationarySalary;
    private String supervisor;
    private int evaluationPeriod;
    private boolean passedProbation;
    private int remainingProbationDays;
    private String performanceStatus;
    private boolean canBeRegularized;
    
    // Probation-specific constants
    private static final int STANDARD_PROBATION_DAYS = 180; // 6 months
    private static final double PROBATIONARY_SALARY_FACTOR = 0.90; // 90% of regular salary
    
    public ProbationaryEmployee() {
        super();
        this.probationStartDate = LocalDate.now();
        this.probationEndDate = LocalDate.now().plusDays(STANDARD_PROBATION_DAYS);
        this.probationarySalary = super.getBasicSalary() * PROBATIONARY_SALARY_FACTOR;
        this.evaluationPeriod = 30; // Monthly evaluations
        this.passedProbation = false;
        this.performanceStatus = "UNDER REVIEW";
        this.canBeRegularized = false;
        calculateRemainingDays();
    }
    
    public ProbationaryEmployee(LocalDate startDate) {
        super();
        this.probationStartDate = startDate;
        this.probationEndDate = startDate.plusDays(STANDARD_PROBATION_DAYS);
        this.probationarySalary = super.getBasicSalary() * PROBATIONARY_SALARY_FACTOR;
        this.evaluationPeriod = 30;
        this.passedProbation = false;
        this.performanceStatus = "UNDER REVIEW";
        this.canBeRegularized = false;
        calculateRemainingDays();
    }
    
    // Getters and Setters
    public LocalDate getProbationStartDate() { return probationStartDate; }
    public void setProbationStartDate(LocalDate date) { 
        this.probationStartDate = date;
        this.probationEndDate = date.plusDays(STANDARD_PROBATION_DAYS);
        calculateRemainingDays();
    }
    
    public LocalDate getProbationEndDate() { return probationEndDate; }
    
    public double getProbationarySalary() { return probationarySalary; }
    public void setProbationarySalary(double salary) { this.probationarySalary = salary; }
    
    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }
    
    public int getEvaluationPeriod() { return evaluationPeriod; }
    public void setEvaluationPeriod(int days) { this.evaluationPeriod = days; }
    
    public boolean isPassedProbation() { return passedProbation; }
    public void setPassedProbation(boolean passed) { 
        this.passedProbation = passed;
        if (passed) {
            this.performanceStatus = "PASSED";
            this.canBeRegularized = true;
        }
    }
    
    public int getRemainingProbationDays() { return remainingProbationDays; }
    
    public String getPerformanceStatus() { return performanceStatus; }
    public void setPerformanceStatus(String status) { this.performanceStatus = status; }
    
    public boolean isCanBeRegularized() { return canBeRegularized; }
    public void setCanBeRegularized(boolean canBe) { this.canBeRegularized = canBe; }
    
    // Business methods
    private void calculateRemainingDays() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(probationEndDate)) {
            this.remainingProbationDays = (int) Period.between(now, probationEndDate).getDays();
        } else {
            this.remainingProbationDays = 0;
        }
    }
    
    public void conductEvaluation(String evaluator, String comments) {
        // Simulate evaluation process
        System.out.println("Evaluation conducted by: " + evaluator);
        System.out.println("Comments: " + comments);
        
        // Logic to determine if probation is passed
        if (remainingProbationDays <= 30 && performanceStatus.equals("GOOD")) {
            this.passedProbation = true;
            this.canBeRegularized = true;
        }
    }
    
    public RegularEmployee convertToRegular() {
        if (!canBeRegularized) {
            throw new IllegalStateException("Employee cannot be regularized yet");
        }
        
        RegularEmployee regular = new RegularEmployee();
        // Copy common properties
        regular.setEmployeeId(this.getEmployeeId());
        regular.setFirstName(this.getFirstName());
        regular.setLastName(this.getLastName());
        regular.setEmail(this.getEmail());
        regular.setPhoneNumber(this.getPhoneNumber());
        regular.setAddress(this.getAddress());
        regular.setBirthDate(this.getBirthDate());
        regular.setBasicSalary(this.getBasicSalary()); // Full salary now
        regular.setRiceSubsidy(this.getRiceSubsidy());
        regular.setPhoneAllowance(this.getPhoneAllowance());
        regular.setClothingAllowance(this.getClothingAllowance());
        regular.setPosition(this.getPosition());
        regular.setHireDate(this.getHireDate());
        regular.setStatus("REGULAR");
        regular.setImmediateSupervisor(this.getImmediateSupervisor());
        regular.setGovernmentIds(this.getGovernmentIds());
        
        return regular;
    }
    
    public String getProbationProgress() {
        int totalDays = STANDARD_PROBATION_DAYS;
        int daysElapsed = totalDays - remainingProbationDays;
        double percentComplete = (daysElapsed * 100.0) / totalDays;
        return String.format("%d%% COMPLETE (%d/%d days)", 
            (int)percentComplete, daysElapsed, totalDays);
    }
    
    @Override
    public String getRoleName() {
        return "PROBATIONARY EMPLOYEE";
    }
    
    @Override
    public boolean canAccess(String feature) {
        // Probationary employees have same access as regular employees
        // but may have additional restrictions
        switch (feature) {
            case "VIEW_OWN_RECORDS":
            case "SUBMIT_LEAVE":
            case "VIEW_OWN_PAYSLIP":
            case "TIME_TRACKING":
                return true;
            case "REQUEST_ADVANCE_SALARY":
            case "APPLY_FOR_PROMOTION":
                return false; // Additional restrictions
            default:
                return false;
        }
    }
    
    @Override
    public DashboardType getDashboardType() {
        return DashboardType.EMPLOYEE; // Same dashboard as regular employees
    }
    
    @Override
    public String toString() {
        return String.format("ProbationaryEmployee[id=%s, name=%s, remaining=%d days, status=%s]",
            getEmployeeId(), getFullName(), remainingProbationDays, performanceStatus);
    }
}