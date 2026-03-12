package model;

import model.Employee.*;

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

        // Check by employee class first — most reliable
        if (emp instanceof AdminEmployee)   return Role.ADMIN;
        if (emp instanceof HREmployee)      return Role.HR;
        if (emp instanceof FinanceEmployee) return Role.FINANCE;
        if (emp instanceof ITEmployee)      return Role.IT;

        // Fallback: check by position title from CSV
        String position = emp.getPosition();
        if (position == null || position.trim().isEmpty()) return Role.EMPLOYEE;

        String pos = position.toLowerCase().trim();

        // ── ADMIN positions ──
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") ||
                pos.contains("coo") || pos.contains("cmo") ||
                pos.contains("executive") || pos.contains("president") ||
                pos.contains("director") ||
                (pos.contains("manager") && pos.contains("general"))) {
            return Role.ADMIN;
        }

        // ── HR positions ──
        if (pos.contains("human resources") || pos.contains(" hr ") ||
                pos.startsWith("hr ") || pos.endsWith(" hr") ||
                pos.contains("recruitment") || pos.contains("personnel") ||
                pos.contains("hiring") || pos.contains("talent")) {
            return Role.HR;
        }

        // ── FINANCE positions ──

        if (pos.contains("accounting") || pos.contains("payroll") ||
                pos.contains("finance") || pos.contains("treasury") ||
                pos.contains("audit") || pos.contains("bookkeeper") ||
                pos.contains("budget") || pos.contains("controller")) {
            return Role.FINANCE;
        }

        // ── IT positions ──
        if (pos.contains("information technology") ||
                pos.contains("developer") || pos.contains("programmer") ||
                pos.contains("network") || pos.contains("system admin") ||
                pos.contains("database") || pos.contains("cybersecurity") ||
                pos.contains("helpdesk") || pos.contains("help desk") ||
                pos.contains("it support") || pos.contains("tech support")) {
            return Role.IT;
        }

        return Role.EMPLOYEE;
    }

    // ========== GETTERS ==========

    public String getUsername()  { return username; }
    public String getPassword()  { return password; }
    public Employee getEmployee(){ return employee; }
    public boolean isActive()    { return isActive; }

    public Role getRole() {
        if (role != null) return role;
        if (employee != null) return determineRoleFromEmployee(employee);
        return Role.EMPLOYEE;
    }

    public LocalDateTime getLastLogin()   { return lastLogin; }
    public LocalDateTime getCreatedDate() { return createdDate; }

    // ========== SETTERS ==========

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }

    public void setEmployee(Employee employee) {
        this.employee = employee;
        if (employee != null) this.role = determineRoleFromEmployee(employee);
    }

    public void setActive(boolean active)              { isActive = active; }
    public void setRole(Role role)                     { this.role = role; }
    public void setLastLogin(LocalDateTime lastLogin)  { this.lastLogin = lastLogin; }
    public void setCreatedDate(LocalDateTime created)  { this.createdDate = created; }

    // ========== CONVENIENCE ==========

    public String getFullName()   { return employee != null ? employee.getFullName() : username; }
    public String getEmployeeId() { return employee != null ? employee.getEmployeeId() : null; }
    public String getFirstName()  { return employee != null ? employee.getFirstName() : null; }
    public String getLastName()   { return employee != null ? employee.getLastName()  : null; }
    public String getEmail()      { return employee != null ? employee.getEmail()     : null; }

    public String getRoleName() {
        switch (getRole()) {
            case ADMIN:   return "ADMINISTRATOR";
            case HR:      return "HUMAN RESOURCES";
            case FINANCE: return "FINANCE";
            case IT:      return "INFORMATION TECHNOLOGY";
            default:      return "EMPLOYEE";
        }
    }

    public boolean canAccess(String feature) {
        Role currentRole = getRole();

        switch (feature) {

            // Admin-only features
            case "LEAVE_MANAGEMENT":
            case "VIEW_ALL_PAYROLL":
            case "EDIT_ALL_EMPLOYEES":
            case "DELETE_EMPLOYEES":
            case "MANAGE_USERS":
            case "VIEW_AUDIT_LOGS":
            case "SYSTEM_CONFIGURATION":
                return currentRole == Role.ADMIN;

            // Employee Management
            case "EMPLOYEE_MANAGEMENT":
            case "VIEW_ALL_EMPLOYEES":
                return currentRole == Role.ADMIN || currentRole == Role.HR;

            // Payroll Processing
            case "PAYROLL_PROCESSING":
            case "PROCESS_PAYROLL":
            case "VIEW_PAYROLL":
            case "VIEW_REPORTS":
            case "FINANCIAL_DATA":
                return currentRole == Role.ADMIN || currentRole == Role.FINANCE;

            // HR features
            case "VIEW_EMPLOYEES":
            case "EDIT_EMPLOYEES":
            case "APPROVE_LEAVE":
            case "LEAVE_APPROVALS":
            case "EMPLOYEE_RECORDS":
            case "VIEW_EMPLOYEE_DETAILS":
                return currentRole == Role.ADMIN || currentRole == Role.HR;

            // IT features
            case "MANAGE_SYSTEM":
            case "VIEW_LOGS":
            case "USER_ACCOUNTS":
                return currentRole == Role.ADMIN || currentRole == Role.IT;

            // All logged-in users
            case "VIEW_OWN_RECORDS":
            case "SUBMIT_LEAVE":
            case "VIEW_OWN_PAYSLIP":
            case "TIME_TRACKING":
                return true;

            default:
                return false;
        }
    }

    // ========== OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("User[username=%s, role=%s, active=%s, employeeId=%s]",
                username, getRole(), isActive, getEmployeeId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return username.equals(((User) o).username);
    }

    @Override
    public int hashCode() { return username.hashCode(); }
}