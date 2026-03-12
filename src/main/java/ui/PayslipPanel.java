package ui;

import main.MainController;
import model.*;
import model.Employee.Employee;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;

public class PayslipPanel extends JPanel {
    
    private final MainController controller;
    private final PayrollService payrollService;
    private final EmployeeService employeeService;
    private final User currentUser;
    private final Employee currentEmployee;
    
    // Constants
    private static final YearMonth COMPANY_FOUNDING_DATE = YearMonth.of(2024, 6);
    
    // UI Components
    private JComboBox<YearMonth> periodCombo;
    private JTextArea payslipDisplayArea;
    private JTable historyTable;
    private DefaultTableModel historyModel;
    
    // Buttons
    private JButton generateBtn;
    private JButton exportBtn;
    private JButton printBtn;
    private JButton backBtn;
    
    // Current payslip
    private Payslip currentPayslip;
    
    public PayslipPanel(MainController controller, PayrollService payrollService,
                        EmployeeService employeeService, User currentUser, 
                        Employee currentEmployee) {
        this.controller = controller;
        this.payrollService = payrollService;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.currentEmployee = currentEmployee;
        
        initializePanel();
        loadHistory();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Split content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        
        splitPane.setLeftComponent(createGeneratorPanel());
        splitPane.setRightComponent(createHistoryPanel());
        
        add(splitPane, BorderLayout.CENTER);
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
        
        JLabel titleLabel = new JLabel("MY PAYSLIPS");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createGeneratorPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        // Employee info
        JPanel empInfoPanel = new JPanel(new BorderLayout());
        empInfoPanel.setOpaque(false);
        empInfoPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel nameLabel = new JLabel(currentEmployee.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        empInfoPanel.add(nameLabel, BorderLayout.NORTH);
        
        JLabel detailsLabel = new JLabel(
            (currentEmployee.getPosition() != null ? currentEmployee.getPosition() : "No Position") + 
            " | " + currentEmployee.getEmployeeId()
        );
        detailsLabel.setFont(UITheme.NORMAL_FONT);
        detailsLabel.setForeground(UITheme.TEXT_SECONDARY);
        empInfoPanel.add(detailsLabel, BorderLayout.CENTER);
        
        panel.add(empInfoPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Period selection
        JPanel periodPanel = new JPanel(new BorderLayout());
        periodPanel.setOpaque(false);
        periodPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel periodLabel = new JLabel("SELECT PAYROLL PERIOD");
        periodLabel.setFont(UITheme.BOLD_SMALL_FONT);
        periodLabel.setForeground(UITheme.TEXT_SECONDARY);
        periodPanel.add(periodLabel, BorderLayout.NORTH);
        
        periodCombo = new JComboBox<>();
        periodCombo.setFont(UITheme.NORMAL_FONT);
        periodCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        periodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Add months from June 2024 up to current month
        YearMonth current = YearMonth.now();
        YearMonth start = COMPANY_FOUNDING_DATE;
        
        YearMonth month = current;
        while (!month.isBefore(start)) {
            periodCombo.addItem(month);
            month = month.minusMonths(1);
        }
        
        periodPanel.add(periodCombo, BorderLayout.CENTER);
        panel.add(periodPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Generate button
        generateBtn = UITheme.createPrimaryButton("GENERATE PAYSLIP", UITheme.ACCENT_BLUE);
        generateBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        generateBtn.addActionListener(e -> generatePayslip());
        panel.add(generateBtn);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Payslip display area
        payslipDisplayArea = new JTextArea();
        payslipDisplayArea.setFont(UITheme.MONO_FONT);
        payslipDisplayArea.setEditable(false);
        payslipDisplayArea.setBackground(new Color(250, 250, 250));
        payslipDisplayArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                "PAYSLIP PREVIEW",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                UITheme.BOLD_SMALL_FONT,
                UITheme.TEXT_SECONDARY
            ),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JScrollPane scrollPane = new JScrollPane(payslipDisplayArea);
        scrollPane.setPreferredSize(new Dimension(400, 400));
        scrollPane.setBorder(null);
        panel.add(scrollPane);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel titleLabel = UITheme.createSectionHeader("PAYMENT HISTORY");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"PERIOD", "GROSS PAY", "DEDUCTIONS", "NET PAY", "STATUS"};
        historyModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(35);
        historyTable.setFont(UITheme.NORMAL_FONT);
        historyTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        historyTable.getTableHeader().setBackground(UITheme.HEADER_BG);
        historyTable.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(UITheme.BORDER_COLOR);
        
        // Right align currency columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
        historyTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        historyTable.getColumnModel().getColumn(3).setCellRenderer(rightRenderer);
        
        // Status column renderer
        historyTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String status = value != null ? value.toString() : "";
                
                setHorizontalAlignment(SwingConstants.CENTER);
                
                if ("PROCESSED".equals(status)) {
                    setForeground(UITheme.ACCENT_GREEN);
                } else {
                    setForeground(UITheme.ACCENT_ORANGE);
                }
                
                return c;
            }
        });
        
        // Double click to view payslip
        historyTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewHistoricalPayslip();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        buttonPanel.setOpaque(false);
        
        exportBtn = UITheme.createDashboardButton("EXPORT TXT");
        exportBtn.addActionListener(e -> exportPayslip());
        buttonPanel.add(exportBtn);
        
        printBtn = UITheme.createDashboardButton("PRINT");
        printBtn.addActionListener(e -> printPayslip());
        buttonPanel.add(printBtn);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadHistory() {
        historyModel.setRowCount(0);
        
        try {
            java.util.List<Payslip> payslips = payrollService.getEmployeePayslips(currentUser.getEmployeeId());
            
            for (Payslip p : payslips) {
                historyModel.addRow(new Object[]{
                    p.getFormattedPeriod(),
                    p.getFormattedGrossSalary(),
                    p.getFormattedTotalDeductions(),
                    p.getFormattedNetPay(),
                    "PROCESSED"
                });
            }
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void generatePayslip() {
        YearMonth period = (YearMonth) periodCombo.getSelectedItem();
        if (period == null) return;
        
        try {
            currentPayslip = payrollService.generatePayslip(
                currentEmployee.getEmployeeId(), period);
            
            payslipDisplayArea.setText(currentPayslip.toDetailedString());
            
            // Check if already in history, if not add it
            boolean exists = false;
            for (int i = 0; i < historyModel.getRowCount(); i++) {
                if (historyModel.getValueAt(i, 0).equals(currentPayslip.getFormattedPeriod())) {
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                historyModel.insertRow(0, new Object[]{
                    currentPayslip.getFormattedPeriod(),
                    currentPayslip.getFormattedGrossSalary(),
                    currentPayslip.getFormattedTotalDeductions(),
                    currentPayslip.getFormattedNetPay(),
                    "PROCESSED"
                });
            }
            
        } catch (Exception e) {
            controller.showError("No payslip found for " + 
                period.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        }
    }
    
    private void viewHistoricalPayslip() {
        int row = historyTable.getSelectedRow();
        if (row == -1) return;
        
        String periodStr = (String) historyModel.getValueAt(row, 0);
        
        try {
            // Parse the period string (e.g., "January 2024")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
            YearMonth period = YearMonth.parse(periodStr, formatter);
            
            currentPayslip = payrollService.generatePayslip(
                currentEmployee.getEmployeeId(), period);
            payslipDisplayArea.setText(currentPayslip.toDetailedString());
            
            // Set period combo to match
            for (int i = 0; i < periodCombo.getItemCount(); i++) {
                if (periodCombo.getItemAt(i).equals(period)) {
                    periodCombo.setSelectedIndex(i);
                    break;
                }
            }
            
        } catch (Exception e) {
            controller.showError("Error loading payslip");
        }
    }
    
    private void exportPayslip() {
        if (currentPayslip == null) {
            controller.showWarning("Generate a payslip first");
            return;
        }
        
        String filename = String.format("Payslip_%s_%s.txt",
            currentEmployee.getEmployeeId(),
            currentPayslip.getFormattedPeriod().replace(" ", "_"));
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new java.io.File(filename));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.PrintWriter writer = new java.io.PrintWriter(fileChooser.getSelectedFile())) {
                writer.print(payslipDisplayArea.getText());
                controller.showInfo("Payslip exported successfully");
            } catch (Exception e) {
                controller.showError("Error exporting payslip: " + e.getMessage());
            }
        }
    }
    
    private void printPayslip() {
        if (currentPayslip == null) {
            controller.showWarning("Generate a payslip first");
            return;
        }
        
        try {
            boolean printed = payslipDisplayArea.print();
            if (printed) {
                controller.showInfo("Payslip sent to printer");
            }
        } catch (Exception e) {
            controller.showError("Error printing payslip: " + e.getMessage());
        }
    }
}