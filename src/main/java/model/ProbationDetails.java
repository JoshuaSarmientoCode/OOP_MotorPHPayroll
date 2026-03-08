package model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class ProbationDetails {
    // ========== PRIVATE FIELDS ==========
    private LocalDate startDate;
    private LocalDate endDate;
    private String supervisor;
    private String performanceStatus;
    private boolean passedProbation;
    private String evaluationNotes;
    private LocalDate lastEvaluationDate;
    private int evaluationScore;
    
    // ========== CONSTANTS ==========
    private static final int STANDARD_PROBATION_DAYS = 180; // 6 months
    private static final int MIN_PROBATION_DAYS = 90;      // 3 months minimum
    private static final int MAX_PROBATION_DAYS = 365;     // 1 year maximum
    private static final double PROBATIONARY_SALARY_FACTOR = 0.90; // 90% of regular salary
    
    // ========== CONSTRUCTORS ==========
    
    public ProbationDetails() {
        this.startDate = LocalDate.now();
        this.endDate = startDate.plusDays(STANDARD_PROBATION_DAYS);
        this.performanceStatus = "UNDER REVIEW";
        this.passedProbation = false;
        this.evaluationScore = 0;
    }
    
    public ProbationDetails(LocalDate startDate) {
        setStartDate(startDate);
        this.performanceStatus = "UNDER REVIEW";
        this.passedProbation = false;
        this.evaluationScore = 0;
    }
    
    public ProbationDetails(LocalDate startDate, String supervisor) {
        this(startDate);
        setSupervisor(supervisor);
    }
    
    // ========== GETTERS ==========
    
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getSupervisor() { return supervisor; }
    public String getPerformanceStatus() { return performanceStatus; }
    public boolean isPassedProbation() { return passedProbation; }
    public String getEvaluationNotes() { return evaluationNotes; }
    public LocalDate getLastEvaluationDate() { return lastEvaluationDate; }
    public int getEvaluationScore() { return evaluationScore; }
    
    // ========== SETTERS WITH VALIDATION ==========
    
    public void setStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (startDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the future");
        }
        if (startDate.isBefore(LocalDate.now().minusYears(1))) {
            throw new IllegalArgumentException("Start date cannot be more than 1 year ago");
        }
        this.startDate = startDate;
        this.endDate = startDate.plusDays(STANDARD_PROBATION_DAYS);
    }
    
    public void setEndDate(LocalDate endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("End date cannot be null");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        long days = Period.between(startDate, endDate).getDays();
        if (days < MIN_PROBATION_DAYS) {
            throw new IllegalArgumentException("Probation period must be at least " + MIN_PROBATION_DAYS + " days");
        }
        if (days > MAX_PROBATION_DAYS) {
            throw new IllegalArgumentException("Probation period cannot exceed " + MAX_PROBATION_DAYS + " days");
        }
        this.endDate = endDate;
    }
    
    public void setSupervisor(String supervisor) {
        if (supervisor != null && supervisor.trim().isEmpty()) {
            this.supervisor = null;
        } else {
            this.supervisor = supervisor;
        }
    }
    
    public void setPerformanceStatus(String performanceStatus) {
        if (performanceStatus == null || performanceStatus.trim().isEmpty()) {
            throw new IllegalArgumentException("Performance status cannot be empty");
        }
        String status = performanceStatus.toUpperCase();
        if (!status.matches("UNDER REVIEW|GOOD|FAIR|POOR|PASSED|FAILED")) {
            throw new IllegalArgumentException("Invalid performance status: " + performanceStatus);
        }
        this.performanceStatus = status;
    }
    
    public void setPassedProbation(boolean passedProbation) {
        this.passedProbation = passedProbation;
        if (passedProbation) {
            this.performanceStatus = "PASSED";
        }
    }
    
    public void setEvaluationNotes(String evaluationNotes) {
        this.evaluationNotes = evaluationNotes;
    }
    
    public void setLastEvaluationDate(LocalDate lastEvaluationDate) {
        if (lastEvaluationDate != null && lastEvaluationDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Evaluation date cannot be in the future");
        }
        this.lastEvaluationDate = lastEvaluationDate;
    }
    
    public void setEvaluationScore(int evaluationScore) {
        if (evaluationScore < 0 || evaluationScore > 100) {
            throw new IllegalArgumentException("Evaluation score must be between 0 and 100");
        }
        this.evaluationScore = evaluationScore;
    }
    
    // ========== BUSINESS METHODS ==========
    
    /**
     * Calculate remaining probation days
     */
    public int getRemainingDays() {
        LocalDate now = LocalDate.now();
        if (now.isBefore(endDate)) {
            return (int) Period.between(now, endDate).getDays();
        }
        return 0;
    }
    
    /**
     * Calculate days elapsed in probation
     */
    public int getElapsedDays() {
        return (int) Period.between(startDate, LocalDate.now()).getDays();
    }
    
    /**
     * Get total probation duration in days
     */
    public int getTotalDays() {
        return (int) Period.between(startDate, endDate).getDays();
    }
    
    /**
     * Get completion percentage
     */
    public double getCompletionPercentage() {
        int total = getTotalDays();
        if (total == 0) return 0;
        int elapsed = getElapsedDays();
        return Math.min(100.0, (elapsed * 100.0) / total);
    }
    
    /**
     * Get formatted progress string
     */
    public String getProgress() {
        return String.format("%d%% COMPLETE (%d/%d days)", 
            (int) getCompletionPercentage(), getElapsedDays(), getTotalDays());
    }
    
    /**
     * Check if employee can be regularized
     */
    public boolean canBeRegularized() {
        return getRemainingDays() == 0 && 
               "PASSED".equals(performanceStatus) && 
               evaluationScore >= 75;
    }
    
    /**
     * Conduct evaluation
     */
    public void conductEvaluation(String evaluator, int score, String notes) {
        if (evaluator == null || evaluator.trim().isEmpty()) {
            throw new IllegalArgumentException("Evaluator name is required");
        }
        setEvaluationScore(score);
        setEvaluationNotes(notes);
        setLastEvaluationDate(LocalDate.now());
        
        // Auto-determine status based on score
        if (score >= 90) {
            setPerformanceStatus("GOOD");
        } else if (score >= 75) {
            setPerformanceStatus("FAIR");
        } else {
            setPerformanceStatus("POOR");
        }
        
        // Check if probation is complete and passed
        if (getRemainingDays() == 0 && score >= 75) {
            setPassedProbation(true);
        }
    }
    
    /**
     * Extend probation period
     */
    public void extendProbation(int additionalDays) {
        if (additionalDays <= 0) {
            throw new IllegalArgumentException("Extension days must be positive");
        }
        if (additionalDays > 90) {
            throw new IllegalArgumentException("Extension cannot exceed 90 days");
        }
        if (passedProbation) {
            throw new IllegalStateException("Cannot extend probation after passing");
        }
        this.endDate = endDate.plusDays(additionalDays);
    }
    
    /**
     * Calculate probationary salary (90% of regular salary)
     */
    public double calculateProbationarySalary(double regularSalary) {
        return regularSalary * PROBATIONARY_SALARY_FACTOR;
    }
    
    // ========== FORMATTED GETTERS ==========
    
    public String getFormattedStartDate() {
        return startDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }
    
    public String getFormattedEndDate() {
        return endDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
    }
    
    public String getFormattedLastEvaluation() {
        return lastEvaluationDate != null ? 
            lastEvaluationDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "Not evaluated";
    }
    
    public String getStatusBadge() {
        if (passedProbation) return "PASSED";
        if (getRemainingDays() == 0) return "EXPIRED";
        if (performanceStatus.equals("POOR")) return "AT RISK";
        return "ACTIVE";
    }
    
    // ========== OVERRIDDEN METHODS ==========
    
    @Override
    public String toString() {
        return String.format("ProbationDetails[start=%s, end=%s, remaining=%d days, status=%s]",
            startDate, endDate, getRemainingDays(), performanceStatus);
    }
}