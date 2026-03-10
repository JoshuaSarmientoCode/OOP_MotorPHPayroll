package model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class Payslip {
    private String payslipId;
    private String employeeId;
    private String employeeName;
    private YearMonth period;
    private LocalDate generatedDate;

    // Earnings
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double totalAllowance;
    private double grossBasic;
    private double grossSalary;
    private double overtimePay;

    // Deductions
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;
    private double totalDeductions;

    // Net Pay
    private double netPay;

    // Attendance data
    private double totalHoursWorked;
    private int presentDays;
    private int absentDays;
    private double overtimeHours;
    private double lateHours;

    public Payslip() {
        this.generatedDate = LocalDate.now();
    }

    // ========== GETTERS ==========

    public String getPayslipId() { return payslipId; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public YearMonth getPeriod() { return period; }
    public LocalDate getGeneratedDate() { return generatedDate; }

    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getTotalAllowance() { return totalAllowance; }
    public double getGrossBasic() { return grossBasic; }
    public double getGrossSalary() { return grossSalary; }
    public double getOvertimePay() { return overtimePay; }

    public double getSss() { return sss; }
    public double getPhilhealth() { return philhealth; }
    public double getPagibig() { return pagibig; }
    public double getTax() { return tax; }
    public double getTotalDeductions() { return totalDeductions; }

    public double getNetPay() { return netPay; }

    public double getTotalHoursWorked() { return totalHoursWorked; }
    public int getPresentDays() { return presentDays; }
    public int getAbsentDays() { return absentDays; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getLateHours() { return lateHours; }

    public String getStatus() { return "PROCESSED"; }

    // ========== SETTERS ==========

    public void setPayslipId(String payslipId) { this.payslipId = payslipId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setPeriod(YearMonth period) { this.period = period; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }

    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public void setTotalAllowance(double totalAllowance) { this.totalAllowance = totalAllowance; }
    public void setGrossBasic(double grossBasic) { this.grossBasic = grossBasic; }
    public void setGrossSalary(double grossSalary) { this.grossSalary = grossSalary; }
    public void setOvertimePay(double overtimePay) { this.overtimePay = overtimePay; }

    public void setSss(double sss) { this.sss = sss; }
    public void setPhilhealth(double philhealth) { this.philhealth = philhealth; }
    public void setPagibig(double pagibig) { this.pagibig = pagibig; }
    public void setTax(double tax) { this.tax = tax; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

    public void setNetPay(double netPay) { this.netPay = netPay; }

    public void setTotalHoursWorked(double totalHoursWorked) { this.totalHoursWorked = totalHoursWorked; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }
    public void setAbsentDays(int absentDays) { this.absentDays = absentDays; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setLateHours(double lateHours) { this.lateHours = lateHours; }

    // ========== FORMATTED GETTERS ==========

    public String getFormattedPeriod() {
        return period != null ? period.format(DateTimeFormatter.ofPattern("MMMM yyyy")) : "N/A";
    }

    public String getFormattedBasicSalary() {
        return String.format("₱ %,.2f", basicSalary);
    }

    public String getFormattedGrossSalary() {
        return String.format("₱ %,.2f", grossSalary);
    }

    public String getFormattedTotalDeductions() {
        return String.format("₱ %,.2f", totalDeductions);
    }

    public String getFormattedNetPay() {
        return String.format("₱ %,.2f", netPay);
    }

    public String getFormattedOvertimePay() {
        return String.format("₱ %,.2f", overtimePay);
    }

    public String getFormattedRiceSubsidy() {
        return String.format("₱ %,.2f", riceSubsidy);
    }

    public String getFormattedPhoneAllowance() {
        return String.format("₱ %,.2f", phoneAllowance);
    }

    public String getFormattedClothingAllowance() {
        return String.format("₱ %,.2f", clothingAllowance);
    }

    public String getFormattedSss() {
        return String.format("₱ %,.2f", sss);
    }

    public String getFormattedPhilhealth() {
        return String.format("₱ %,.2f", philhealth);
    }

    public String getFormattedPagibig() {
        return String.format("₱ %,.2f", pagibig);
    }

    public String getFormattedTax() {
        return String.format("₱ %,.2f", tax);
    }

    // ========== DETAILED STRING ==========

    public String toDetailedString() {
        StringBuilder sb = new StringBuilder();
        String line      = "=".repeat(70);
        String separator = "-".repeat(70);

        sb.append("\n").append(line).append("\n");
        sb.append(String.format("%43s\n", "MOTORPH PAYSLIP"));
        sb.append(line).append("\n\n");

        sb.append(String.format("Payslip ID : %s\n", payslipId));
        sb.append(String.format("Period     : %s\n", getFormattedPeriod()));
        sb.append(String.format("Generated  : %s\n",
                generatedDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))));
        sb.append(separator).append("\n");
        sb.append(String.format("Employee   : %s\n", employeeName));
        sb.append(String.format("Employee ID: %s\n", employeeId));
        sb.append(separator).append("\n\n");

        // ATTENDANCE
        sb.append("ATTENDANCE SUMMARY:\n");
        sb.append(row("  Hours Worked",  String.format("%.1f hrs", totalHoursWorked)));
        sb.append(row("  Days Present",  String.valueOf(presentDays)));
        sb.append(row("  Overtime",      String.format("%.1f hrs", overtimeHours)));
        sb.append("\n");

        // EARNINGS
        sb.append("EARNINGS:\n");
        sb.append(row("  Basic Salary",       getFormattedBasicSalary()));
        sb.append(row("  Rice Subsidy",        getFormattedRiceSubsidy()));
        sb.append(row("  Phone Allowance",     getFormattedPhoneAllowance()));
        sb.append(row("  Clothing Allowance",  getFormattedClothingAllowance()));
        sb.append(row("  Total Allowances",    String.format("₱ %,.2f", totalAllowance)));
        if (overtimePay > 0) {
            sb.append(row("  Overtime Pay", getFormattedOvertimePay()));
        }
        sb.append(separator).append("\n");
        sb.append(row("  GROSS SALARY", getFormattedGrossSalary()));
        sb.append("\n");

        // DEDUCTIONS
        sb.append("DEDUCTIONS:\n");
        sb.append(row("  SSS",              getFormattedSss()));
        sb.append(row("  PhilHealth",       getFormattedPhilhealth()));
        sb.append(row("  Pag-IBIG",         getFormattedPagibig()));
        sb.append(row("  Withholding Tax",  getFormattedTax()));
        sb.append(separator).append("\n");
        sb.append(row("  TOTAL DEDUCTIONS", getFormattedTotalDeductions()));
        sb.append("\n");

        // NET PAY
        sb.append(line).append("\n");
        sb.append(row("  NET PAY", getFormattedNetPay()));
        sb.append(line).append("\n");

        return sb.toString();
    }

    /**
     * Formats a label-value pair so the value is always right-aligned at column 70.
     * e.g. "  Basic Salary" + "₱ 90,000.00" → "  Basic Salary          ₱ 90,000.00\n"
     */
    private String row(String label, String value) {
        int totalWidth = 70;
        int valueWidth = totalWidth - label.length();
        return label + String.format("%" + valueWidth + "s\n", value);
    }
}