package dao;

import model.Payroll;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

public class PayrollDAO extends BaseDAO<Payroll> {
    
    private static final String[] HEADERS = {
        "Payroll ID", "Employee ID", "Employee Name", "Period", "Basic Salary",
        "Rice Subsidy", "Phone Allowance", "Clothing Allowance", "Gross Salary",
        "SSS", "PhilHealth", "Pag-IBIG", "Tax", "Total Deductions", "Net Salary",
        "Hours Worked", "Present Days", "Leave Days", "Overtime Hours",
        "Generated Date", "Status", "Department", "Position"
    };
    
    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public PayrollDAO(String filePath) {
        super(filePath);
    }
    
    @Override
    public Payroll fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 15) return null;
        
        Payroll payroll = new Payroll();
        
        try {
            payroll.setPayrollId(safeGet(data, 0));
            payroll.setEmployeeId(safeGet(data, 1));
            payroll.setEmployeeName(safeGet(data, 2));
            
            String periodStr = safeGet(data, 3);
            if (!periodStr.isEmpty()) {
                payroll.setPayrollPeriod(YearMonth.parse(periodStr, PERIOD_FORMATTER));
            }
            
            payroll.setBasicSalary(parseDouble(safeGet(data, 4)));
            payroll.setRiceSubsidy(parseDouble(safeGet(data, 5)));
            payroll.setPhoneAllowance(parseDouble(safeGet(data, 6)));
            payroll.setClothingAllowance(parseDouble(safeGet(data, 7)));
            payroll.setGrossSalary(parseDouble(safeGet(data, 8)));
            payroll.setSssDeduction(parseDouble(safeGet(data, 9)));
            payroll.setPhilHealthDeduction(parseDouble(safeGet(data, 10)));
            payroll.setPagIbigDeduction(parseDouble(safeGet(data, 11)));
            payroll.setTaxDeduction(parseDouble(safeGet(data, 12)));
            payroll.setTotalDeductions(parseDouble(safeGet(data, 13)));
            payroll.setNetSalary(parseDouble(safeGet(data, 14)));
            
            if (data.length > 15) payroll.setTotalHoursWorked(parseDouble(safeGet(data, 15)));
            if (data.length > 16) payroll.setPresentDays(parseInt(safeGet(data, 16)));
            if (data.length > 17) payroll.setTotalLeaveDays(parseInt(safeGet(data, 17)));
            if (data.length > 18) payroll.setOvertimeHours(parseDouble(safeGet(data, 18)));
            
            if (data.length > 19 && !safeGet(data, 19).isEmpty()) {
                payroll.setGeneratedDate(LocalDate.parse(safeGet(data, 19), DATE_FORMATTER));
            }
            
            if (data.length > 20) payroll.setStatus(safeGet(data, 20));
            if (data.length > 21) payroll.setDepartment(safeGet(data, 21));
            if (data.length > 22) payroll.setPosition(safeGet(data, 22));
            
        } catch (Exception e) {
            LOGGER.warning("Error parsing payroll: " + e.getMessage());
            return null;
        }
        
        return payroll;
    }
    
    @Override
    public String toCSV(Payroll payroll) {
        List<String> fields = new ArrayList<>();
        
        fields.add(payroll.getPayrollId());
        fields.add(payroll.getEmployeeId());
        fields.add(payroll.getEmployeeName());
        fields.add(payroll.getPayrollPeriod() != null ? payroll.getPayrollPeriod().format(PERIOD_FORMATTER) : "");
        fields.add(String.valueOf(payroll.getBasicSalary()));
        fields.add(String.valueOf(payroll.getRiceSubsidy()));
        fields.add(String.valueOf(payroll.getPhoneAllowance()));
        fields.add(String.valueOf(payroll.getClothingAllowance()));
        fields.add(String.valueOf(payroll.getGrossSalary()));
        fields.add(String.valueOf(payroll.getSssDeduction()));
        fields.add(String.valueOf(payroll.getPhilHealthDeduction()));
        fields.add(String.valueOf(payroll.getPagIbigDeduction()));
        fields.add(String.valueOf(payroll.getTaxDeduction()));
        fields.add(String.valueOf(payroll.getTotalDeductions()));
        fields.add(String.valueOf(payroll.getNetSalary()));
        fields.add(String.valueOf(payroll.getTotalHoursWorked()));
        fields.add(String.valueOf(payroll.getPresentDays()));
        fields.add(String.valueOf(payroll.getTotalLeaveDays()));
        fields.add(String.valueOf(payroll.getOvertimeHours()));
        fields.add(payroll.getGeneratedDate() != null ? payroll.getGeneratedDate().format(DATE_FORMATTER) : "");
        fields.add(payroll.getStatus() != null ? payroll.getStatus() : "");
        fields.add(payroll.getDepartment() != null ? payroll.getDepartment() : "");
        fields.add(payroll.getPosition() != null ? payroll.getPosition() : "");
        
        return String.join(",", fields);
    }
    
    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }
    
    @Override
    protected String getId(Payroll item) {
        return item.getPayrollId();
    }
    
    private String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }
    
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    private int parseInt(String value) {
        if (value == null || value.trim().isEmpty()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
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
    
    public List<Payroll> findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(p -> p.getEmployeeId().equals(employeeId))
                .sorted((p1, p2) -> {
                    if (p1.getPayrollPeriod() == null) return 1;
                    if (p2.getPayrollPeriod() == null) return -1;
                    return p2.getPayrollPeriod().compareTo(p1.getPayrollPeriod());
                })
                .collect(Collectors.toList());
    }
    
    public List<Payroll> findByPeriod(YearMonth period) {
        return cache.stream()
                .filter(p -> p.getPayrollPeriod() != null && p.getPayrollPeriod().equals(period))
                .collect(Collectors.toList());
    }
    
    public Payroll findByEmployeeAndPeriod(String employeeId, YearMonth period) {
        return cache.stream()
                .filter(p -> p.getEmployeeId().equals(employeeId))
                .filter(p -> p.getPayrollPeriod() != null && p.getPayrollPeriod().equals(period))
                .findFirst()
                .orElse(null);
    }
    
    public boolean addPayroll(Payroll payroll) {
        if (payroll == null) return false;
        
        // Check for duplicate ID
        boolean exists = cache.stream()
                .anyMatch(p -> p.getPayrollId().equals(payroll.getPayrollId()));
        
        if (exists) {
            LOGGER.warning("Payroll with ID " + payroll.getPayrollId() + " already exists");
            return false;
        }
        
        // Check for duplicate employee + period
        boolean duplicatePeriod = cache.stream()
                .anyMatch(p -> p.getEmployeeId().equals(payroll.getEmployeeId()) &&
                               p.getPayrollPeriod() != null &&
                               p.getPayrollPeriod().equals(payroll.getPayrollPeriod()));
        
        if (duplicatePeriod) {
            LOGGER.warning("Payroll already exists for employee " + payroll.getEmployeeId() + 
                          " for period " + payroll.getPayrollPeriod());
            return false;
        }
        
        return add(payroll);
    }
    
    public Payroll findById(String id) {
        return cache.stream()
                .filter(p -> p.getPayrollId().equals(id))
                .findFirst()
                .orElse(null);
    }
}