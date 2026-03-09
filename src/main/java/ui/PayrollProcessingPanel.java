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
    private JButton refreshBtn;

    // Stats
    private JLabel totalEmployeesLabel;
    private JLabel processedLabel;
    private JLabel pendingLabel;
    private JLabel totalGrossLabel;
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
        setupListeners();

        SwingUtilities.invokeLater(() -> loadData());
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);

        // --- TOP SECTION CONTAINER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createHeaderPanel());
        topContainer.add(createStatsPanel());
        topContainer.add(createControlsPanel());

        add(topContainer, BorderLayout.NORTH);

        // Table
        add(createTablePanel(), BorderLayout.CENTER);

        // Summary panel
        add(createSummaryPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);

        // Bold Black Back Button
        backBtn = createDashboardStyleButton("← BACK");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.addActionListener(e -> controller.showDashboard());

        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));

        JLabel titleLabel = new JLabel("PAYROLL PROCESSING");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);

        return headerPanel;
    }

    // --- DASHBOARD STYLE BUTTON HELPER (Standardized) ---
    private JButton createDashboardStyleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setBackground(UITheme.CARD_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(245, 245, 245));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 1),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UITheme.CARD_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        return btn;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        panel.add(createStatCard("TOTAL", "0", UITheme.TEXT_PRIMARY));
        panel.add(createStatCard("PROCESSED", "0", UITheme.ACCENT_GREEN));
        panel.add(createStatCard("PENDING", "0", UITheme.ACCENT_ORANGE));
        panel.add(createStatCard("TOTAL GROSS", "₱0", UITheme.ACCENT_BLUE));
        panel.add(createStatCard("TOTAL NET", "₱0", UITheme.ACCENT_PURPLE));

        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.BOLD_SMALL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(valueLabel, BorderLayout.CENTER);

        switch (title) {
            case "TOTAL": totalEmployeesLabel = valueLabel; break;
            case "PROCESSED": processedLabel = valueLabel; break;
            case "PENDING": pendingLabel = valueLabel; break;
            case "TOTAL GROSS": totalGrossLabel = valueLabel; break;
            case "TOTAL NET": totalNetLabel = valueLabel; break;
        }
        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        panel.add(new JLabel("PAYROLL PERIOD:"));

        periodCombo = new JComboBox<>();
        periodCombo.setFont(UITheme.NORMAL_FONT);
        periodCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        periodCombo.setPreferredSize(new Dimension(150, 35));

        YearMonth current = YearMonth.now();
        YearMonth start = COMPANY_FOUNDING_DATE;
        YearMonth month = current;
        while (!month.isBefore(start)) {
            periodCombo.addItem(month);
            month = month.minusMonths(1);
        }

        // Set current month as default
        periodCombo.setSelectedItem(current);

        panel.add(periodCombo);

        refreshBtn = createDashboardStyleButton("REFRESH");
        refreshBtn.addActionListener(e -> loadData());
        panel.add(refreshBtn);

        processAllBtn = UITheme.createPrimaryButton("PROCESS ALL", UITheme.ACCENT_GREEN);
        processAllBtn.addActionListener(e -> processAllPayroll());
        panel.add(processAllBtn);

        processSelectedBtn = UITheme.createPrimaryButton("PROCESS SELECTED", UITheme.ACCENT_BLUE);
        processSelectedBtn.addActionListener(e -> processSelectedPayroll());
        panel.add(processSelectedBtn);

        viewReportBtn = createDashboardStyleButton("VIEW REPORT");
        viewReportBtn.addActionListener(e -> viewReport());
        panel.add(viewReportBtn);

        exportBtn = createDashboardStyleButton("EXPORT");
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
                setFont(getFont().deriveFont(Font.BOLD));
                if ("PROCESSED".equals(status)) {
                    setForeground(UITheme.ACCENT_GREEN);
                } else {
                    setForeground(UITheme.ACCENT_ORANGE);
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

    private void setupListeners() {
        periodCombo.addActionListener(e -> {
            if (statusLabel != null && tableModel != null) {
                loadData();
            }
        });
    }

    private void loadData() {
        if (statusLabel == null || periodCombo == null || tableModel == null) return;

        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;

        statusLabel.setText("Loading data...");
        final YearMonth selectedPeriod = period;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                try {
                    allEmployees = employeeService.getAllEmployees();
                    processedStatus.clear();
                    for (Employee emp : allEmployees) {
                        boolean isProcessed = payrollService.hasPayroll(emp.getEmployeeId(), selectedPeriod);
                        processedStatus.put(emp.getEmployeeId(), isProcessed);
                        System.out.println("Employee " + emp.getEmployeeId() + " processed: " + isProcessed);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void done() {
                updateTable();
                updateStats(selectedPeriod);
                statusLabel.setText("Ready");
            }
        };
        worker.execute();
    }

    private void updateTable() {
        if (tableModel == null || allEmployees == null) return;

        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0);
            for (Employee emp : allEmployees) {
                boolean isProcessed = processedStatus.getOrDefault(emp.getEmployeeId(), false);
                tableModel.addRow(new Object[]{
                        emp.getEmployeeId(),
                        emp.getFullName(),
                        emp.getPosition() != null ? emp.getPosition() : "—",
                        emp.getDepartment() != null ? emp.getDepartment() : "—",
                        UITheme.formatCurrency(emp.getBasicSalary()),
                        isProcessed ? "PROCESSED" : "PENDING"
                });
            }
        });
    }

    private void updateStats(YearMonth period) {
        if (period == null || totalEmployeesLabel == null) return;
        try {
            Map<String, Object> status = payrollService.getPayrollStatus(period);

            SwingUtilities.invokeLater(() -> {
                totalEmployeesLabel.setText(String.valueOf(status.get("totalEmployees")));
                processedLabel.setText(String.valueOf(status.get("processedCount")));
                pendingLabel.setText(String.valueOf(status.get("pendingCount")));
                totalGrossLabel.setText(UITheme.formatCurrency((double) status.get("totalGrossSalary")));
                totalNetLabel.setText(UITheme.formatCurrency((double) status.get("totalNetSalary")));
                summaryLabel.setText(String.format("Processed: %s/%s employees",
                        status.get("processedCount"), status.get("totalEmployees")));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAllPayroll() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;

        int pendingCount = Integer.parseInt(pendingLabel.getText());
        if (pendingCount == 0) {
            controller.showInfo("All employees already processed for this period.");
            return;
        }

        if (controller.showConfirm("Process all pending payroll for " + period + "?", "CONFIRM")) {
            setButtonsEnabled(false);
            statusLabel.setText("Processing all payroll...");

            SwingWorker<List<Payslip>, Void> worker = new SwingWorker<>() {
                @Override
                protected List<Payslip> doInBackground() throws Exception {
                    return payrollService.processPayroll(period);
                }
                @Override
                protected void done() {
                    try {
                        List<Payslip> results = get();
                        controller.showInfo("Successfully processed " + results.size() + " payroll records.");
                        loadData();
                    } catch (Exception e) {
                        e.printStackTrace();
                        controller.showError("Error processing payroll: " + e.getMessage());
                    } finally {
                        setButtonsEnabled(true);
                        statusLabel.setText("Ready");
                    }
                }
            };
            worker.execute();
        }
    }

    private void processSelectedPayroll() {
        int[] selectedRows = employeeTable.getSelectedRows();
        if (selectedRows.length == 0) {
            controller.showWarning("Please select at least one employee to process.");
            return;
        }

        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;

        // Check if any selected employees are already processed
        List<String> toProcess = new ArrayList<>();
        List<String> alreadyProcessed = new ArrayList<>();

        for (int row : selectedRows) {
            String empId = (String) tableModel.getValueAt(row, 0);
            if (!processedStatus.getOrDefault(empId, false)) {
                toProcess.add(empId);
            } else {
                alreadyProcessed.add(empId);
            }
        }

        if (toProcess.isEmpty()) {
            controller.showInfo("All selected employees are already processed.");
            return;
        }

        String message = "Process payroll for " + toProcess.size() + " selected employee(s)?";
        if (!alreadyProcessed.isEmpty()) {
            message += "\n(" + alreadyProcessed.size() + " employee(s) already processed will be skipped)";
        }

        if (controller.showConfirm(message, "CONFIRM")) {
            setButtonsEnabled(false);
            statusLabel.setText("Processing selected payroll...");

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    int successCount = 0;
                    for (String empId : toProcess) {
                        try {
                            System.out.println("Processing payroll for employee: " + empId);

                            // Generate payslip - this now saves to history
                            Payslip payslip = payrollService.generatePayslip(empId, period);

                            if (payslip != null) {
                                successCount++;
                                System.out.println("Successfully processed payroll for: " + empId);

                                // Verify it was saved
                                boolean hasPayroll = payrollService.hasPayroll(empId, period);
                                System.out.println("Verification - hasPayroll: " + hasPayroll);

                                // Update the processed status map
                                processedStatus.put(empId, true);
                            }
                        } catch (Exception e) {
                            System.err.println("Error processing employee " + empId + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    return successCount;
                }
                @Override
                protected void done() {
                    try {
                        int successCount = get();
                        controller.showInfo("Successfully processed " + successCount + " of " + toProcess.size() + " selected employees.");

                        // Update the table immediately with the new status
                        updateTable();

                        // Also reload stats
                        updateStats(period);

                        // Force a complete reload of data from database to be sure
                        loadData();

                    } catch (Exception e) {
                        e.printStackTrace();
                        controller.showError("Error processing selected payroll: " + e.getMessage());
                    } finally {
                        setButtonsEnabled(true);
                        statusLabel.setText("Ready");
                    }
                }
            };
            worker.execute();
        }
    }

    private void viewReport() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;

        try {
            Map<String, Object> status = payrollService.getPayrollStatus(period);

            StringBuilder report = new StringBuilder();
            report.append("=".repeat(60)).append("\n");
            report.append(String.format("%40s\n", "PAYROLL REPORT"));
            report.append("=".repeat(60)).append("\n\n");
            report.append("Period: ").append(period.format(DateTimeFormatter.ofPattern("MMMM yyyy"))).append("\n");
            report.append("Generated: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"))).append("\n\n");
            report.append("-".repeat(60)).append("\n\n");
            report.append("Total Employees: ").append(status.get("totalEmployees")).append("\n");
            report.append("Processed: ").append(status.get("processedCount")).append("\n");
            report.append("Pending: ").append(status.get("pendingCount")).append("\n\n");
            report.append("Total Gross Salary: ").append(UITheme.formatCurrency((double) status.get("totalGrossSalary"))).append("\n");
            report.append("Total Deductions: ").append(UITheme.formatCurrency((double) status.get("totalDeductions"))).append("\n");
            report.append("Total Net Salary: ").append(UITheme.formatCurrency((double) status.get("totalNetSalary"))).append("\n\n");
            report.append("=".repeat(60));

            JTextArea textArea = new JTextArea(report.toString());
            textArea.setFont(UITheme.MONO_FONT);
            textArea.setEditable(false);
            textArea.setBackground(UITheme.CARD_BG);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(600, 400));

            JOptionPane.showMessageDialog(this, scrollPane, "PAYROLL REPORT", JOptionPane.PLAIN_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            controller.showError("Error generating report: " + e.getMessage());
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
                writer.println("Employee ID,Employee Name,Position,Department,Basic Salary,Gross Salary,SSS,PhilHealth,Pag-IBIG,Tax,Total Deductions,Net Salary,Status");

                // Write data
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String empId = (String) tableModel.getValueAt(i, 0);
                    boolean isProcessed = processedStatus.getOrDefault(empId, false);

                    if (isProcessed) {
                        // Get the payroll record for more details
                        String payrollId = empId + "_" + period.toString().replace("-", "_");
                        Payslip payslip = payrollService.getPayslip(payrollId);

                        if (payslip != null) {
                            writer.println(
                                    empId + "," +
                                            tableModel.getValueAt(i, 1) + "," +
                                            tableModel.getValueAt(i, 2) + "," +
                                            tableModel.getValueAt(i, 3) + "," +
                                            tableModel.getValueAt(i, 4).toString().replace("₱ ", "").replace(",", "") + "," +
                                            String.format("%.2f", payslip.getGrossSalary()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getSss()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getPhilhealth()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getPagibig()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getTax()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getTotalDeductions()).replace(",", "") + "," +
                                            String.format("%.2f", payslip.getNetPay()).replace(",", "") + "," +
                                            "PROCESSED"
                            );
                        } else {
                            writer.println(
                                    empId + "," +
                                            tableModel.getValueAt(i, 1) + "," +
                                            tableModel.getValueAt(i, 2) + "," +
                                            tableModel.getValueAt(i, 3) + "," +
                                            tableModel.getValueAt(i, 4).toString().replace("₱ ", "").replace(",", "") + "," +
                                            ",,,,,,,PROCESSED"
                            );
                        }
                    } else {
                        writer.println(
                                empId + "," +
                                        tableModel.getValueAt(i, 1) + "," +
                                        tableModel.getValueAt(i, 2) + "," +
                                        tableModel.getValueAt(i, 3) + "," +
                                        tableModel.getValueAt(i, 4).toString().replace("₱ ", "").replace(",", "") + "," +
                                        ",,,,,,,PENDING"
                        );
                    }
                }

                controller.showInfo("Payroll data exported successfully to " + filename);

            } catch (Exception e) {
                e.printStackTrace();
                controller.showError("Error exporting data: " + e.getMessage());
            }
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        processAllBtn.setEnabled(enabled);
        processSelectedBtn.setEnabled(enabled);
        viewReportBtn.setEnabled(enabled);
        exportBtn.setEnabled(enabled);
        refreshBtn.setEnabled(enabled);
        periodCombo.setEnabled(enabled);
    }
}