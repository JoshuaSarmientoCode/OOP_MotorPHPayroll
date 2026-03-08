package dao;

import model.User;
import model.Employee;
import java.util.*;
import java.util.stream.Collectors;

public class UserDAO extends BaseDAO<User> {

    private static final String[] HEADERS = {
            "Username", "Password", "Employee ID", "Is Active", "Role"
    };

    private EmployeeDAO employeeDAO;

    public UserDAO(String filePath) {
        super(filePath);
    }

    public void setEmployeeDAO(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @Override
    public User fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 4) return null; // Now need at least 4 fields (Role is optional for backward compatibility)

        User user = new User();

        try {
            user.setUsername(safeGet(data, 0));
            user.setPassword(safeGet(data, 1));

            // Set employee if we have employeeDAO and employee ID
            String empId = safeGet(data, 2);
            if (!empId.isEmpty() && employeeDAO != null) {
                Employee emp = employeeDAO.findByEmployeeId(empId);
                user.setEmployee(emp);
            }

            // Parse Is Active
            String activeStr = safeGet(data, 3);
            user.setActive(!"false".equalsIgnoreCase(activeStr));

            // Parse Role if present (for backward compatibility)
            if (data.length > 4 && !safeGet(data, 4).isEmpty()) {
                String roleStr = safeGet(data, 4);
                try {
                    User.Role role = User.Role.valueOf(roleStr.toUpperCase());
                    user.setRole(role);
                    System.out.println("Loaded user " + user.getUsername() + " with stored role: " + role);
                } catch (IllegalArgumentException e) {
                    // If role string is invalid, ignore and let it be determined from employee
                    System.out.println("Invalid role string in CSV: " + roleStr + ", will determine from employee");
                }
            } else {
                // If no role in CSV, determine from employee
                if (user.getEmployee() != null) {
                    User.Role determinedRole = user.determineRoleFromEmployee(user.getEmployee());
                    user.setRole(determinedRole);
                    System.out.println("Determined role for user " + user.getUsername() + ": " + determinedRole);
                }
            }

        } catch (Exception e) {
            LOGGER.warning("Error parsing user: " + e.getMessage());
            return null;
        }

        return user;
    }

    @Override
    public String toCSV(User user) {
        List<String> fields = new ArrayList<>();

        // Username
        fields.add(user.getUsername());

        // Password
        fields.add(user.getPassword());

        // Employee ID
        fields.add(user.getEmployeeId() != null ? user.getEmployeeId() : "");

        // Is Active
        fields.add(String.valueOf(user.isActive()));

        // Role - CRITICAL: Store the role in CSV
        User.Role role = user.getRole();
        fields.add(role != null ? role.toString() : "EMPLOYEE");

        System.out.println("Writing user to CSV: " + user.getUsername() + " with role: " + role);

        // Return as a single line without any newline characters
        return String.join(",", fields);
    }

    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }

    @Override
    protected String getId(User item) {
        return item.getUsername();
    }

    private String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }

    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    // ========== BUSINESS METHODS ==========

    public User findByUsername(String username) {
        return findById(username);
    }

    public User findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(u -> employeeId.equals(u.getEmployeeId()))
                .findFirst()
                .orElse(null);
    }

    public List<User> getAllUsers() {
        return readAll();
    }

    public boolean addUser(User user) {
        // Ensure role is set before saving
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        System.out.println("Adding user: " + user.getUsername() + " with role: " + user.getRole());
        return add(user);
    }

    public boolean updateUser(User user) {
        // Ensure role is set before updating
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        System.out.println("Updating user: " + user.getUsername() + " with role: " + user.getRole());
        return update(user);
    }

    @Override
    public boolean add(User item) {
        // Override to add debug info
        boolean result = super.add(item);
        if (result) {
            System.out.println("User successfully added to cache and file");
        } else {
            System.out.println("Failed to add user");
        }
        return result;
    }
}