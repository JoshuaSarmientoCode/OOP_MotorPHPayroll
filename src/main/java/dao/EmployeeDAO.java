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
        List<String> fields = new ArrayList<>(Arrays.asList(parseCSVLine(csvLine)));
        if (fields.size() < 17) {
            LOGGER.warning("Insufficient fields: expected at least 17 but got " + fields.size());
            return null;
        }

        try {
            String employeeId = fields.get(0).trim();
            String lastName = fields.get(1).trim();
            String firstName = fields.get(2).trim();
            String birthday = fields.get(3).trim();
            String address = fields.get(4).trim();
            String phoneNumber = fields.get(5).trim();
            String sss = fields.get(6).trim();
            String philhealth = fields.get(7).trim();
            String tin = fields.get(8).trim();
            String pagibig = fields.get(9).trim();
            String status = fields.get(10).trim();
            String position = fields.get(11).trim();
            String supervisor = fields.get(12).trim();

            String basicSalaryStr = fields.size() > 13 ? fields.get(13).trim() : "0";
            String riceSubsidyStr = fields.size() > 14 ? fields.get(14).trim() : "0";
            String phoneAllowanceStr = fields.size() > 15 ? fields.get(15).trim() : "0";
            String clothingAllowanceStr = fields.size() > 16 ? fields.get(16).trim() : "0";

            Employee emp = createEmployeeByPositionAndStatus(position, status);
            emp.setEmployeeId(employeeId);
            emp.setLastName(lastName);
            emp.setFirstName(firstName);

            if (!birthday.isEmpty() && !birthday.equals("N/A")) {
                try { emp.setBirthDate(LocalDate.parse(birthday, DATE_FORMATTER)); }
                catch (Exception e) { LOGGER.warning("Could not parse birthday: " + birthday); }
            }

            if (address.startsWith("\"") && address.endsWith("\""))
                address = address.substring(1, address.length() - 1);
            emp.setAddress(address);
            emp.setPhoneNumber(phoneNumber);

            GovernmentIds govIds = new GovernmentIds();
            govIds.setSssNumber(sss);
            govIds.setPhilHealthNumber(philhealth);
            govIds.setTinNumber(tin);
            govIds.setPagIbigNumber(pagibig);
            emp.setGovernmentIds(govIds);

            emp.setStatus(status);
            emp.setPosition(position);
            if (!supervisor.equals("N/A") && !supervisor.isEmpty())
                emp.setImmediateSupervisor(supervisor);

            if (!basicSalaryStr.isEmpty() && !basicSalaryStr.equals("N/A"))
                emp.setBasicSalary(parseCurrency(basicSalaryStr));
            if (!riceSubsidyStr.isEmpty() && !riceSubsidyStr.equals("N/A"))
                emp.setRiceSubsidy(parseCurrency(riceSubsidyStr));
            if (!phoneAllowanceStr.isEmpty() && !phoneAllowanceStr.equals("N/A"))
                emp.setPhoneAllowance(parseCurrency(phoneAllowanceStr));
            if (!clothingAllowanceStr.isEmpty() && !clothingAllowanceStr.equals("N/A"))
                emp.setClothingAllowance(parseCurrency(clothingAllowanceStr));

            emp.setEmail(generateEmail(firstName, lastName));
            emp.setHireDate(emp.getBirthDate() != null
                    ? emp.getBirthDate().plusYears(20)
                    : LocalDate.now().minusYears(1));

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
        fields.add(gov != null && gov.getSssNumber() != null ? gov.getSssNumber() : "");
        fields.add(gov != null && gov.getPhilHealthNumber() != null ? gov.getPhilHealthNumber() : "");
        fields.add(gov != null && gov.getTinNumber() != null ? gov.getTinNumber() : "");
        fields.add(gov != null && gov.getPagIbigNumber() != null ? gov.getPagIbigNumber() : "");
        fields.add(emp.getStatus() != null ? emp.getStatus().toString() : "REGULAR");
        fields.add(emp.getPosition() != null ? emp.getPosition() : "");
        fields.add(emp.getImmediateSupervisor() != null ? emp.getImmediateSupervisor() : "");
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getRiceSubsidy()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getPhoneAllowance()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getClothingAllowance()) + "\"");
        fields.add("\"" + String.format("%,.0f", emp.getBasicSalary() / 2) + "\"");
        fields.add(String.format("%.2f", emp.getBasicSalary() / 168));
        return String.join(",", fields);
    }

    @Override
    protected String[] getHeaders() { return HEADERS; }

    @Override
    protected String getId(Employee item) { return item.getEmployeeId(); }

    private double parseCurrency(String value) {
        if (value == null || value.trim().isEmpty() || value.equals("N/A")) return 0.0;
        try {
            return Double.parseDouble(
                    value.replace("\"", "").replace(",", "").replace("₱", "").replace("PHP", "").trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Could not parse currency value: '" + value + "'");
            return 0.0;
        }
    }

    private Employee createEmployeeByPositionAndStatus(String position, String status) {
        if (position == null || position.isEmpty()) return new RegularEmployee();
        String pos = position.toLowerCase();
        String stat = status != null ? status.toLowerCase() : "";

        if (stat.contains("probationary")) return new ProbationaryEmployee();
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") ||
                pos.contains("coo") || pos.contains("cmo") || pos.contains("admin") ||
                pos.contains("executive") || pos.contains("president") ||
                pos.contains("director") || (pos.contains("manager") && pos.contains("general")))
            return new AdminEmployee();
        if (pos.contains("hr") || pos.contains("human resources") ||
                pos.contains("recruitment") || pos.contains("personnel"))
            return new HREmployee();
        if (pos.contains("finance") || pos.contains("account") ||
                pos.contains("payroll") || pos.contains("treasury") ||
                pos.contains("audit") || pos.contains("bookkeeper"))
            return new FinanceEmployee();
        if (pos.contains("it") || pos.contains("information technology") ||
                pos.contains("system") || pos.contains("tech") ||
                pos.contains("developer") || pos.contains("programmer") ||
                pos.contains("network") || pos.contains("support"))
            return new ITEmployee();
        return new RegularEmployee();
    }

    private String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) return null;
        String cleanFirst = firstName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        String cleanLast = lastName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        return cleanFirst + "." + cleanLast + "@motorph.com";
    }

    public Employee findByEmployeeId(String employeeId) { return findById(employeeId); }

    public List<Employee> getAllEmployees() { return readAll(); }

    @Override
    public boolean add(Employee employee) { return super.add(employee); }

    public boolean addEmployee(Employee emp) { return add(emp); }

    @Override
    public boolean update(Employee employee) { return super.update(employee); }

    public boolean updateEmployee(Employee emp) { return update(emp); }

    @Override
    public boolean delete(String id) { return super.delete(id); }

    public boolean deleteEmployee(String employeeId) { return delete(employeeId); }

    public String getNextEmployeeId() {
        int maxId = cache.stream()
                .map(e -> { try { return Integer.parseInt(e.getEmployeeId()); } catch (NumberFormatException ex) { return 0; } })
                .max(Integer::compareTo).orElse(10000);
        return String.format("%05d", maxId + 1);
    }

    public List<Employee> searchEmployees(String keyword) {
        String lowerKeyword = keyword.toLowerCase();
        return cache.stream()
                .filter(e -> e.getEmployeeId().contains(keyword) ||
                        e.getFirstName().toLowerCase().contains(lowerKeyword) ||
                        e.getLastName().toLowerCase().contains(lowerKeyword) ||
                        (e.getPosition() != null && e.getPosition().toLowerCase().contains(lowerKeyword)))
                .collect(Collectors.toList());
    }
}