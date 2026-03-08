package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.List;

public class PayrollProcessingPanel extends JPanel {
    
    private final MainController controller;
    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final User currentUser;
    
    // Constants
    private static final YearMonth COMPANY_FOUNDING_DATE = YearMonth.of(2024, 6);
    
    // UI Components
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JComboBox<YearMonth> periodCombo;
    private JLabel statusLabel;
    private JLabel summaryLabel;
    
    // Buttons
    private JButton processAllBtn;
    private JButton processSelectedBtn;
    private JButton viewReportBtn;
    private JButton exportBtn;
    private JButton backBtn;
    
    // Stats
    private JLabel totalEmployeesLabel;
    private JLabel processedLabel;
    private JLabel pendingLabel;
    private JLabel totalNetLabel;
    
    private List<Employee> allEmployees;
    private Map<String, Boolean> processedStatus;
    
    public PayrollProcessingPanel(MainController controller, PayrollService payrollService,
                                  EmployeeService employeeService, User currentUser) {
        this.controller = controller;
        this.payrollService = payrollService;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.allEmployees = new ArrayList<>();
        this.processedStatus = new HashMap<>();
        
        initializePanel();
        loadData();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Stats panel
        add(createStatsPanel(), BorderLayout.NORTH);
        
        // Controls panel
        add(createControlsPanel(), BorderLayout.NORTH);
        
        // Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Summary panel
        add(createSummaryPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);
        
        backBtn = UITheme.createBackButton();
        backBtn.addActionListener(e -> controller.goBack());
        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JLabel titleLabel = new JLabel("PAYROLL PROCESSING");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        panel.add(createStatCard("TOTAL EMPLOYEES", "0", UITheme.TEXT_PRIMARY));
        panel.add(createStatCard("PROCESSED", "0", UITheme.ACCENT_GREEN));
        panel.add(createStatCard("PENDING", "0", UITheme.ACCENT_ORANGE));
        panel.add(createStatCard("TOTAL NET PAY", "₱0", UITheme.ACCENT_BLUE));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.BOLD_SMALL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        // Store references
        switch (title) {
            case "TOTAL EMPLOYEES":
                totalEmployeesLabel = valueLabel;
                break;
            case "PROCESSED":
                processedLabel = valueLabel;
                break;
            case "PENDING":
                pendingLabel = valueLabel;
                break;
            case "TOTAL NET PAY":
                totalNetLabel = valueLabel;
                break;
        }
        
        return panel;
    }
    
    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        panel.add(new JLabel("PAYROLL PERIOD:"));
        
        periodCombo = new JComboBox<>();
        periodCombo.setFont(UITheme.NORMAL_FONT);
        periodCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        periodCombo.setPreferredSize(new Dimension(150, 35));
        periodCombo.addActionListener(e -> loadData());
        
        // Add months from June 2024 up to current month
        YearMonth current = YearMonth.now();
        YearMonth start = COMPANY_FOUNDING_DATE;
        
        YearMonth month = current;
        while (!month.isBefore(start)) {
            periodCombo.addItem(month);
            month = month.minusMonths(1);
        }
        
        panel.add(periodCombo);
        
        processAllBtn = UITheme.createPrimaryButton("PROCESS ALL", UITheme.ACCENT_GREEN);
        processAllBtn.setPreferredSize(new Dimension(150, 35));
        processAllBtn.addActionListener(e -> processAllPayroll());
        panel.add(processAllBtn);
        
        processSelectedBtn = UITheme.createPrimaryButton("PROCESS SELECTED", UITheme.ACCENT_BLUE);
        processSelectedBtn.setPreferredSize(new Dimension(150, 35));
        processSelectedBtn.addActionListener(e -> processSelectedPayroll());
        panel.add(processSelectedBtn);
        
        viewReportBtn = UITheme.createDashboardButton("VIEW REPORT");
        viewReportBtn.setPreferredSize(new Dimension(120, 35));
        viewReportBtn.addActionListener(e -> viewReport());
        panel.add(viewReportBtn);
        
        exportBtn = UITheme.createDashboardButton("EXPORT");
        exportBtn.setPreferredSize(new Dimension(100, 35));
        exportBtn.addActionListener(e -> exportData());
        panel.add(exportBtn);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        
        String[] columns = {"ID", "EMPLOYEE", "POSITION", "DEPARTMENT", "BASIC SALARY", "STATUS"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(40);
        employeeTable.setFont(UITheme.NORMAL_FONT);
        employeeTable.setSelectionBackground(UITheme.SELECTION_BG);
        employeeTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        employeeTable.getTableHeader().setBackground(UITheme.HEADER_BG);
        employeeTable.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        employeeTable.setShowGrid(true);
        employeeTable.setGridColor(UITheme.BORDER_COLOR);
        employeeTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // Status column renderer
        employeeTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String status = value != null ? value.toString() : "";
                
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if ("PROCESSED".equals(status)) {
                    setForeground(UITheme.ACCENT_GREEN);
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setForeground(UITheme.ACCENT_ORANGE);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(statusLabel, BorderLayout.WEST);
        
        summaryLabel = new JLabel("");
        summaryLabel.setFont(UITheme.BOLD_SMALL_FONT);
        summaryLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(summaryLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private void loadData() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        try {
            allEmployees = employeeService.getAllEmployees();
            if (allEmployees == null) {
                allEmployees = new ArrayList<>();
            }
            
            Map<String, Object> status = payrollService.getPayrollStatus(period);
            if (status == null) {
                status = new HashMap<>();
                status.put("totalEmployees", allEmployees.size());
                status.put("processedCount", 0);
                status.put("pendingCount", allEmployees.size());
            }
            
            processedStatus.clear();
            for (Employee emp : allEmployees) {
                boolean isProcessed = false;
                try {
                    isProcessed = payrollService.hasPayroll(emp.getEmployeeId(), period);
                } catch (Exception ex) {
                    // Ignore
                }
                processedStatus.put(emp.getEmployeeId(), isProcessed);
            }
            
            updateTable();
            updateStats(status);
            
        } catch (Exception e) {
            statusLabel.setText("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void updateTable() {
        tableModel.setRowCount(0);
        
        if (allEmployees == null) return;
        
        for (Employee emp : allEmployees) {
            String status = processedStatus.getOrDefault(emp.getEmployeeId(), false) 
                ? "PROCESSED" : "PENDING";
            
            tableModel.addRow(new Object[]{
                emp.getEmployeeId(),
                emp.getFullName(),
                emp.getPosition() != null ? emp.getPosition() : "—",
                emp.getDepartment() != null ? emp.getDepartment() : "—",
                UITheme.formatCurrency(emp.getBasicSalary()),
                status
            });
        }
    }
    
    private void updateStats(Map<String, Object> status) {
        int total = status.containsKey("totalEmployees") ? (int) status.get("totalEmployees") : 0;
        int processed = status.containsKey("processedCount") ? (int) status.get("processedCount") : 0;
        int pending = status.containsKey("pendingCount") ? (int) status.get("pendingCount") : 0;
        
        // Handle totalNetSalary which might not be present in the status map
        double totalNet = 0.0;
        if (status.containsKey("totalNetSalary")) {
            totalNet = (double) status.get("totalNetSalary");
        }
        
        totalEmployeesLabel.setText(String.valueOf(total));
        processedLabel.setText(String.valueOf(processed));
        pendingLabel.setText(String.valueOf(pending));
        totalNetLabel.setText(UITheme.formatCurrency(totalNet));
        
        summaryLabel.setText(String.format("Processed: %d/%d employees", processed, total));
    }
    
    private void processAllPayroll() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        boolean confirmed = controller.showConfirm(
            "Process payroll for ALL employees for " + 
            period.format(DateTimeFormatter.ofPattern("MMMM yyyy")) + "?",
            "CONFIRM PAYROLL PROCESSING"
        );
        
        if (!confirmed) return;
        
        // Disable buttons during processing
        setButtonsEnabled(false);
        statusLabel.setText("Processing payroll...");
        
        SwingWorker<List<Payslip>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Payslip> doInBackground() {
                try {
                    return payrollService.processPayroll(period);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new ArrayList<>();
                }
            }
            
            @Override
            protected void done() {
                try {
                    List<Payslip> payslips = get();
                    if (payslips != null) {
                        controller.showInfo("Payroll processed successfully for " + 
                            payslips.size() + " employees");
                    } else {
                        controller.showWarning("Payroll processing completed with no results");
                    }
                    loadData();
                } catch (Exception e) {
                    controller.showError("Error processing payroll: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    statusLabel.setText("Ready");
                }
            }
        };
        
        worker.execute();
    }
    
    private void processSelectedPayroll() {
        int[] selectedRows = employeeTable.getSelectedRows();
        if (selectedRows.length == 0) {
            controller.showWarning("SELECT AT LEAST ONE EMPLOYEE");
            return;
        }
        
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        boolean confirmed = controller.showConfirm(
            "Process payroll for " + selectedRows.length + " selected employees?",
            "CONFIRM PAYROLL PROCESSING"
        );
        
        if (!confirmed) return;
        
        setButtonsEnabled(false);
        statusLabel.setText("Processing selected employees...");
        
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int success = 0;
                for (int row : selectedRows) {
                    String empId = (String) tableModel.getValueAt(row, 0);
                    try {
                        if (!processedStatus.getOrDefault(empId, false)) {
                            Payslip payslip = payrollService.generatePayslip(empId, period);
                            if (payslip != null) {
                                success++;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error processing employee " + empId + ": " + e.getMessage());
                    }
                }
                return success;
            }
            
            @Override
            protected void done() {
                try {
                    int success = get();
                    controller.showInfo("Payroll processed for " + success + " employees");
                    loadData();
                } catch (Exception e) {
                    controller.showError("Error processing payroll: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    setButtonsEnabled(true);
                    statusLabel.setText("Ready");
                }
            }
        };
        
        worker.execute();
    }
    
    private void viewReport() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        try {
            Map<String, Object> status = payrollService.getPayrollStatus(period);
            if (status == null) {
                controller.showWarning("No payroll data available for this period");
                return;
            }
            
            StringBuilder report = new StringBuilder();
            report.append("PAYROLL REPORT\n");
            report.append("=".repeat(60)).append("\n\n");
            report.append("Period: ").append(period.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n\n");
            
            report.append("SUMMARY\n");
            report.append("-".repeat(40)).append("\n");
            report.append(String.format("Total Employees: %d\n", status.getOrDefault("totalEmployees", 0)));
            report.append(String.format("Processed: %d\n", status.getOrDefault("processedCount", 0)));
            report.append(String.format("Pending: %d\n", status.getOrDefault("pendingCount", 0)));
            
            if (status.containsKey("totalGrossSalary")) {
                report.append(String.format("Total Gross Salary: %s\n", UITheme.formatCurrency((double) status.get("totalGrossSalary"))));
            }
            if (status.containsKey("totalDeductions")) {
                report.append(String.format("Total Deductions: %s\n", UITheme.formatCurrency((double) status.get("totalDeductions"))));
            }
            if (status.containsKey("totalNetSalary")) {
                report.append(String.format("Total Net Pay: %s\n\n", UITheme.formatCurrency((double) status.get("totalNetSalary"))));
            }
            
            if (status.containsKey("byDepartment")) {
                report.append("DEPARTMENT BREAKDOWN\n");
                report.append("-".repeat(40)).append("\n");
                Map<String, Double> deptTotals = (Map<String, Double>) status.get("byDepartment");
                if (deptTotals != null && !deptTotals.isEmpty()) {
                    for (Map.Entry<String, Double> entry : deptTotals.entrySet()) {
                        report.append(String.format("%s: %s\n", entry.getKey(), UITheme.formatCurrency(entry.getValue())));
                    }
                } else {
                    report.append("No department data available\n");
                }
            }
            
            JTextArea textArea = new JTextArea(report.toString());
            textArea.setEditable(false);
            textArea.setFont(UITheme.MONO_FONT);
            textArea.setBackground(UITheme.CARD_BG);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 500));
            
            JOptionPane.showMessageDialog(this, scrollPane, "PAYROLL REPORT", 
                JOptionPane.PLAIN_MESSAGE);
            
        } catch (Exception e) {
            controller.showError("Error generating report: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void exportData() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        String filename = String.format("Payroll_%s.csv", period.toString().replace("-", "_"));
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(filename));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                // Write header
                writer.println("Employee ID,Employee Name,Position,Department,Basic Salary,Status");
                
                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    writer.println(
                        tableModel.getValueAt(i, 0) + "," +
                        tableModel.getValueAt(i, 1) + "," +
                        tableModel.getValueAt(i, 2) + "," +
                        tableModel.getValueAt(i, 3) + "," +
                        tableModel.getValueAt(i, 4) + "," +
                        tableModel.getValueAt(i, 5)
                    );
                }
                
                controller.showInfo("Data exported successfully");
                
            } catch (Exception e) {
                controller.showError("Error exporting data: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    private void setButtonsEnabled(boolean enabled) {
        processAllBtn.setEnabled(enabled);
        processSelectedBtn.setEnabled(enabled);
        viewReportBtn.setEnabled(enabled);
        exportBtn.setEnabled(enabled);
        periodCombo.setEnabled(enabled);
    }
}