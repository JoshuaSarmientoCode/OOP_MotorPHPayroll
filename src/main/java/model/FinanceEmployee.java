package model;

import java.util.ArrayList;
import java.util.List;

public class FinanceEmployee extends Employee {
    // ========== PRIVATE FIELDS ==========
    private List<Payroll> processedPayrolls;
    private double budgetAllocation;
    private List<String> taxRecords;
    private List<String> financialReports;
    private List<String> approvedTransactions;
    private double monthlyBudget;
    private double spentAmount;
    private String financeRole;

    // ========== CONSTRUCTORS ==========

    public FinanceEmployee() {
        super();
        this.processedPayrolls = new ArrayList<>();
        this.taxRecords = new ArrayList<>();
        this.financialReports = new ArrayList<>();
        this.approvedTransactions = new ArrayList<>();
        this.budgetAllocation = 1000000.0;
        this.monthlyBudget = 100000.0;
        this.spentAmount = 0.0;
        this.financeRole = "ACCOUNTANT";
    }

    public FinanceEmployee(String employeeId, String firstName, String lastName, String position) {
        super(employeeId, firstName, lastName, position);
        this.processedPayrolls = new ArrayList<>();
        this.taxRecords = new ArrayList<>();
        this.financialReports = new ArrayList<>();
        this.approvedTransactions = new ArrayList<>();
        this.budgetAllocation = 1000000.0;
        this.monthlyBudget = 100000.0;
        this.spentAmount = 0.0;
        this.financeRole = "ACCOUNTANT";
    }

    // ========== GETTERS ==========

    public List<Payroll> getProcessedPayrolls() {
        return new ArrayList<>(processedPayrolls);
    }

    public double getBudgetAllocation() {
        return budgetAllocation;
    }

    public List<String> getTaxRecords() {
        return new ArrayList<>(taxRecords);
    }

    public List<String> getFinancialReports() {
        return new ArrayList<>(financialReports);
    }

    public List<String> getApprovedTransactions() {
        return new ArrayList<>(approvedTransactions);
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public double getRemainingBudget() {
        return monthlyBudget - spentAmount;
    }

    public String getFinanceRole() {
        return financeRole;
    }

    // ========== SETTERS WITH VALIDATION ==========

    public void setProcessedPayrolls(List<Payroll> processedPayrolls) {
        this.processedPayrolls = processedPayrolls != null ?
                new ArrayList<>(processedPayrolls) : new ArrayList<>();
    }

    public void setBudgetAllocation(double budgetAllocation) {
        if (budgetAllocation < 0) {
            throw new IllegalArgumentException("Budget allocation cannot be negative");
        }
        if (budgetAllocation > 1_000_000_000) {
            throw new IllegalArgumentException("Budget allocation too high");
        }
        this.budgetAllocation = budgetAllocation;
    }

    public void setTaxRecords(List<String> taxRecords) {
        this.taxRecords = taxRecords != null ?
                new ArrayList<>(taxRecords) : new ArrayList<>();
    }

    public void setFinancialReports(List<String> financialReports) {
        this.financialReports = financialReports != null ?
                new ArrayList<>(financialReports) : new ArrayList<>();
    }

    public void setApprovedTransactions(List<String> approvedTransactions) {
        this.approvedTransactions = approvedTransactions != null ?
                new ArrayList<>(approvedTransactions) : new ArrayList<>();
    }

    public void setMonthlyBudget(double monthlyBudget) {
        if (monthlyBudget < 0) {
            throw new IllegalArgumentException("Monthly budget cannot be negative");
        }
        if (monthlyBudget > budgetAllocation) {
            throw new IllegalArgumentException("Monthly budget cannot exceed total allocation");
        }
        this.monthlyBudget = monthlyBudget;
    }

    public void setSpentAmount(double spentAmount) {
        if (spentAmount < 0) {
            throw new IllegalArgumentException("Spent amount cannot be negative");
        }
        if (spentAmount > monthlyBudget) {
            throw new IllegalArgumentException("Spent amount cannot exceed monthly budget");
        }
        this.spentAmount = spentAmount;
    }

    public void setFinanceRole(String financeRole) {
        if (financeRole == null || financeRole.trim().isEmpty()) {
            throw new IllegalArgumentException("Finance role cannot be empty");
        }
        String role = financeRole.toUpperCase();
        if (!role.matches("ACCOUNTANT|AUDITOR|FINANCIAL_ANALYST|CONTROLLER|CFO")) {
            throw new IllegalArgumentException("Invalid finance role: " + financeRole);
        }
        this.financeRole = role;
    }

    // ========== BUSINESS METHODS ==========

    /**
     * Add processed payroll
     */
    public void addProcessedPayroll(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }
        processedPayrolls.add(payroll);
    }

    /**
     * Add tax record
     */
    public void addTaxRecord(String taxRecord) {
        if (taxRecord == null || taxRecord.trim().isEmpty()) {
            throw new IllegalArgumentException("Tax record cannot be empty");
        }
        String timestamped = java.time.LocalDateTime.now() + " - " + taxRecord;
        taxRecords.add(0, timestamped); // Add to beginning

        // Keep only last 1000 records
        if (taxRecords.size() > 1000) {
            taxRecords = taxRecords.subList(0, 1000);
        }
    }

    /**
     * Generate financial report
     */
    public void generateFinancialReport(String reportType) {
        if (reportType == null || reportType.trim().isEmpty()) {
            throw new IllegalArgumentException("Report type cannot be empty");
        }
        String report = String.format("%s REPORT - Generated on %s",
                reportType.toUpperCase(), java.time.LocalDate.now());
        financialReports.add(0, report);
    }

    /**
     * Get recent financial reports
     */
    public List<String> getRecentReports(int count) {
        if (count <= 0) return new ArrayList<>();
        return financialReports.stream()
                .limit(Math.min(count, financialReports.size()))
                .toList();
    }

    /**
     * Approve transaction
     */
    public void approveTransaction(String transactionDetails, double amount) {
        if (transactionDetails == null || transactionDetails.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction details cannot be empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Transaction amount must be positive");
        }
        if (spentAmount + amount > monthlyBudget) {
            throw new IllegalArgumentException("Transaction would exceed monthly budget");
        }

        String approval = String.format("APPROVED: %s - ₱%,.2f on %s",
                transactionDetails, amount, java.time.LocalDate.now());
        approvedTransactions.add(0, approval);
        spentAmount += amount;
    }

    /**
     * Get spending status
     */
    public String getSpendingStatus() {
        double percentUsed = (spentAmount / monthlyBudget) * 100;
        return String.format("Budget Used: ₱%,.2f / ₱%,.2f (%.1f%%)",
                spentAmount, monthlyBudget, percentUsed);
    }

    /**
     * Get total processed payroll amount for period
     */
    public double getTotalProcessedPayroll(java.time.YearMonth period) {
        return processedPayrolls.stream()
                .filter(p -> p.getPayrollPeriod().equals(period))
                .mapToDouble(Payroll::getNetSalary)
                .sum();
    }

    /**
     * Get payroll statistics
     */
    public java.util.Map<String, Object> getPayrollStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalProcessed", processedPayrolls.size());
        stats.put("taxRecordsCount", taxRecords.size());
        stats.put("reportsGenerated", financialReports.size());
        stats.put("approvedTransactions", approvedTransactions.size());
        stats.put("budgetUtilization", getSpendingStatus());
        return stats;
    }

    // ========== OVERRIDDEN ABSTRACT METHODS ==========

    @Override
    public String getDepartment() { return "FINANCE"; }

    @Override
    public String getRoleName() {
        return "FINANCE";
    }

    @Override
    public boolean canAccess(String feature) {
        switch (feature) {
            case "PAYROLL":
            case "PAYSLIP":
            case "VIEW_REPORTS":
            case "FINANCIAL_DATA":
            case "TAX_RECORDS":
            case "BUDGET_MANAGEMENT":
                return true;
            default:
                return false;
        }
    }

    @Override
    public DashboardType getDashboardType() {
        return DashboardType.FINANCE;
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("FinanceEmployee[id=%s, name=%s, role=%s, budget=₱%,.2f]",
                getEmployeeId(), getFullName(), financeRole, getRemainingBudget());
    }
}