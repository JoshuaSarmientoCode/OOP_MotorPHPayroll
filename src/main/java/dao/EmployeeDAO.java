package dao;

import model.*;
import java.io.*;
import java.nio.file.*;
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
        // Use a proper CSV parser that handles quoted fields with commas
        List<String> fields = parseCSVLineProperly(csvLine);

        if (fields.size() < 17) {
            LOGGER.warning("Insufficient fields: expected at least 17 but got " + fields.size());
            return null;
        }

        try {
            // Basic info
            String employeeId = fields.get(0).trim();
            String lastName = fields.get(1).trim();
            String firstName = fields.get(2).trim();
            String position = fields.size() > 11 ? fields.get(11).trim() : "";
            String statusStr = fields.size() > 10 ? fields.get(10).trim() : "Regular";

            // Create appropriate employee type
            Employee emp = createEmployeeByPositionAndStatus(position, statusStr);

            emp.setEmployeeId(employeeId);
            emp.setLastName(lastName);
            emp.setFirstName(firstName);

            // Birthday
            if (fields.size() > 3 && !fields.get(3).trim().isEmpty() && !fields.get(3).trim().equals("N/A")) {
                try {
                    emp.setBirthDate(LocalDate.parse(fields.get(3).trim(), DATE_FORMATTER));
                } catch (Exception e) {
                    // Ignore birthday parsing errors
                }
            }

            // Address
            String address = fields.size() > 4 ? fields.get(4).trim() : "";
            emp.setAddress(address);

            // Phone
            String phone = fields.size() > 5 ? fields.get(5).trim() : "";
            emp.setPhoneNumber(phone);

            // Government IDs
            GovernmentIds govIds = new GovernmentIds();
            if (fields.size() > 6) govIds.setSssNumber(fields.get(6).trim());
            if (fields.size() > 7) govIds.setPhilHealthNumber(fields.get(7).trim());
            if (fields.size() > 8) govIds.setTinNumber(fields.get(8).trim());
            if (fields.size() > 9) govIds.setPagIbigNumber(fields.get(9).trim());
            emp.setGovernmentIds(govIds);

            // Status and Position
            emp.setStatus(statusStr);
            emp.setPosition(position);

            // Supervisor
            if (fields.size() > 12) {
                String supervisor = fields.get(12).trim();
                if (!supervisor.equals("N/A") && !supervisor.isEmpty()) {
                    emp.setImmediateSupervisor(supervisor);
                }
            }

            // ========== SALARY FIELDS PARSING ==========
            // Basic Salary (column 13)
            if (fields.size() > 13) {
                double basicSalary = parseCurrency(fields.get(13));
                emp.setBasicSalary(basicSalary);
            }

            // Rice Subsidy (column 14)
            if (fields.size() > 14) {
                double riceSubsidy = parseCurrency(fields.get(14));
                emp.setRiceSubsidy(riceSubsidy);
            }

            // Phone Allowance (column 15)
            if (fields.size() > 15) {
                double phoneAllowance = parseCurrency(fields.get(15));
                emp.setPhoneAllowance(phoneAllowance);
            }

            // Clothing Allowance (column 16)
            if (fields.size() > 16) {
                double clothingAllowance = parseCurrency(fields.get(16));
                emp.setClothingAllowance(clothingAllowance);
            }

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

        // Column 0: Employee #
        fields.add(emp.getEmployeeId());

        // Column 1: Last Name
        fields.add(emp.getLastName());

        // Column 2: First Name
        fields.add(emp.getFirstName());

        // Column 3: Birthday
        fields.add(emp.getBirthDate() != null ? emp.getBirthDate().format(DATE_FORMATTER) : "");

        // Column 4: Address (with quotes)
        fields.add("\"" + (emp.getAddress() != null ? emp.getAddress() : "") + "\"");

        // Column 5: Phone Number
        fields.add(emp.getPhoneNumber() != null ? emp.getPhoneNumber() : "");

        // Column 6: SSS #
        GovernmentIds gov = emp.getGovernmentIds();
        fields.add(gov != null && gov.getSssNumber() != null ? gov.getSssNumber() : "");

        // Column 7: Philhealth #
        fields.add(gov != null && gov.getPhilHealthNumber() != null ? gov.getPhilHealthNumber() : "");

        // Column 8: TIN #
        fields.add(gov != null && gov.getTinNumber() != null ? gov.getTinNumber() : "");

        // Column 9: Pag-ibig #
        fields.add(gov != null && gov.getPagIbigNumber() != null ? gov.getPagIbigNumber() : "");

        // Column 10: Status
        fields.add(emp.getStatus() != null ? emp.getStatus().toString() : "REGULAR");

        // Column 11: Position
        fields.add(emp.getPosition() != null ? emp.getPosition() : "");

        // Column 12: Immediate Supervisor
        fields.add(emp.getImmediateSupervisor() != null ? emp.getImmediateSupervisor() : "");

        // Column 13: Basic Salary (with quotes and commas)
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary()) + "\"");

        // Column 14: Rice Subsidy (with quotes and commas)
        fields.add("\"" + String.format("%,.0f", emp.getRiceSubsidy()) + "\"");

        // Column 15: Phone Allowance (with quotes and commas)
        fields.add("\"" + String.format("%,.0f", emp.getPhoneAllowance()) + "\"");

        // Column 16: Clothing Allowance (with quotes and commas)
        fields.add("\"" + String.format("%,.0f", emp.getClothingAllowance()) + "\"");

        // Column 17: Gross Semi-monthly Rate (calculated, with quotes and commas)
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary() / 2) + "\"");

        // Column 18: Hourly Rate (calculated)
        fields.add(String.format("%.2f", emp.getBasicSalary() / 168));

        // Return as a single line without any newline characters
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

    /**
     * Properly parse a CSV line handling quoted fields that may contain commas
     */
    private List<String> parseCSVLineProperly(String line) {
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

        // Add the last field
        fields.add(currentField.toString());

        return fields;
    }

    /**
     * Parse currency value that may contain quotes and commas
     */
    private double parseCurrency(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Remove quotes if present
            String withoutQuotes = value.replace("\"", "");

            // Remove commas (thousands separators)
            String withoutCommas = withoutQuotes.replace(",", "");

            // Trim any whitespace
            String cleanValue = withoutCommas.trim();

            if (cleanValue.isEmpty()) {
                return 0.0;
            }

            return Double.parseDouble(cleanValue);

        } catch (NumberFormatException e) {
            LOGGER.warning("Could not parse currency value: '" + value + "'");
            return 0.0;
        }
    }

    /**
     * Creates the appropriate employee type based on position and status
     */
    private Employee createEmployeeByPositionAndStatus(String position, String status) {
        if (position == null || position.isEmpty()) return new RegularEmployee();

        String pos = position.toLowerCase();
        String stat = status != null ? status.toLowerCase() : "";

        // Check if probationary first
        if (stat.contains("probationary")) {
            return new ProbationaryEmployee();
        }

        // Role-based creation
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

    /**
     * Generates an email address from first and last name
     */
    private String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) return null;
        String cleanFirst = firstName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        String cleanLast = lastName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        return cleanFirst + "." + cleanLast + "@motorph.com";
    }

    // ========== BUSINESS METHODS ==========

    public Employee findByEmployeeId(String employeeId) {
        return findById(employeeId);
    }

    public List<Employee> getAllEmployees() {
        return readAll();
    }

    @Override
    public boolean add(Employee employee) {
        return super.add(employee);
    }

    public boolean addEmployee(Employee emp) {
        return add(emp);
    }

    @Override
    public boolean update(Employee employee) {
        return super.update(employee);
    }

    public boolean updateEmployee(Employee emp) {
        return update(emp);
    }

    @Override
    public boolean delete(String id) {
        return super.delete(id);
    }

    public boolean deleteEmployee(String employeeId) {
        return delete(employeeId);
    }

    public String getNextEmployeeId() {
        int maxId = cache.stream()
                .map(e -> {
                    try {
                        return Integer.parseInt(e.getEmployeeId());
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
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