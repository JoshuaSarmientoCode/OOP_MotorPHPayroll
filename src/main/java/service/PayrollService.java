package service;

import dao.*;
import model.*;
import service.deductions.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class PayrollService {

    private final PayrollDAO payrollDAO;
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;

    // Deduction services
    private final SSSDeduction sssDeduction;
    private final PhilHealthDeduction philHealthDeduction;
    private final PagIbigDeduction pagIbigDeduction;
    private final TaxDeduction taxDeduction;

    // Constants
    private static final int STANDARD_WORKING_DAYS_PER_MONTH = 22;
    private static final double STANDARD_WORKING_HOURS_PER_DAY = 8.0;
    private static final double STANDARD_WORKING_HOURS_PER_MONTH =
            STANDARD_WORKING_DAYS_PER_MONTH * STANDARD_WORKING_HOURS_PER_DAY;

    public PayrollService(PayrollDAO payrollDAO, EmployeeDAO employeeDAO,
                          AttendanceDAO attendanceDAO) {
        this.payrollDAO = payrollDAO;
        this.employeeDAO = employeeDAO;
        this.attendanceDAO = attendanceDAO;

        this.sssDeduction = new SSSDeduction();
        this.philHealthDeduction = new PhilHealthDeduction();
        this.pagIbigDeduction = new PagIbigDeduction();
        this.taxDeduction = new TaxDeduction();
    }

    /**
     * Generate a payslip for an employee for a specific period
     */
    public Payslip generatePayslip(String employeeId, YearMonth period) {
        Employee emp = employeeDAO.findByEmployeeId(employeeId);
        if (emp == null) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }

        // Get attendance for the period
        LocalDate startDate = period.atDay(1);
        LocalDate endDate = period.atEndOfMonth();
        List<Attendance> attendance = attendanceDAO.findByEmployeeAndDateRange(employeeId, startDate, endDate);

        // Calculate attendance statistics
        double totalHoursWorked = 0;
        int daysPresent = 0;
        double totalOvertime = 0;

        for (Attendance a : attendance) {
            if (a.getTimeOut() != null) {
                totalHoursWorked += a.getHoursWorked();
                totalOvertime += a.getOvertimeHours();
                daysPresent++;
            }
        }

        // Calculate attendance multiplier (proration)
        double attendanceMultiplier;
        if (totalHoursWorked >= STANDARD_WORKING_HOURS_PER_MONTH) {
            attendanceMultiplier = 1.0;
        } else if (totalHoursWorked > 0) {
            attendanceMultiplier = totalHoursWorked / STANDARD_WORKING_HOURS_PER_MONTH;
        } else {
            attendanceMultiplier = 0.0;
        }

        // Calculate prorated basic salary
        double proratedBasicSalary = emp.getBasicSalary() * attendanceMultiplier;

        // Calculate allowances (full regardless of attendance)
        double riceSubsidy = emp.getRiceSubsidy();
        double phoneAllowance = emp.getPhoneAllowance();
        double clothingAllowance = emp.getClothingAllowance();
        double totalAllowance = riceSubsidy + phoneAllowance + clothingAllowance;

        // Calculate overtime pay (25% premium)
        double overtimePay = totalOvertime * (emp.getHourlyRate() * 1.25);

        double grossSalary = proratedBasicSalary + totalAllowance + overtimePay;

        // Calculate deductions
        double sss = sssDeduction.calculate(proratedBasicSalary);
        double philHealth = philHealthDeduction.calculate(proratedBasicSalary);
        double pagIbig = pagIbigDeduction.calculate(proratedBasicSalary);

        double taxableIncome = grossSalary - sss - philHealth - pagIbig;
        double tax = taxDeduction.calculate(taxableIncome);

        double totalDeductions = sss + philHealth + pagIbig + tax;
        double netPay = grossSalary - totalDeductions;

        // Create payslip
        Payslip payslip = new Payslip();
        payslip.setPayslipId(generatePayslipId());
        payslip.setEmployeeId(employeeId);
        payslip.setEmployeeName(emp.getFullName());
        payslip.setPeriod(period);
        payslip.setGeneratedDate(LocalDate.now());

        payslip.setBasicSalary(proratedBasicSalary);
        payslip.setRiceSubsidy(riceSubsidy);
        payslip.setPhoneAllowance(phoneAllowance);
        payslip.setClothingAllowance(clothingAllowance);
        payslip.setTotalAllowance(totalAllowance);
        payslip.setGrossBasic(proratedBasicSalary);
        payslip.setGrossSalary(grossSalary);
        payslip.setOvertimePay(overtimePay);

        payslip.setSss(sss);
        payslip.setPhilhealth(philHealth);
        payslip.setPagibig(pagIbig);
        payslip.setTax(tax);
        payslip.setTotalDeductions(totalDeductions);
        payslip.setNetPay(netPay);

        // Attendance data
        payslip.setTotalHoursWorked(totalHoursWorked);
        payslip.setPresentDays(daysPresent);
        payslip.setAbsentDays(STANDARD_WORKING_DAYS_PER_MONTH - daysPresent);
        payslip.setOvertimeHours(totalOvertime);

        return payslip;
    }

    /**
     * Process payroll for all employees for a specific period
     */
    public List<Payslip> processPayroll(YearMonth period) {
        List<Employee> allEmployees = employeeDAO.getAllEmployees();
        List<Payslip> payslips = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Employee emp : allEmployees) {
            try {
                // Check if already processed
                if (hasPayroll(emp.getEmployeeId(), period)) {
                    System.out.println("Payroll already exists for: " + emp.getEmployeeId());
                    continue;
                }

                Payslip payslip = generatePayslip(emp.getEmployeeId(), period);
                payslips.add(payslip);

                // Save to payroll history
                savePayslipToHistory(payslip);

            } catch (Exception e) {
                String error = "Error processing payroll for " + emp.getEmployeeId() + ": " + e.getMessage();
                System.err.println(error);
                errors.add(error);
            }
        }

        System.out.println("Payroll processed: " + payslips.size() + " successful, " + errors.size() + " errors");
        return payslips;
    }

    /**
     * Save payslip to history (creates a Payroll record)
     */
    private void savePayslipToHistory(Payslip payslip) {
        Payroll payroll = convertToPayroll(payslip);
        boolean success = payrollDAO.addPayroll(payroll);
        if (success) {
            System.out.println("Payroll saved to history: " + payslip.getPayslipId());
        } else {
            System.err.println("Failed to save payroll to history: " + payslip.getPayslipId());
        }
    }

    /**
     * Get employee's payslip history
     */
    public List<Payslip> getEmployeePayslips(String employeeId) {
        List<Payroll> payrolls = payrollDAO.findByEmployeeId(employeeId);
        List<Payslip> payslips = new ArrayList<>();

        for (Payroll p : payrolls) {
            payslips.add(convertToPayslip(p));
        }

        // Sort by period descending (newest first)
        payslips.sort((p1, p2) -> p2.getPeriod().compareTo(p1.getPeriod()));

        return payslips;
    }

    /**
     * Get payslip by ID
     */
    public Payslip getPayslip(String payslipId) {
        Payroll payroll = payrollDAO.findById(payslipId);
        return payroll != null ? convertToPayslip(payroll) : null;
    }

    /**
     * Get payroll status for period (for UI display)
     */
    public Map<String, Object> getPayrollStatus(YearMonth period) {
        List<Payroll> processed = payrollDAO.findByPeriod(period);
        List<Employee> allEmployees = employeeDAO.getAllEmployees();

        Map<String, Object> status = new HashMap<>();
        status.put("period", period);
        status.put("totalEmployees", allEmployees.size());
        status.put("processedCount", processed.size());
        status.put("pendingCount", allEmployees.size() - processed.size());

        // Calculate totals from processed payrolls
        double totalGrossSalary = 0.0;
        double totalDeductions = 0.0;
        double totalNetSalary = 0.0;

        for (Payroll p : processed) {
            totalGrossSalary += p.getGrossSalary();
            totalDeductions += p.getTotalDeductions();
            totalNetSalary += p.getNetSalary();
        }

        status.put("totalGrossSalary", totalGrossSalary);
        status.put("totalDeductions", totalDeductions);
        status.put("totalNetSalary", totalNetSalary);

        return status;
    }

    /**
     * Check if payroll exists for employee and period
     */
    public boolean hasPayroll(String employeeId, YearMonth period) {
        Payroll payroll = payrollDAO.findByEmployeeAndPeriod(employeeId, period);
        return payroll != null;
    }

    /**
     * Get payroll statistics for period
     */
    public Map<String, Object> getPayrollStatistics(YearMonth period) {
        List<Payroll> processed = payrollDAO.findByPeriod(period);

        Map<String, Object> stats = new HashMap<>();
        stats.put("period", period);
        stats.put("totalProcessed", processed.size());

        double totalNet = processed.stream().mapToDouble(Payroll::getNetSalary).sum();
        stats.put("totalNetPay", totalNet);

        return stats;
    }

    // ========== CONVERSION METHODS ==========

    private String generatePayslipId() {
        return "PS" + System.currentTimeMillis() + String.format("%04d", new Random().nextInt(10000));
    }

    private Payslip convertToPayslip(Payroll p) {
        Payslip ps = new Payslip();
        ps.setPayslipId(p.getPayrollId());
        ps.setEmployeeId(p.getEmployeeId());
        ps.setEmployeeName(p.getEmployeeName());
        ps.setPeriod(p.getPayrollPeriod());
        ps.setGeneratedDate(p.getGeneratedDate());

        ps.setBasicSalary(p.getBasicSalary());
        ps.setRiceSubsidy(p.getRiceSubsidy());
        ps.setPhoneAllowance(p.getPhoneAllowance());
        ps.setClothingAllowance(p.getClothingAllowance());
        ps.setTotalAllowance(p.getRiceSubsidy() + p.getPhoneAllowance() + p.getClothingAllowance());
        ps.setGrossBasic(p.getBasicSalary());
        ps.setGrossSalary(p.getGrossSalary());

        ps.setSss(p.getSssDeduction());
        ps.setPhilhealth(p.getPhilHealthDeduction());
        ps.setPagibig(p.getPagIbigDeduction());
        ps.setTax(p.getTaxDeduction());
        ps.setTotalDeductions(p.getTotalDeductions());
        ps.setNetPay(p.getNetSalary());

        ps.setTotalHoursWorked(p.getTotalHoursWorked());
        ps.setPresentDays(p.getPresentDays());
        ps.setOvertimeHours(p.getOvertimeHours());

        return ps;
    }

    private Payroll convertToPayroll(Payslip ps) {
        Payroll payroll = new Payroll();
        payroll.setPayrollId(ps.getPayslipId());
        payroll.setEmployeeId(ps.getEmployeeId());
        payroll.setEmployeeName(ps.getEmployeeName());
        payroll.setPayrollPeriod(ps.getPeriod());
        payroll.setBasicSalary(ps.getBasicSalary());
        payroll.setRiceSubsidy(ps.getRiceSubsidy());
        payroll.setPhoneAllowance(ps.getPhoneAllowance());
        payroll.setClothingAllowance(ps.getClothingAllowance());
        payroll.setGrossSalary(ps.getGrossSalary());
        payroll.setSssDeduction(ps.getSss());
        payroll.setPhilHealthDeduction(ps.getPhilhealth());
        payroll.setPagIbigDeduction(ps.getPagibig());
        payroll.setTaxDeduction(ps.getTax());
        payroll.setTotalDeductions(ps.getTotalDeductions());
        payroll.setNetSalary(ps.getNetPay());
        payroll.setTotalHoursWorked(ps.getTotalHoursWorked());
        payroll.setPresentDays(ps.getPresentDays());
        payroll.setOvertimeHours(ps.getOvertimeHours());
        payroll.setGeneratedDate(ps.getGeneratedDate());
        payroll.setStatus("PROCESSED");

        // Get employee for department and position
        Employee emp = employeeDAO.findByEmployeeId(ps.getEmployeeId());
        if (emp != null) {
            payroll.setDepartment(emp.getDepartment());
            payroll.setPosition(emp.getPosition());
        }

        return payroll;
    }
}