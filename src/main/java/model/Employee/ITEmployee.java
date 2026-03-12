package model.Employee;

import java.util.ArrayList;
import java.util.List;

public class ITEmployee extends Employee {
    // ========== PRIVATE FIELDS ==========
    private List<String> systemPermissions;
    private List<String> userAccounts;
    private List<String> systemLogs;
    private List<String> technicalIssues;
    private List<String> resolvedIssues;
    private String itSpecialization;
    private int ticketsResolved;
    private double systemUptime;

    // ========== CONSTRUCTORS ==========

    public ITEmployee() {
        super();
        this.systemPermissions = new ArrayList<>();
        this.userAccounts = new ArrayList<>();
        this.systemLogs = new ArrayList<>();
        this.technicalIssues = new ArrayList<>();
        this.resolvedIssues = new ArrayList<>();
        this.itSpecialization = "TECHNICAL_SUPPORT";
        this.ticketsResolved = 0;
        this.systemUptime = 99.9;

        // Add default permissions
        addDefaultPermissions();
    }

    public ITEmployee(String employeeId, String firstName, String lastName, String position) {
        super(employeeId, firstName, lastName, position);
        this.systemPermissions = new ArrayList<>();
        this.userAccounts = new ArrayList<>();
        this.systemLogs = new ArrayList<>();
        this.technicalIssues = new ArrayList<>();
        this.resolvedIssues = new ArrayList<>();
        this.itSpecialization = "TECHNICAL_SUPPORT";
        this.ticketsResolved = 0;
        this.systemUptime = 99.9;

        addDefaultPermissions();
    }

    // ========== GETTERS ==========

    public List<String> getSystemPermissions() {
        return new ArrayList<>(systemPermissions);
    }

    public List<String> getUserAccounts() {
        return new ArrayList<>(userAccounts);
    }

    public List<String> getSystemLogs() {
        return new ArrayList<>(systemLogs);
    }

    public List<String> getTechnicalIssues() {
        return new ArrayList<>(technicalIssues);
    }

    public List<String> getResolvedIssues() {
        return new ArrayList<>(resolvedIssues);
    }

    public String getItSpecialization() {
        return itSpecialization;
    }

    public int getTicketsResolved() {
        return ticketsResolved;
    }

    public double getSystemUptime() {
        return systemUptime;
    }

    // ========== SETTERS WITH VALIDATION ==========

    public void setSystemPermissions(List<String> systemPermissions) {
        this.systemPermissions = systemPermissions != null ?
                new ArrayList<>(systemPermissions) : new ArrayList<>();
    }

    public void setUserAccounts(List<String> userAccounts) {
        this.userAccounts = userAccounts != null ?
                new ArrayList<>(userAccounts) : new ArrayList<>();
    }

    public void setSystemLogs(List<String> systemLogs) {
        this.systemLogs = systemLogs != null ?
                new ArrayList<>(systemLogs) : new ArrayList<>();
    }

    public void setTechnicalIssues(List<String> technicalIssues) {
        this.technicalIssues = technicalIssues != null ?
                new ArrayList<>(technicalIssues) : new ArrayList<>();
    }

    public void setResolvedIssues(List<String> resolvedIssues) {
        this.resolvedIssues = resolvedIssues != null ?
                new ArrayList<>(resolvedIssues) : new ArrayList<>();
    }

    public void setItSpecialization(String itSpecialization) {
        if (itSpecialization == null || itSpecialization.trim().isEmpty()) {
            throw new IllegalArgumentException("IT specialization cannot be empty");
        }
        String spec = itSpecialization.toUpperCase();
        if (!spec.matches("TECHNICAL_SUPPORT|NETWORK_ADMIN|SYSTEM_ADMIN|DATABASE_ADMIN|SECURITY|DEVELOPER")) {
            throw new IllegalArgumentException("Invalid IT specialization: " + itSpecialization);
        }
        this.itSpecialization = spec;
    }

    public void setTicketsResolved(int ticketsResolved) {
        if (ticketsResolved < 0) {
            throw new IllegalArgumentException("Tickets resolved cannot be negative");
        }
        this.ticketsResolved = ticketsResolved;
    }

    public void setSystemUptime(double systemUptime) {
        if (systemUptime < 0 || systemUptime > 100) {
            throw new IllegalArgumentException("System uptime must be between 0 and 100");
        }
        this.systemUptime = systemUptime;
    }

    // ========== PRIVATE METHODS ==========

    private void addDefaultPermissions() {
        systemPermissions.add("VIEW_SYSTEM_LOGS");
        systemPermissions.add("MANAGE_USER_ACCOUNTS");
        systemPermissions.add("RESET_PASSWORDS");
        systemPermissions.add("VIEW_TECHNICAL_ISSUES");
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Add system permission
     */
    public void addSystemPermission(String permission) {
        if (permission == null || permission.trim().isEmpty()) {
            throw new IllegalArgumentException("Permission cannot be empty");
        }
        if (!systemPermissions.contains(permission)) {
            systemPermissions.add(permission);
            logSystemEvent("PERMISSION_ADDED: " + permission);
        }
    }

    /**
     * Add user account
     */
    public void addUserAccount(String username, String employeeId) {
        if (username == null || employeeId == null) {
            throw new IllegalArgumentException("Username and employee ID are required");
        }
        String account = String.format("%s (ID: %s) - Created %s",
                username, employeeId, java.time.LocalDate.now());
        userAccounts.add(account);
        logSystemEvent("USER_CREATED: " + username);
    }

    /**
     * Log system event
     */
    public void logSystemEvent(String event) {
        if (event != null && !event.trim().isEmpty()) {
            String timestamped = java.time.LocalDateTime.now() + " - " + event;
            systemLogs.add(0, timestamped); // Add to beginning

            // Keep only last 1000 logs
            if (systemLogs.size() > 1000) {
                systemLogs = systemLogs.subList(0, 1000);
            }
        }
    }

    /**
     * Report technical issue
     */
    public void reportTechnicalIssue(String issue, String priority) {
        if (issue == null || issue.trim().isEmpty()) {
            throw new IllegalArgumentException("Issue description cannot be empty");
        }
        if (priority == null || !priority.matches("LOW|MEDIUM|HIGH|CRITICAL")) {
            throw new IllegalArgumentException("Priority must be LOW, MEDIUM, HIGH, or CRITICAL");
        }

        String issueEntry = String.format("[%s] %s - %s - Reported %s",
                priority, issue, "OPEN", java.time.LocalDateTime.now());
        technicalIssues.add(issueEntry);
        logSystemEvent("ISSUE_REPORTED: " + issue);
    }

    /**
     * Resolve technical issue
     */
    public void resolveTechnicalIssue(String issueId, String resolution) {
        if (issueId == null || resolution == null) {
            throw new IllegalArgumentException("Issue ID and resolution are required");
        }

        // Find and remove from open issues
        technicalIssues.removeIf(issue -> issue.contains(issueId));

        // Add to resolved issues
        String resolved = String.format("%s - RESOLVED: %s on %s",
                issueId, resolution, java.time.LocalDateTime.now());
        resolvedIssues.add(0, resolved);

        ticketsResolved++;
        logSystemEvent("ISSUE_RESOLVED: " + issueId);
    }

    /**
     * Get open issues count
     */
    public int getOpenIssuesCount() {
        return technicalIssues.size();
    }

    /**
     * Get system health status
     */
    public String getSystemHealthStatus() {
        return String.format("System Uptime: %.1f%% | Open Issues: %d | Tickets Resolved: %d",
                systemUptime, getOpenIssuesCount(), ticketsResolved);
    }

    /**
     * Get IT statistics
     */
    public java.util.Map<String, Object> getITStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("userAccounts", userAccounts.size());
        stats.put("openIssues", getOpenIssuesCount());
        stats.put("resolvedIssues", resolvedIssues.size());
        stats.put("ticketsResolved", ticketsResolved);
        stats.put("systemUptime", systemUptime + "%");
        stats.put("specialization", itSpecialization);
        return stats;
    }

    /**
     * Reset user password
     */
    public String resetUserPassword(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        String newPassword = "temp" + (int)(Math.random() * 10000);
        logSystemEvent("PASSWORD_RESET: " + username);
        return newPassword;
    }

    // ========== OVERRIDDEN ABSTRACT METHODS ==========

    @Override
    public String getDepartment() { return "INFORMATION TECHNOLOGY"; }

    @Override
    public String getRoleName() {
        return "INFORMATION TECHNOLOGY";
    }

    @Override
    public boolean canAccess(String feature) {
        switch (feature) {
            case "SYSTEM_TOOLS":
            case "USER_MANAGEMENT":
            case "SYSTEM_LOGS":
            case "TECHNICAL_SUPPORT":
            case "NETWORK_TOOLS":
                return true;
            default:
                return false;
        }
    }

    @Override
    public DashboardType getDashboardType() {
        return DashboardType.IT;
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("ITEmployee[id=%s, name=%s, spec=%s, open=%d, resolved=%d]",
                getEmployeeId(), getFullName(), itSpecialization,
                getOpenIssuesCount(), ticketsResolved);
    }
}