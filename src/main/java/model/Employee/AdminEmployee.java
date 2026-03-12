package model.Employee;

import java.util.ArrayList;
import java.util.List;

public class AdminEmployee extends Employee {
    // ========== PRIVATE FIELDS ==========
    private List<String> systemPermissions;
    private List<String> auditLogs;
    private boolean canManageAllUsers;
    private String adminLevel;
    private List<String> managedDepartments;

    // ========== CONSTRUCTORS ==========

    public AdminEmployee() {
        super();
        this.systemPermissions = new ArrayList<>();
        this.auditLogs = new ArrayList<>();
        this.managedDepartments = new ArrayList<>();
        this.canManageAllUsers = true;
        this.adminLevel = "SUPER_ADMIN";

        // Add default permissions
        addDefaultPermissions();
    }

    public AdminEmployee(String employeeId, String firstName, String lastName, String position) {
        super(employeeId, firstName, lastName, position);
        this.systemPermissions = new ArrayList<>();
        this.auditLogs = new ArrayList<>();
        this.managedDepartments = new ArrayList<>();
        this.canManageAllUsers = true;
        this.adminLevel = "SUPER_ADMIN";

        addDefaultPermissions();
    }

    // ========== GETTERS ==========

    public List<String> getSystemPermissions() {
        return new ArrayList<>(systemPermissions);
    }

    public List<String> getAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    public boolean isCanManageAllUsers() {
        return canManageAllUsers;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public List<String> getManagedDepartments() {
        return new ArrayList<>(managedDepartments);
    }

    // ========== SETTERS WITH VALIDATION ==========

    public void setSystemPermissions(List<String> systemPermissions) {
        this.systemPermissions = systemPermissions != null ?
                new ArrayList<>(systemPermissions) : new ArrayList<>();
    }

    public void setAuditLogs(List<String> auditLogs) {
        this.auditLogs = auditLogs != null ?
                new ArrayList<>(auditLogs) : new ArrayList<>();
    }

    public void setCanManageAllUsers(boolean canManageAllUsers) {
        this.canManageAllUsers = canManageAllUsers;
    }

    public void setAdminLevel(String adminLevel) {
        if (adminLevel == null || adminLevel.trim().isEmpty()) {
            throw new IllegalArgumentException("Admin level cannot be empty");
        }
        String level = adminLevel.toUpperCase();
        if (!level.matches("SUPER_ADMIN|ADMIN|MODERATOR")) {
            throw new IllegalArgumentException("Invalid admin level: " + adminLevel);
        }
        this.adminLevel = level;
    }

    public void setManagedDepartments(List<String> managedDepartments) {
        this.managedDepartments = managedDepartments != null ?
                new ArrayList<>(managedDepartments) : new ArrayList<>();
    }

    // ========== PRIVATE METHODS ==========

    private void addDefaultPermissions() {
        systemPermissions.add("VIEW_ALL_EMPLOYEES");
        systemPermissions.add("EDIT_ALL_EMPLOYEES");
        systemPermissions.add("DELETE_EMPLOYEES");
        systemPermissions.add("MANAGE_USERS");
        systemPermissions.add("PROCESS_PAYROLL");
        systemPermissions.add("VIEW_ALL_PAYROLL");
        systemPermissions.add("APPROVE_LEAVE");
        systemPermissions.add("MANAGE_DEPARTMENTS");
        systemPermissions.add("VIEW_AUDIT_LOGS");
        systemPermissions.add("SYSTEM_CONFIGURATION");
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
            addAuditLog("PERMISSION_ADDED: " + permission);
        }
    }

    /**
     * Remove system permission
     */
    public void removeSystemPermission(String permission) {
        if (systemPermissions.remove(permission)) {
            addAuditLog("PERMISSION_REMOVED: " + permission);
        }
    }

    /**
     * Check if has specific permission
     */
    public boolean hasPermission(String permission) {
        return systemPermissions.contains(permission) || "SUPER_ADMIN".equals(adminLevel);
    }

    /**
     * Add audit log entry
     */
    public void addAuditLog(String log) {
        if (log != null && !log.trim().isEmpty()) {
            String timestampedLog = java.time.LocalDateTime.now() + " - " + log;
            auditLogs.add(0, timestampedLog); // Add to beginning for reverse chronological

            // Keep only last 1000 logs to prevent memory issues
            if (auditLogs.size() > 1000) {
                auditLogs = auditLogs.subList(0, 1000);
            }
        }
    }

    /**
     * Clear audit logs
     */
    public void clearAuditLogs() {
        auditLogs.clear();
        addAuditLog("AUDIT_LOGS_CLEARED");
    }

    /**
     * Get recent audit logs
     */
    public List<String> getRecentAuditLogs(int count) {
        if (count <= 0) return new ArrayList<>();
        return auditLogs.stream()
                .limit(Math.min(count, auditLogs.size()))
                .toList();
    }

    /**
     * Add managed department
     */
    public void addManagedDepartment(String department) {
        if (department != null && !department.trim().isEmpty()) {
            if (!managedDepartments.contains(department)) {
                managedDepartments.add(department);
                addAuditLog("DEPARTMENT_ADDED: " + department);
            }
        }
    }

    /**
     * Remove managed department
     */
    public void removeManagedDepartment(String department) {
        if (managedDepartments.remove(department)) {
            addAuditLog("DEPARTMENT_REMOVED: " + department);
        }
    }

    /**
     * Check if manages department
     */
    public boolean managesDepartment(String department) {
        return managedDepartments.contains(department) || canManageAllUsers;
    }

    // ========== OVERRIDDEN ABSTRACT METHODS ==========

    @Override
    public String getDepartment() { return "ADMINISTRATION"; }

    @Override
    public String getRoleName() {
        return "ADMINISTRATOR";
    }

    @Override
    public boolean canAccess(String feature) {
        // Admin can access everything
        return true;
    }

    @Override
    public DashboardType getDashboardType() {
        return DashboardType.ADMIN;
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("AdminEmployee[id=%s, name=%s, level=%s, permissions=%d]",
                getEmployeeId(), getFullName(), adminLevel, systemPermissions.size());
    }
}