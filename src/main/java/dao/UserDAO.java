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
        if (data.length < 4) return null;

        User user = new User();
        try {
            user.setUsername(safeGet(data, 0));
            user.setPassword(safeGet(data, 1));

            String empId = safeGet(data, 2);
            if (!empId.isEmpty() && employeeDAO != null) {
                Employee emp = employeeDAO.findByEmployeeId(empId);
                if (emp != null) user.setEmployee(emp);
            }

            user.setActive(!"false".equalsIgnoreCase(safeGet(data, 3)));

            if (data.length > 4 && !safeGet(data, 4).isEmpty()) {
                try {
                    user.setRole(User.Role.valueOf(safeGet(data, 4).toUpperCase()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warning("Invalid role in CSV: " + safeGet(data, 4));
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
        return String.join(",",
                user.getUsername(),
                user.getPassword(),
                user.getEmployeeId() != null ? user.getEmployeeId() : "",
                String.valueOf(user.isActive()),
                user.getRole() != null ? user.getRole().toString() : "EMPLOYEE"
        );
    }

    @Override
    protected String[] getHeaders() { return HEADERS; }

    @Override
    protected String getId(User item) { return item.getUsername(); }

    public User findByUsername(String username) { return findById(username); }

    public User findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(u -> employeeId.equals(u.getEmployeeId()))
                .findFirst().orElse(null);
    }

    public List<User> getAllUsers() { return readAll(); }

    public boolean addUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null)
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        return add(user);
    }

    public boolean updateUser(User user) {
        if (user.getRole() == null && user.getEmployee() != null)
            user.setRole(user.determineRoleFromEmployee(user.getEmployee()));
        return update(user);
    }
}