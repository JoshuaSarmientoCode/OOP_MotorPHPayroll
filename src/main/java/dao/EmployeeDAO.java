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
    private List<Employee> employees;
    private final String filePath;

    public EmployeeDAO(String filePath) {
        super(filePath);
        this.filePath = filePath;
        this.employees = new ArrayList<>();
        loadData();
    }

    private void loadData() {
        this.employees = new ArrayList<>();
        Path path = Paths.get(filePath);

        if (!Files.exists(path)) {
            LOGGER.warning("Employee file not found: " + filePath);
            return;
        }

        System.out.println("========================================");
        System.out.println("Loading employees from: " + filePath);
        System.out.println("========================================");

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            int successCount = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Employee emp = parseEmployee(line);
                    if (emp != null) {
                        employees.add(emp);
                        successCount++;

                        // Debug output for verification
                        if (successCount <= 5) {
                            System.out.println("\n--- Employee " + successCount + " ---");
                            System.out.println("ID: " + emp.getEmployeeId());
                            System.out.println("Name: " + emp.getFirstName() + " " + emp.getLastName());
                            System.out.println("Basic Salary: " + emp.getBasicSalary());
                            System.out.println("Rice Subsidy: " + emp.getRiceSubsidy());
                            System.out.println("Phone Allowance: " + emp.getPhoneAllowance());
                            System.out.println("Clothing Allowance: " + emp.getClothingAllowance());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing employee at line " + lineNumber + ": " + e.getMessage());
                }
            }

            System.out.println("\n========================================");
            System.out.println("Successfully loaded " + successCount + " employees from " + filePath);
            System.out.println("========================================\n");

        } catch (IOException e) {
            System.err.println("Error reading employee file: " + e.getMessage());
        }
    }

    private Employee parseEmployee(String csvLine) {
        // Use a proper CSV parser that handles quoted fields with commas
        List<String> fields = parseCSVLineProperly(csvLine);

        if (fields.size() < 17) {
            System.err.println("Insufficient fields: expected at least 17 but got " + fields.size());
            System.err.println("Line: " + csvLine);
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
            // All salary fields need proper parsing to remove quotes and commas

            // Basic Salary (column 13)
            if (fields.size() > 13) {
                double basicSalary = parseCurrency(fields.get(13));
                emp.setBasicSalary(basicSalary);
                System.out.println("  Basic Salary raw: '" + fields.get(13) + "' -> parsed: " + basicSalary);
            }

            // Rice Subsidy (column 14)
            if (fields.size() > 14) {
                double riceSubsidy = parseCurrency(fields.get(14));
                emp.setRiceSubsidy(riceSubsidy);
                System.out.println("  Rice Subsidy raw: '" + fields.get(14) + "' -> parsed: " + riceSubsidy);
            }

            // Phone Allowance (column 15)
            if (fields.size() > 15) {
                double phoneAllowance = parseCurrency(fields.get(15));
                emp.setPhoneAllowance(phoneAllowance);
                System.out.println("  Phone Allowance raw: '" + fields.get(15) + "' -> parsed: " + phoneAllowance);
            }

            // Clothing Allowance (column 16)
            if (fields.size() > 16) {
                double clothingAllowance = parseCurrency(fields.get(16));
                emp.setClothingAllowance(clothingAllowance);
                System.out.println("  Clothing Allowance raw: '" + fields.get(16) + "' -> parsed: " + clothingAllowance);
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
            System.err.println("Error parsing employee: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Properly parse a CSV line handling quoted fields that may contain commas
     * This is the key fix for your CSV with quoted salary values
     */
    private List<String> parseCSVLineProperly(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Toggle quote state
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                // Add character to current field
                currentField.append(c);
            }
        }

        // Add the last field
        fields.add(currentField.toString());

        return fields;
    }

    /**
     * Parse currency value that may contain quotes and commas
     * Handles formats like: "90,000" -> 90000.0
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
            System.err.println("Could not parse currency value: '" + value + "'");
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

    // ========== BaseDAO IMPLEMENTATION ==========

    @Override
    public Employee fromCSV(String csvLine) {
        return parseEmployee(csvLine);
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
        fields.add(gov != null && gov.getSssNumber() != null ? gov.getSssNumber() : "");
        fields.add(gov != null && gov.getPhilHealthNumber() != null ? gov.getPhilHealthNumber() : "");
        fields.add(gov != null && gov.getTinNumber() != null ? gov.getTinNumber() : "");
        fields.add(gov != null && gov.getPagIbigNumber() != null ? gov.getPagIbigNumber() : "");

        // Status
        if (emp.getStatus() != null) {
            fields.add(emp.getStatus().toString());
        } else {
            fields.add("REGULAR");
        }

        fields.add(emp.getPosition() != null ? emp.getPosition() : "");
        fields.add(emp.getImmediateSupervisor() != null ? emp.getImmediateSupervisor() : "");

        // Format with commas for thousands and wrap in quotes to preserve commas
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getRiceSubsidy()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getPhoneAllowance()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getClothingAllowance()) + "\"");

        // Calculated fields
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary() / 2) + "\"");
        fields.add(String.format("%.2f", emp.getBasicSalary() / 168));

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

    // ========== BUSINESS METHODS ==========

    @Override
    public List<Employee> readAll() {
        return new ArrayList<>(employees);
    }

    public Employee findByEmployeeId(String employeeId) {
        return employees.stream()
                .filter(e -> e.getEmployeeId().equals(employeeId))
                .findFirst()
                .orElse(null);
    }

    public List<Employee> getAllEmployees() {
        return new ArrayList<>(employees);
    }

    public boolean addEmployee(Employee emp) {
        if (employees.stream().anyMatch(e -> e.getEmployeeId().equals(emp.getEmployeeId()))) {
            return false;
        }
        employees.add(emp);
        return true;
    }

    public boolean updateEmployee(Employee emp) {
        for (int i = 0; i < employees.size(); i++) {
            if (employees.get(i).getEmployeeId().equals(emp.getEmployeeId())) {
                employees.set(i, emp);
                return true;
            }
        }
        return false;
    }

    public boolean deleteEmployee(String employeeId) {
        return employees.removeIf(e -> e.getEmployeeId().equals(employeeId));
    }

    public String getNextEmployeeId() {
        int maxId = employees.stream()
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
        return employees.stream()
                .filter(e ->
                        e.getEmployeeId().contains(keyword) ||
                                e.getFirstName().toLowerCase().contains(lowerKeyword) ||
                                e.getLastName().toLowerCase().contains(lowerKeyword) ||
                                (e.getPosition() != null && e.getPosition().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }

    public void refreshData() {
        loadData();
    }
}