package model.Employee;

import model.LeaveRequest;

import java.util.ArrayList;
import java.util.List;

public class HREmployee extends Employee {
    // ========== PRIVATE FIELDS ==========
    private List<LeaveRequest> pendingApprovals;
    private int maxEmployeesCanManage;
    private List<String> recruitmentPipeline;
    private List<String> openPositions;
    private List<String> conductedInterviews;
    private int employeesManaged;
    private String hrSpecialization;

    // ========== CONSTRUCTORS ==========

    public HREmployee() {
        super();
        this.pendingApprovals = new ArrayList<>();
        this.recruitmentPipeline = new ArrayList<>();
        this.openPositions = new ArrayList<>();
        this.conductedInterviews = new ArrayList<>();
        this.maxEmployeesCanManage = 50;
        this.employeesManaged = 0;
        this.hrSpecialization = "GENERALIST";
    }

    public HREmployee(String employeeId, String firstName, String lastName, String position) {
        super(employeeId, firstName, lastName, position);
        this.pendingApprovals = new ArrayList<>();
        this.recruitmentPipeline = new ArrayList<>();
        this.openPositions = new ArrayList<>();
        this.conductedInterviews = new ArrayList<>();
        this.maxEmployeesCanManage = 50;
        this.employeesManaged = 0;
        this.hrSpecialization = "GENERALIST";
    }

    // ========== GETTERS ==========

    public List<LeaveRequest> getPendingApprovals() {
        return new ArrayList<>(pendingApprovals);
    }

    public int getMaxEmployeesCanManage() {
        return maxEmployeesCanManage;
    }

    public List<String> getRecruitmentPipeline() {
        return new ArrayList<>(recruitmentPipeline);
    }

    public List<String> getOpenPositions() {
        return new ArrayList<>(openPositions);
    }

    public List<String> getConductedInterviews() {
        return new ArrayList<>(conductedInterviews);
    }

    public int getEmployeesManaged() {
        return employeesManaged;
    }

    public String getHrSpecialization() {
        return hrSpecialization;
    }

    // ========== SETTERS WITH VALIDATION ==========

    public void setPendingApprovals(List<LeaveRequest> pendingApprovals) {
        this.pendingApprovals = pendingApprovals != null ?
                new ArrayList<>(pendingApprovals) : new ArrayList<>();
    }

    public void setMaxEmployeesCanManage(int maxEmployeesCanManage) {
        if (maxEmployeesCanManage < 1) {
            throw new IllegalArgumentException("Max employees must be at least 1");
        }
        if (maxEmployeesCanManage > 500) {
            throw new IllegalArgumentException("Max employees cannot exceed 500");
        }
        this.maxEmployeesCanManage = maxEmployeesCanManage;
    }

    public void setRecruitmentPipeline(List<String> recruitmentPipeline) {
        this.recruitmentPipeline = recruitmentPipeline != null ?
                new ArrayList<>(recruitmentPipeline) : new ArrayList<>();
    }

    public void setOpenPositions(List<String> openPositions) {
        this.openPositions = openPositions != null ?
                new ArrayList<>(openPositions) : new ArrayList<>();
    }

    public void setConductedInterviews(List<String> conductedInterviews) {
        this.conductedInterviews = conductedInterviews != null ?
                new ArrayList<>(conductedInterviews) : new ArrayList<>();
    }

    public void setEmployeesManaged(int employeesManaged) {
        if (employeesManaged < 0) {
            throw new IllegalArgumentException("Employees managed cannot be negative");
        }
        if (employeesManaged > maxEmployeesCanManage) {
            throw new IllegalArgumentException("Cannot exceed maximum capacity");
        }
        this.employeesManaged = employeesManaged;
    }

    public void setHrSpecialization(String hrSpecialization) {
        if (hrSpecialization == null || hrSpecialization.trim().isEmpty()) {
            throw new IllegalArgumentException("HR specialization cannot be empty");
        }
        String spec = hrSpecialization.toUpperCase();
        if (!spec.matches("GENERALIST|RECRUITMENT|TRAINING|COMPENSATION|EMPLOYEE_RELATIONS")) {
            throw new IllegalArgumentException("Invalid HR specialization: " + hrSpecialization);
        }
        this.hrSpecialization = spec;
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Add pending approval request
     */
    public void addPendingApproval(LeaveRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Leave request cannot be null");
        }
        if (!pendingApprovals.contains(request)) {
            pendingApprovals.add(request);
        }
    }

    /**
     * Remove pending approval request
     */
    public void removePendingApproval(LeaveRequest request) {
        pendingApprovals.remove(request);
    }

    /**
     * Get count of pending approvals
     */
    public int getPendingApprovalsCount() {
        return pendingApprovals.size();
    }

    /**
     * Add open position
     */
    public void addOpenPosition(String position) {
        if (position == null || position.trim().isEmpty()) {
            throw new IllegalArgumentException("Position cannot be empty");
        }
        if (!openPositions.contains(position)) {
            openPositions.add(position);
        }
    }

    /**
     * Remove open position
     */
    public void removeOpenPosition(String position) {
        openPositions.remove(position);
    }

    /**
     * Add candidate to recruitment pipeline
     */
    public void addCandidate(String candidateName, String position) {
        if (candidateName == null || position == null) {
            throw new IllegalArgumentException("Candidate name and position are required");
        }
        String entry = String.format("%s - %s - Added on %s",
                candidateName, position, java.time.LocalDate.now());
        recruitmentPipeline.add(0, entry); // Add to beginning
    }

    /**
     * Conduct interview
     */
    public void conductInterview(String candidateName, String position, String result) {
        if (candidateName == null || position == null || result == null) {
            throw new IllegalArgumentException("All interview details are required");
        }
        String interview = String.format("%s for %s - %s - %s",
                candidateName, position, result, java.time.LocalDate.now());
        conductedInterviews.add(0, interview); // Add to beginning
    }

    /**
     * Check if can manage more employees
     */
    public boolean canManageMoreEmployees() {
        return employeesManaged < maxEmployeesCanManage;
    }

    /**
     * Increment employees managed count
     */
    public void addManagedEmployee() {
        if (!canManageMoreEmployees()) {
            throw new IllegalStateException("Maximum employee management capacity reached");
        }
        employeesManaged++;
    }

    /**
     * Decrement employees managed count
     */
    public void removeManagedEmployee() {
        if (employeesManaged > 0) {
            employeesManaged--;
        }
    }

    /**
     * Get recruitment statistics
     */
    public java.util.Map<String, Object> getRecruitmentStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("openPositions", openPositions.size());
        stats.put("candidatesInPipeline", recruitmentPipeline.size());
        stats.put("interviewsConducted", conductedInterviews.size());
        stats.put("employeesManaged", employeesManaged);
        stats.put("capacityRemaining", maxEmployeesCanManage - employeesManaged);
        return stats;
    }

    // ========== OVERRIDDEN ABSTRACT METHODS ==========

    @Override
    public String getDepartment() { return "HUMAN RESOURCES"; }

    @Override
    public String getRoleName() {
        return "HUMAN RESOURCES";
    }

    @Override
    public boolean canAccess(String feature) {
        switch (feature) {
            case "EMPLOYEE_MANAGEMENT":
            case "LEAVE_MANAGEMENT":
            case "LEAVE_APPROVALS":
            case "VIEW_REPORTS":
            case "RECRUITMENT":
            case "TRAINING":
                return true;
            default:
                return false;
        }
    }

    @Override
    public DashboardType getDashboardType() {
        return DashboardType.HR;
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("HREmployee[id=%s, name=%s, specialization=%s, pending=%d]",
                getEmployeeId(), getFullName(), hrSpecialization, pendingApprovals.size());
    }
}