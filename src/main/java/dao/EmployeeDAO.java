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
            // Debug: Print field count
            System.out.println("Parsing employee line with " + fields.size() + " fields");

            // Extract fields by index - MATCHING YOUR CSV STRUCTURE
            String employeeId = fields.get(0).trim();           // Employee #
            String lastName = fields.get(1).trim();             // Last Name
            String firstName = fields.get(2).trim();            // First Name
            String birthday = fields.get(3).trim();             // Birthday
            String address = fields.get(4).trim();              // Address
            String phoneNumber = fields.get(5).trim();          // Phone Number
            String sss = fields.get(6).trim();                  // SSS #
            String philhealth = fields.get(7).trim();           // Philhealth #
            String tin = fields.get(8).trim();                  // TIN #
            String pagibig = fields.get(9).trim();              // Pag-ibig #
            String status = fields.get(10).trim();              // Status
            String position = fields.get(11).trim();            // Position
            String supervisor = fields.get(12).trim();          // Immediate Supervisor

            // Salary fields - THESE ARE THE ONLY ONES THAT SHOULD GO TO parseCurrency
            String basicSalaryStr = fields.size() > 13 ? fields.get(13).trim() : "0";
            String riceSubsidyStr = fields.size() > 14 ? fields.get(14).trim() : "0";
            String phoneAllowanceStr = fields.size() > 15 ? fields.get(15).trim() : "0";
            String clothingAllowanceStr = fields.size() > 16 ? fields.get(16).trim() : "0";

            // These are calculated fields - we can ignore them when reading
            // String grossSemiMonthlyStr = fields.size() > 17 ? fields.get(17).trim() : "0";
            // String hourlyRateStr = fields.size() > 18 ? fields.get(18).trim() : "0";

            // Create appropriate employee type based on position and status
            Employee emp = createEmployeeByPositionAndStatus(position, status);

            // Set basic info
            emp.setEmployeeId(employeeId);
            emp.setLastName(lastName);
            emp.setFirstName(firstName);

            // Birthday
            if (!birthday.isEmpty() && !birthday.equals("N/A")) {
                try {
                    emp.setBirthDate(LocalDate.parse(birthday, DATE_FORMATTER));
                } catch (Exception e) {
                    LOGGER.warning("Could not parse birthday: " + birthday);
                }
            }

            // Address (remove quotes if present)
            if (address.startsWith("\"") && address.endsWith("\"")) {
                address = address.substring(1, address.length() - 1);
            }
            emp.setAddress(address);

            // Phone
            emp.setPhoneNumber(phoneNumber);

            // Government IDs
            GovernmentIds govIds = new GovernmentIds();
            govIds.setSssNumber(sss);
            govIds.setPhilHealthNumber(philhealth);
            govIds.setTinNumber(tin);
            govIds.setPagIbigNumber(pagibig);
            emp.setGovernmentIds(govIds);

            // Status and Position
            emp.setStatus(status);
            emp.setPosition(position);

            // Supervisor
            if (!supervisor.equals("N/A") && !supervisor.isEmpty()) {
                emp.setImmediateSupervisor(supervisor);
            }

            // ========== SALARY FIELDS PARSING ==========
            // Only parse these fields - they are the numeric ones
            if (!basicSalaryStr.isEmpty() && !basicSalaryStr.equals("N/A")) {
                double basicSalary = parseCurrency(basicSalaryStr);
                emp.setBasicSalary(basicSalary);
            }

            if (!riceSubsidyStr.isEmpty() && !riceSubsidyStr.equals("N/A")) {
                double riceSubsidy = parseCurrency(riceSubsidyStr);
                emp.setRiceSubsidy(riceSubsidy);
            }

            if (!phoneAllowanceStr.isEmpty() && !phoneAllowanceStr.equals("N/A")) {
                double phoneAllowance = parseCurrency(phoneAllowanceStr);
                emp.setPhoneAllowance(phoneAllowance);
            }

            if (!clothingAllowanceStr.isEmpty() && !clothingAllowanceStr.equals("N/A")) {
                double clothingAllowance = parseCurrency(clothingAllowanceStr);
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

            System.out.println("Successfully parsed employee: " + employeeId + " - " + firstName + " " + lastName);
            return emp;

        } catch (Exception e) {
            LOGGER.warning("Error parsing employee: " + e.getMessage());
            e.printStackTrace();
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
     * ONLY call this on actual numeric fields
     */
    private double parseCurrency(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("N/A")) {
            return 0.0;
        }

        try {
            // Remove quotes if present
            String withoutQuotes = value.replace("\"", "");

            // Remove commas (thousands separators)
            String withoutCommas = withoutQuotes.replace(",", "");

            // Remove any currency symbols
            String withoutSymbols = withoutCommas.replace("₱", "").replace("PHP", "");

            // Trim any whitespace
            String cleanValue = withoutSymbols.trim();

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

        // Check for executive/admin positions
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") ||
                pos.contains("coo") || pos.contains("cmo") || pos.contains("admin") ||
                pos.contains("executive") || pos.contains("president") ||
                pos.contains("director") || (pos.contains("manager") && pos.contains("general"))) {
            return new AdminEmployee();
        }

        // Check for HR positions
        if (pos.contains("hr") || pos.contains("human resources") ||
                pos.contains("recruitment") || pos.contains("personnel")) {
            return new HREmployee();
        }

        // Check for Finance positions
        if (pos.contains("finance") || pos.contains("account") ||
                pos.contains("payroll") || pos.contains("treasury") ||
                pos.contains("audit") || pos.contains("bookkeeper")) {
            return new FinanceEmployee();
        }

        // Check for IT positions
        if (pos.contains("it") || pos.contains("information technology") ||
                pos.contains("system") || pos.contains("tech") ||
                pos.contains("developer") || pos.contains("programmer") ||
                pos.contains("network") || pos.contains("support")) {
            return new ITEmployee();
        }

        return new RegularEmployee();
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
        System.out.println("EmployeeDAO.delete called for ID: " + id);
        boolean result = super.delete(id);
        System.out.println("Delete result: " + result);
        return result;
    }

    public boolean deleteEmployee(String employeeId) {
        System.out.println("deleteEmployee called for ID: " + employeeId);
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