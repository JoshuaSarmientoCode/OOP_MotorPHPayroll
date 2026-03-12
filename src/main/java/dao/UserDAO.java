package dao;

import model.User;
import model.Employee.Employee;
import java.util.*;
import java.util.logging.Logger;

public class UserDAO extends BaseDAO<User> {
    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());


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
        if (data.length < 4) return null;

        User user = new User();

        try {
            user.setUsername(safeGet(data, 0));
            user.setPassword(safeGet(data, 1));

            // Set employee if we have employeeDAO and employee ID
            String empId = safeGet(data, 2);
            if (!empId.isEmpty() && employeeDAO != null) {
                Employee emp = employeeDAO.findByEmployeeId(empId);
                if (emp != null) {
                    user.setEmployee(emp);
                    LOGGER.fine("Loaded employee for user " + user.getUsername() + ": " + emp.getFullName());
                } else {
                    LOGGER.fine("Warning: Employee not found for ID: " + empId);
                }
            }

            // Parse Is Active
            String activeStr = safeGet(data, 3);
            user.setActive(!"false".equalsIgnoreCase(activeStr));

            // Parse Role if present
            if (data.length > 4 && !safeGet(data, 4).isEmpty()) {
                String roleStr = safeGet(data, 4);
                try {
                    User.Role role = User.Role.valueOf(roleStr.toUpperCase());
                    user.setRole(role);
                    LOGGER.fine("Loaded user " + user.getUsername() + " with stored role: " + role);
                } catch (IllegalArgumentException e) {
                    LOGGER.fine("Invalid role string in CSV: " + roleStr);
                }
            }

        } catch (Exception e) {
            LOGGER.warning("Error parsing user: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return user;
    }

    @Override
    public String toCSV(User user) {
        List<String> fields = new ArrayList<>();

        fields.add(user.getUsername());
        fields.add(user.getPassword());
        fields.add(user.getEmployeeId() != null ? user.getEmployeeId() : "");
        fields.add(String.valueOf(user.isActive()));
        fields.add(user.getRole() != null ? user.getRole().toString() : "EMPLOYEE");

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
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        LOGGER.fine("Adding user: " + user.getUsername() + " with role: " + user.getRole());
        return add(user);
    }

    public boolean updateUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null) {
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        }
        return update(user);
    }
}