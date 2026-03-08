package dao;

import model.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeDAO extends BaseDAO<Employee> {
    
    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Birthday", "Address", 
        "Phone Number", "SSS #", "Philhealth #", "TIN #", "Pag-ibig #",
        "Status", "Position", "Immediate Supervisor", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance",
        "Gross Semi-monthly Rate", "Hourly Rate"
    };
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    
    public EmployeeDAO(String filePath) {
        super(filePath);
    }
    
    @Override
    public Employee fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 14) return null;
        
        try {
            String employeeId = safeGet(data, 0);
            String lastName = safeGet(data, 1);
            String firstName = safeGet(data, 2);
            String position = safeGet(data, 11);
            String status = safeGet(data, 10);
            
            // Create appropriate employee type
            Employee emp = createEmployeeByType(position, status);
            
            emp.setEmployeeId(employeeId);
            emp.setLastName(lastName);
            emp.setFirstName(firstName);
            
            // Birthday
            String birthdayStr = safeGet(data, 3);
            if (!birthdayStr.isEmpty() && !birthdayStr.equals("N/A")) {
                try {
                    emp.setBirthDate(LocalDate.parse(birthdayStr, DATE_FORMATTER));
                } catch (Exception e) {
                    // Ignore birthday parsing errors
                }
            }
            
            // Address
            String address = safeGet(data, 4);
            if (address.startsWith("\"") && address.endsWith("\"")) {
                address = address.substring(1, address.length() - 1);
            }
            emp.setAddress(address);
            
            // Phone - keep as is
            emp.setPhoneNumber(safeGet(data, 5));
            
            // Government IDs - store as is without validation
            GovernmentIds govIds = new GovernmentIds();
            govIds.setSssNumber(safeGet(data, 6));
            govIds.setPhilHealthNumber(safeGet(data, 7));
            govIds.setTinNumber(safeGet(data, 8));
            govIds.setPagIbigNumber(safeGet(data, 9));
            emp.setGovernmentIds(govIds);
            
            emp.setStatus(status);
            emp.setPosition(position);
            
            // Supervisor
            String supervisor = safeGet(data, 12);
            if (!supervisor.isEmpty() && !supervisor.equals("N/A")) {
                emp.setImmediateSupervisor(supervisor);
            }
            
            // Salary and allowances
            emp.setBasicSalary(parseDouble(safeGet(data, 13)));
            emp.setRiceSubsidy(parseDouble(safeGet(data, 14)));
            emp.setPhoneAllowance(parseDouble(safeGet(data, 15)));
            emp.setClothingAllowance(parseDouble(safeGet(data, 16)));
            
            // Generate email
            emp.setEmail(generateEmail(firstName, lastName));
            
            // Set hire date
            if (emp.getBirthDate() != null) {
                emp.setHireDate(emp.getBirthDate().plusYears(20));
            } else {
                emp.setHireDate(LocalDate.now().minusYears(1));
            }
            
            return emp;
            
        } catch (Exception e) {
            LOGGER.warning("Error parsing employee: " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public String toCSV(Employee emp) {
        List<String> fields = new ArrayList<>();
        
        fields.add(emp.getEmployeeId());
        fields.add(emp.getLastName());
        fields.add(emp.getFirstName());
        fields.add(emp.getBirthDate() != null ? emp.getBirthDate().format(DATE_FORMATTER) : "");
        fields.add("\"" + (emp.getAddress() != null ? emp.getAddress() : "") + "\"");
        fields.add(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "");
        
        GovernmentIds gov = emp.getGovernmentIds();
        fields.add(gov != null ? gov.getSssNumber() : "");
        fields.add(gov != null ? gov.getPhilHealthNumber() : "");
        fields.add(gov != null ? gov.getTinNumber() : "");
        fields.add(gov != null ? gov.getPagIbigNumber() : "");
        
        fields.add(emp.getStatus() != null ? emp.getStatus().toString() : "REGULAR");
        fields.add(emp.getPosition() != null ? emp.getPosition() : "");
        fields.add(emp.getImmediateSupervisor() != null ? emp.getImmediateSupervisor() : "");
        
        fields.add(formatCurrency(emp.getBasicSalary()));
        fields.add(formatCurrency(emp.getRiceSubsidy()));
        fields.add(formatCurrency(emp.getPhoneAllowance()));
        fields.add(formatCurrency(emp.getClothingAllowance()));
        
        // Calculated fields
        fields.add(formatCurrency(emp.getBasicSalary() / 2)); // Gross Semi-monthly
        fields.add(String.format("%.2f", emp.getBasicSalary() / 168)); // Hourly Rate
        
        return String.join(",", fields);
    }
    
    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }
    
    @Override
    protected String getId(Employee item) {
        return item.getEmployeeId();
    }
    
    // ========== HELPER METHODS ==========
    
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
    
    private String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }
    
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private String formatCurrency(double amount) {
        return String.format("%,.0f", amount);
    }
    
    private String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) return null;
        String cleanFirst = firstName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        String cleanLast = lastName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        return cleanFirst + "." + cleanLast + "@motorph.com";
    }
    
    private Employee createEmployeeByType(String position, String status) {
        if (position == null) return new RegularEmployee();
        
        String pos = position.toLowerCase();
        
        if (status != null && status.toLowerCase().contains("probationary")) {
            return new ProbationaryEmployee();
        }
        
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") || 
            pos.contains("coo") || pos.contains("cmo") || pos.contains("admin")) {
            return new AdminEmployee();
        } else if (pos.contains("hr")) {
            return new HREmployee();
        } else if (pos.contains("finance") || pos.contains("account") || pos.contains("payroll")) {
            return new FinanceEmployee();
        } else if (pos.contains("it") || pos.contains("information technology") || 
                   pos.contains("system") || pos.contains("tech")) {
            return new ITEmployee();
        } else {
            return new RegularEmployee();
        }
    }
    
    // ========== BUSINESS METHODS ==========
    
    public Employee findByEmployeeId(String employeeId) {
        return findById(employeeId);
    }
    
    public List<Employee> getAllEmployees() {
        return readAll();
    }
    
    public boolean addEmployee(Employee employee) {
        return add(employee);
    }
    
    public boolean updateEmployee(Employee employee) {
        return update(employee);
    }
    
    public boolean deleteEmployee(String employeeId) {
        return delete(employeeId);
    }
    
    public String getNextEmployeeId() {
        int maxId = cache.stream()
                .map(e -> Integer.parseInt(e.getEmployeeId()))
                .max(Integer::compareTo)
                .orElse(10000);
        return String.format("%05d", maxId + 1);
    }
    
    public List<Employee> searchEmployees(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return cache.stream()
                .filter(e -> 
                    e.getEmployeeId().contains(keyword) ||
                    e.getFirstName().toLowerCase().contains(lowerKeyword) ||
                    e.getLastName().toLowerCase().contains(lowerKeyword) ||
                    (e.getPosition() != null && e.getPosition().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
}