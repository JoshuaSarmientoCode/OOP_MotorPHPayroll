package model;

import java.time.LocalDate;
import java.time.YearMonth;

public class Payroll {
    private String payrollId;
    private String employeeId;
    private String employeeName;
    private YearMonth payrollPeriod;
    private double basicSalary;
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    private double grossSalary;
    private double sssDeduction;
    private double philHealthDeduction;
    private double pagIbigDeduction;
    private double taxDeduction;
    private double totalDeductions;
    private double netSalary;
    private double totalHoursWorked;
    private int presentDays;
    private int totalLeaveDays;
    private double overtimeHours;
    private LocalDate generatedDate;
    private String status;
    private String department;
    private String position;
    
    public Payroll() {
        this.generatedDate = LocalDate.now();
        this.status = "PENDING";
    }
    
    // ========== GETTERS ==========
    
    public String getPayrollId() { return payrollId; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public YearMonth getPayrollPeriod() { return payrollPeriod; }
    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public double getGrossSalary() { return grossSalary; }
    public double getSssDeduction() { return sssDeduction; }
    public double getPhilHealthDeduction() { return philHealthDeduction; }
    public double getPagIbigDeduction() { return pagIbigDeduction; }
    public double getTaxDeduction() { return taxDeduction; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetSalary() { return netSalary; }
    public double getTotalHoursWorked() { return totalHoursWorked; }
    public int getPresentDays() { return presentDays; }
    public int getTotalLeaveDays() { return totalLeaveDays; }
    public double getOvertimeHours() { return overtimeHours; }
    public LocalDate getGeneratedDate() { return generatedDate; }
    public String getStatus() { return status; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }
    
    // ========== SETTERS ==========
    
    public void setPayrollId(String payrollId) { this.payrollId = payrollId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setPayrollPeriod(YearMonth payrollPeriod) { this.payrollPeriod = payrollPeriod; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public void setGrossSalary(double grossSalary) { this.grossSalary = grossSalary; }
    public void setSssDeduction(double sssDeduction) { this.sssDeduction = sssDeduction; }
    public void setPhilHealthDeduction(double philHealthDeduction) { this.philHealthDeduction = philHealthDeduction; }
    public void setPagIbigDeduction(double pagIbigDeduction) { this.pagIbigDeduction = pagIbigDeduction; }
    public void setTaxDeduction(double taxDeduction) { this.taxDeduction = taxDeduction; }
    public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }
    public void setNetSalary(double netSalary) { this.netSalary = netSalary; }
    public void setTotalHoursWorked(double totalHoursWorked) { this.totalHoursWorked = totalHoursWorked; }
    public void setPresentDays(int presentDays) { this.presentDays = presentDays; }
    public void setTotalLeaveDays(int totalLeaveDays) { this.totalLeaveDays = totalLeaveDays; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setGeneratedDate(LocalDate generatedDate) { this.generatedDate = generatedDate; }
    public void setStatus(String status) { this.status = status; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }
}