package model;

import java.time.LocalDateTime;

public class User {
    private String username;
    private String password;
    private Employee employee;
    private boolean isActive;
    private Role role;
    private LocalDateTime lastLogin;
    private LocalDateTime createdDate;

    public enum Role {
        ADMIN, HR, FINANCE, IT, EMPLOYEE
    }

    public User() {
        this.isActive = true;
        this.role = Role.EMPLOYEE;
        this.createdDate = LocalDateTime.now();
    }

    public User(String username, String password, Employee employee) {
        this.username = username;
        this.password = password;
        this.employee = employee;
        this.isActive = true;
        this.role = determineRoleFromEmployee(employee);
        this.createdDate = LocalDateTime.now();
    }

    public Role determineRoleFromEmployee(Employee emp) {
        if (emp == null) return Role.EMPLOYEE;

        System.out.println("=== DETERMINING ROLE FOR EMPLOYEE ===");
        System.out.println("Employee ID: " + emp.getEmployeeId());
        System.out.println("Employee Name: " + emp.getFullName());
        System.out.println("Employee Class: " + emp.getClass().getSimpleName());
        System.out.println("Employee Position: '" + emp.getPosition() + "'");

        // Check by employee type first (most reliable)
        if (emp instanceof AdminEmployee) {
            System.out.println("-> Found AdminEmployee class, returning ADMIN");
            return Role.ADMIN;
        } else if (emp instanceof HREmployee) {
            System.out.println("-> Found HREmployee class, returning HR");
            return Role.HR;
        } else if (emp instanceof FinanceEmployee) {
            System.out.println("-> Found FinanceEmployee class, returning FINANCE");
            return Role.FINANCE;
        } else if (emp instanceof ITEmployee) {
            System.out.println("-> Found ITEmployee class, returning IT");
            return Role.IT;
        }

        // Check by position title as fallback
        String position = emp.getPosition();
        if (position != null) {
            String pos = position.toLowerCase();

            // Check for executive/admin positions
            if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") ||
                    pos.contains("coo") || pos.contains("cmo") || pos.contains("admin") ||
                    pos.contains("executive") || pos.contains("president") ||
                    pos.contains("director") || (pos.contains("manager") && pos.contains("general"))) {
                System.out.println("-> Executive/Admin position detected, returning ADMIN");
                return Role.ADMIN;
            }

            // Check for HR positions
            if (pos.contains("hr") || pos.contains("human resources") ||
                    pos.contains("recruitment") || pos.contains("personnel")) {
                System.out.println("-> HR position detected, returning HR");
                return Role.HR;
            }

            // Check for Finance positions
            if (pos.contains("finance") || pos.contains("account") ||
                    pos.contains("payroll") || pos.contains("treasury") ||
                    pos.contains("audit") || pos.contains("bookkeeper")) {
                System.out.println("-> Finance position detected, returning FINANCE");
                return Role.FINANCE;
            }

            // Check for IT positions
            if (pos.contains("it") || pos.contains("information technology") ||
                    pos.contains("system") || pos.contains("tech") ||
                    pos.contains("developer") || pos.contains("programmer") ||
                    pos.contains("network") || pos.contains("support")) {
                System.out.println("-> IT position detected, returning IT");
                return Role.IT;
            }
        }

        System.out.println("-> No special role detected, returning EMPLOYEE");
        return Role.EMPLOYEE;
    }

    // ========== GETTERS ==========

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Employee getEmployee() { return employee; }
    public boolean isActive() { return isActive; }

    public Role getRole() {
        // If we have a stored role, use it (for CSV loading)
        if (role != null) {
            return role;
        }
        // Otherwise calculate from employee
        if (employee != null) {
            return determineRoleFromEmployee(employee);
        }
        return Role.EMPLOYEE;
    }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public LocalDateTime getCreatedDate() { return createdDate; }

    // ========== SETTERS ==========

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) {
            this.role = determineRoleFromEmployee(employee);
        }
    }

    public void setActive(boolean active) { isActive = active; }
    public void setRole(Role role) { this.role = role; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    // ========== CONVENIENCE METHODS ==========

    public String getFullName() {
        return employee != null ? employee.getFullName() : username;
    }

    public String getEmployeeId() {
        return employee != null ? employee.getEmployeeId() : null;
    }

    public String getFirstName() {
        return employee != null ? employee.getFirstName() : null;
    }

    public String getLastName() {
        return employee != null ? employee.getLastName() : null;
    }

    public String getEmail() {
        return employee != null ? employee.getEmail() : null;
    }

    public String getRoleName() {
        Role r = getRole();
        switch (r) {
            case ADMIN: return "ADMINISTRATOR";
            case HR: return "HUMAN RESOURCES";
            case FINANCE: return "FINANCE";
            case IT: return "INFORMATION TECHNOLOGY";
            default: return "EMPLOYEE";
        }
    }

    public boolean canAccess(String feature) {
        Role currentRole = getRole();

        // Debug output
        System.out.println("User " + username + " with role " + currentRole + " checking access to: " + feature);

        switch (feature) {
            // Admin can access everything
            case "EMPLOYEE_MANAGEMENT":
            case "LEAVE_MANAGEMENT":
            case "LEAVE_APPROVALS":
            case "PAYROLL_PROCESSING":
            case "VIEW_ALL_EMPLOYEES":
            case "EDIT_ALL_EMPLOYEES":
            case "DELETE_EMPLOYEES":
            case "MANAGE_USERS":
            case "VIEW_ALL_PAYROLL":
            case "VIEW_AUDIT_LOGS":
            case "SYSTEM_CONFIGURATION":
                return currentRole == Role.ADMIN;

            // HR access
            case "VIEW_EMPLOYEES":
            case "EDIT_EMPLOYEES":
            case "APPROVE_LEAVE":
                return currentRole == Role.ADMIN || currentRole == Role.HR;

            // Finance access
            case "PROCESS_PAYROLL":
            case "VIEW_PAYROLL":
            case "VIEW_REPORTS":
                return currentRole == Role.ADMIN || currentRole == Role.FINANCE;

            // IT access
            case "MANAGE_SYSTEM":
            case "VIEW_LOGS":
                return currentRole == Role.ADMIN || currentRole == Role.IT;

            // Employee access (everyone)
            case "VIEW_OWN_RECORDS":
            case "SUBMIT_LEAVE":
            case "VIEW_OWN_PAYSLIP":
            case "TIME_TRACKING":
                return true;

            default:
                return false;
        }
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("User[username=%s, role=%s, active=%s, employeeId=%s]",
                username, getRole(), isActive, getEmployeeId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}