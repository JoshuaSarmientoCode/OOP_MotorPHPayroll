package service;

import dao.*;
import model.*;
import java.util.*;
import java.util.logging.Logger;

public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    private final UserDAO userDAO;
    private final EmployeeDAO employeeDAO;
    private final ValidationService validator;
    private User currentUser;

    public UserService(UserDAO userDAO, EmployeeDAO employeeDAO, ValidationService validator) {
        this.userDAO = userDAO;
        this.employeeDAO = employeeDAO;
        this.validator = validator;
        this.userDAO.setEmployeeDAO(this.employeeDAO);
        createDefaultAdminIfNeeded();
    }

    // ========== AUTHENTICATION ==========

    public boolean login(String username, String password) {
        ValidationService.ValidationResult result = validator.validateLogin(username, password);
        if (!result.isValid()) return false;

        User user = userDAO.findByUsername(username);

        if (user != null) {
            if (user.getEmployee() == null) {
                Employee emp = employeeDAO.findByEmployeeId(username);
                if (emp != null) user.setEmployee(emp);
            }
            if (user.getPassword().equals(password) && user.isActive()) {
                currentUser = user;
                LOGGER.info("Login successful for: " + username);
                return true;
            }
        }

        // Auto-create user for employees logging in for the first time
        if (username.matches("\\d{5}")) {
            return autoCreateUser(username, password);
        }

        LOGGER.warning("Login failed for: " + username);
        return false;
    }

    private boolean autoCreateUser(String employeeId, String password) {
        Employee emp = employeeDAO.findByEmployeeId(employeeId);
        if (emp == null) return false;

        String lastTwoDigits = employeeId.substring(employeeId.length() - 2);
        String expectedPassword = "emp" + lastTwoDigits;

        if (!expectedPassword.equals(password)) return false;

        User existing = userDAO.findByUsername(employeeId);
        if (existing != null) return false;

        User newUser = new User();
        newUser.setUsername(employeeId);
        newUser.setPassword(password);
        newUser.setEmployee(emp);
        newUser.setActive(true);

        boolean added = userDAO.addUser(newUser);
        if (added) {
            currentUser = newUser;
            LOGGER.info("Auto-created user for employee: " + employeeId);
        }
        return added;
    }

    private void createDefaultAdminIfNeeded() {
        if (userDAO.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setActive(true);
            admin.setRole(User.Role.ADMIN);
            userDAO.addUser(admin);
            LOGGER.info("Default admin user created");
        }
    }

    public void logout() {
        currentUser = null;
    }

    public boolean changePassword(User user, String newPassword) {
        if (user == null || newPassword == null || newPassword.trim().isEmpty()) return false;

        // No strength validation here — this overload is for system/admin resets
        // where the password format is controlled (e.g. forgot password reset)
        user.setPassword(newPassword);
        boolean updated = userDAO.updateUser(user);
        if (updated) LOGGER.info("Password reset by admin for user: " + user.getUsername());
        return updated;
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (username == null || oldPassword == null || newPassword == null) return false;

        User user = userDAO.findByUsername(username);
        if (user == null) return false;

        // Verify old password matches
        if (!user.getPassword().equals(oldPassword)) {
            LOGGER.warning("Incorrect old password for user: " + username);
            return false;
        }

        // New password must be different from current
        if (oldPassword.equals(newPassword)) {
            LOGGER.warning("New password same as old for user: " + username);
            return false;
        }

        ValidationService.ValidationResult result = validator.validatePassword(newPassword);
        if (!result.isValid()) {
            LOGGER.warning("Password validation failed for user: " + username
                    + " — " + result.getErrorMessage());
            return false;
        }

        user.setPassword(newPassword);
        boolean updated = userDAO.updateUser(user);

        if (updated) {
            // Keep the in-memory currentUser in sync if it's the same user
            if (currentUser != null && currentUser.getUsername().equals(username)) {
                currentUser.setPassword(newPassword);
            }
            LOGGER.info("Password changed successfully for user: " + username);
        }
        return updated;
    }

    // ========== GETTERS / SESSION ==========

    public User getCurrentUser() {
        if (currentUser != null && currentUser.getEmployee() == null) {
            Employee emp = employeeDAO.findByEmployeeId(currentUser.getUsername());
            if (emp != null) currentUser.setEmployee(emp);
        }
        return currentUser;
    }

    public boolean isLoggedIn() { return currentUser != null; }

    public boolean hasAccess(String feature) {
        if (currentUser == null) return false;
        return currentUser.canAccess(feature);
    }

    public List<User> getAllUsers() { return userDAO.getAllUsers(); }

    public boolean addUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null)
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        return userDAO.addUser(user);
    }

    public boolean updateUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null)
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        return userDAO.updateUser(user);
    }

    public User getUserByUsername(String username) { return userDAO.findByUsername(username); }
    public User getUserByEmployeeId(String employeeId) { return userDAO.findByEmployeeId(employeeId); }
}