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
    
    private Role determineRoleFromEmployee(Employee emp) {
        if (emp == null) return Role.EMPLOYEE;
        if (emp instanceof AdminEmployee) return Role.ADMIN;
        if (emp instanceof HREmployee) return Role.HR;
        if (emp instanceof FinanceEmployee) return Role.FINANCE;
        if (emp instanceof ITEmployee) return Role.IT;
        return Role.EMPLOYEE;
    }
    
    // ========== GETTERS ==========
    
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Employee getEmployee() { return employee; }
    public boolean isActive() { return isActive; }
    public Role getRole() { 
        if (employee != null) {
            return determineRoleFromEmployee(employee);
        }
        return role; 
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
        if (employee != null) return employee.getRoleName();
        return role.toString();
    }
    
    public boolean canAccess(String feature) {
        return employee != null && employee.canAccess(feature);
    }
    
    // ========== OVERRIDDEN OBJECT METHODS ==========
    
    @Override
    public String toString() {
        return String.format("User[username=%s, role=%s, active=%s]",
                username, getRole(), isActive);
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