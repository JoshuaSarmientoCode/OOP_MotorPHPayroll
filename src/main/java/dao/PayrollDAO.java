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
            "Generated Date", "Status"
    };

    private static final DateTimeFormatter PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PayrollDAO(String filePath) { super(filePath); }

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
            if (!periodStr.isEmpty()) payroll.setPayrollPeriod(YearMonth.parse(periodStr, PERIOD_FORMATTER));

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
            if (data.length > 19 && !safeGet(data, 19).isEmpty())
                payroll.setGeneratedDate(LocalDate.parse(safeGet(data, 19), DATE_FORMATTER));
            if (data.length > 20) payroll.setStatus(safeGet(data, 20));

        } catch (Exception e) {
            LOGGER.warning("Error parsing payroll: " + e.getMessage());
            return null;
        }
        return payroll;
    }

    @Override
    public String toCSV(Payroll payroll) {
        return String.join(",",
                payroll.getPayrollId(),
                payroll.getEmployeeId(),
                payroll.getEmployeeName(),
                payroll.getPayrollPeriod() != null ? payroll.getPayrollPeriod().format(PERIOD_FORMATTER) : "",
                String.valueOf(payroll.getBasicSalary()),
                String.valueOf(payroll.getRiceSubsidy()),
                String.valueOf(payroll.getPhoneAllowance()),
                String.valueOf(payroll.getClothingAllowance()),
                String.valueOf(payroll.getGrossSalary()),
                String.valueOf(payroll.getSssDeduction()),
                String.valueOf(payroll.getPhilHealthDeduction()),
                String.valueOf(payroll.getPagIbigDeduction()),
                String.valueOf(payroll.getTaxDeduction()),
                String.valueOf(payroll.getTotalDeductions()),
                String.valueOf(payroll.getNetSalary()),
                String.valueOf(payroll.getTotalHoursWorked()),
                String.valueOf(payroll.getPresentDays()),
                String.valueOf(payroll.getTotalLeaveDays()),
                String.valueOf(payroll.getOvertimeHours()),
                payroll.getGeneratedDate() != null ? payroll.getGeneratedDate().format(DATE_FORMATTER) : "",
                payroll.getStatus() != null ? payroll.getStatus() : ""
        );
    }

    @Override
    protected String[] getHeaders() { return HEADERS; }

    @Override
    protected String getId(Payroll item) { return item.getPayrollId(); }

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
                .findFirst().orElse(null);
    }

    public boolean addPayroll(Payroll payroll) { return add(payroll); }
}