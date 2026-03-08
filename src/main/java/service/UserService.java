package service;

import dao.*;
import model.*;
import java.util.*;

public class UserService {

    private final UserDAO userDAO;
    private final EmployeeDAO employeeDAO;
    private final ValidationService validator;
    private User currentUser;

    public UserService(UserDAO userDAO, EmployeeDAO employeeDAO, ValidationService validator) {
        this.userDAO = userDAO;
        this.employeeDAO = employeeDAO;
        this.validator = validator;

        // Link the DAOs
        this.userDAO.setEmployeeDAO(this.employeeDAO);

        // Create default admin if no users exist
        createDefaultAdminIfNeeded();
    }

    public boolean login(String username, String password) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);

        ValidationService.ValidationResult result = validator.validateLogin(username, password);
        if (!result.isValid()) {
            System.out.println("Validation failed: " + result.getErrorMessage());
            return false;
        }

        // Try to find existing user
        User user = userDAO.findByUsername(username);
        System.out.println("Existing user found: " + (user != null));

        if (user != null) {
            System.out.println("User role from CSV: " + user.getRole());
            System.out.println("User has employee: " + (user.getEmployee() != null));
            if (user.getEmployee() != null) {
                System.out.println("Employee name: " + user.getEmployee().getFullName());
                System.out.println("Employee ID: " + user.getEmployee().getEmployeeId());
                System.out.println("Employee class: " + user.getEmployee().getClass().getSimpleName());
            } else {
                System.out.println("WARNING: User has no employee object! Attempting to reload...");
                // Try to reload employee
                Employee emp = employeeDAO.findByEmployeeId(username);
                if (emp != null) {
                    user.setEmployee(emp);
                    System.out.println("Reloaded employee: " + emp.getFullName());
                }
            }
            System.out.println("Password match: " + user.getPassword().equals(password));
            System.out.println("User active: " + user.isActive());

            if (user.getPassword().equals(password) && user.isActive()) {
                currentUser = user;
                System.out.println("Login successful for: " + username);
                System.out.println("Final user role: " + currentUser.getRole());
                System.out.println("Final employee ID: " + currentUser.getEmployeeId());
                return true;
            }
        }

        // If username is a valid employee ID (5 digits), auto-create
        if (username.matches("\\d{5}")) {
            return autoCreateUser(username, password);
        }

        System.out.println("Login failed for: " + username);
        return false;
    }

    private boolean autoCreateUser(String employeeId, String password) {
        System.out.println("Attempting to auto-create user for employee ID: " + employeeId);

        // Check if employee exists
        Employee emp = employeeDAO.findByEmployeeId(employeeId);
        if (emp == null) {
            System.out.println("Employee not found: " + employeeId);
            return false;
        }

        System.out.println("Employee found: " + emp.getFirstName() + " " + emp.getLastName());
        System.out.println("Employee type: " + emp.getClass().getSimpleName());
        System.out.println("Employee position: " + emp.getPosition());

        // Validate password format - should be "emp" + last digit of employee ID
        String lastDigit = employeeId.substring(employeeId.length() - 1);
        String expectedPassword = "emp" + lastDigit;

        System.out.println("Expected password format: " + expectedPassword);
        System.out.println("Provided password: " + password);

        if (!expectedPassword.equals(password)) {
            System.out.println("Invalid password format. Expected: " + expectedPassword);
            return false;
        }

        // Check if user already exists
        User existing = userDAO.findByUsername(employeeId);
        if (existing != null) {
            System.out.println("User already exists for this employee");
            return false;
        }

        // Create new user
        User newUser = new User();
        newUser.setUsername(employeeId);
        newUser.setPassword(password);
        newUser.setEmployee(emp);
        newUser.setActive(true);

        boolean added = userDAO.addUser(newUser);
        if (added) {
            System.out.println("User auto-created for employee: " + employeeId);
            System.out.println("User saved with role: " + newUser.getRole());
            currentUser = newUser;
            return true;
        }

        return false;
    }

    private void createDefaultAdminIfNeeded() {
        if (userDAO.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword("admin123");
            admin.setActive(true);
            admin.setRole(User.Role.ADMIN);

            boolean added = userDAO.addUser(admin);
            if (added) {
                System.out.println("Default admin user created with role: ADMIN");
            }
        }
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        // Ensure current user has employee object
        if (currentUser != null && currentUser.getEmployee() == null) {
            System.out.println("Current user has no employee, attempting to reload...");
            Employee emp = employeeDAO.findByEmployeeId(currentUser.getUsername());
            if (emp != null) {
                currentUser.setEmployee(emp);
                System.out.println("Reloaded employee: " + emp.getFullName());
            }
        }
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean hasAccess(String feature) {
        if (currentUser == null) return false;
        boolean access = currentUser.canAccess(feature);
        System.out.println("User " + currentUser.getUsername() + " (role: " + currentUser.getRole() +
                ") access to " + feature + ": " + access);
        return access;
    }

    public List<User> getAllUsers() {
        return userDAO.getAllUsers();
    }

    public boolean addUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        return userDAO.addUser(user);
    }

    public boolean updateUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        return userDAO.updateUser(user);
    }

    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public User getUserByEmployeeId(String employeeId) {
        return userDAO.findByEmployeeId(employeeId);
    }
}