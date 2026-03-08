package dao;

import model.*;
import java.util.*;
import java.util.stream.Collectors;

public class UserDAO extends BaseDAO<User> {
    
    private static final String[] HEADERS = {
        "Username", "Password", "Employee ID", "Is Active"
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
        String[] data = csvLine.split(",", -1);
        if (data.length < 3) return null;
        
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
            String.valueOf(user.isActive())
        );
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
        if (exists(user.getUsername())) {
            return false;
        }
        return add(user);
    }
    
    public boolean updateUser(User user) {
        return update(user);
    }
}