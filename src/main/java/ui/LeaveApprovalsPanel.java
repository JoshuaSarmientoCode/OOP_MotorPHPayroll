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
import java.time.format.*;
import java.util.*;
import java.util.List;

public class LeaveApprovalsPanel extends JPanel {
    
    private final MainController controller;
    private final EmployeeService employeeService;
    private final User currentUser;
    
    // UI Components
    private JTable leaveTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JTextField searchField;
    
    // Stat labels
    private JLabel pendingValueLabel;
    private JLabel approvedValueLabel;
    private JLabel rejectedValueLabel;
    private JLabel totalValueLabel;
    
    // Buttons
    private JButton approveBtn;
    private JButton rejectBtn;
    private JButton viewBtn;
    private JButton refreshBtn;
    private JButton backBtn;
    
    private List<LeaveRequest> allRequests;
    
    public LeaveApprovalsPanel(MainController controller, EmployeeService employeeService, 
                              User currentUser) {
        this.controller = controller;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.allRequests = new ArrayList<>();
        
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
        
        // Filter panel
        add(createFilterPanel(), BorderLayout.NORTH);
        
        // Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Action panel
        add(createActionPanel(), BorderLayout.SOUTH);
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
        
        JLabel titleLabel = new JLabel("LEAVE APPROVALS");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        panel.add(createStatCard("PENDING", "0", UITheme.ACCENT_ORANGE));
        panel.add(createStatCard("APPROVED", "0", UITheme.ACCENT_GREEN));
        panel.add(createStatCard("REJECTED", "0", UITheme.ACCENT_RED));
        panel.add(createStatCard("TOTAL", "0", UITheme.TEXT_SECONDARY));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        panel.setPreferredSize(new Dimension(130, 70));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.BOLD_SMALL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        // Store reference to value label
        switch (title) {
            case "PENDING":
                pendingValueLabel = valueLabel;
                break;
            case "APPROVED":
                approvedValueLabel = valueLabel;
                break;
            case "REJECTED":
                rejectedValueLabel = valueLabel;
                break;
            case "TOTAL":
                totalValueLabel = valueLabel;
                break;
        }
        
        return panel;
    }
    
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        panel.add(new JLabel("FILTER:"));
        
        filterCombo = new JComboBox<>(new String[]{"ALL", "PENDING", "APPROVED", "REJECTED"});
        filterCombo.setFont(UITheme.NORMAL_FONT);
        filterCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        filterCombo.setPreferredSize(new Dimension(120, 35));
        filterCombo.addActionListener(e -> filterData());
        panel.add(filterCombo);
        
        panel.add(new JLabel("SEARCH:"));
        
        searchField = new JTextField(15);
        searchField.setFont(UITheme.NORMAL_FONT);
        searchField.setBorder(UITheme.INPUT_BORDER);
        searchField.putClientProperty("JTextField.placeholderText", "EMPLOYEE NAME/ID");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { searchRequests(); }
        });
        panel.add(searchField);
        
        refreshBtn = UITheme.createDashboardButton("REFRESH");
        refreshBtn.addActionListener(e -> loadData());
        panel.add(refreshBtn);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        
        String[] columns = {"ID", "EMPLOYEE", "DEPARTMENT", "TYPE", "DATES", "DAYS", "STATUS"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        leaveTable = new JTable(tableModel);
        leaveTable.setRowHeight(45);
        leaveTable.setFont(UITheme.NORMAL_FONT);
        leaveTable.setSelectionBackground(UITheme.SELECTION_BG);
        leaveTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        leaveTable.getTableHeader().setBackground(UITheme.HEADER_BG);
        leaveTable.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        leaveTable.setShowGrid(true);
        leaveTable.setGridColor(UITheme.BORDER_COLOR);
        
        // Status column renderer
        leaveTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String status = value != null ? value.toString() : "";
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));
                
                if ("PENDING".equals(status)) {
                    setForeground(UITheme.ACCENT_ORANGE);
                } else if ("APPROVED".equals(status)) {
                    setForeground(UITheme.ACCENT_GREEN);
                } else if ("REJECTED".equals(status)) {
                    setForeground(UITheme.ACCENT_RED);
                } else {
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                
                return c;
            }
        });
        
        // Double click to view details
        leaveTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewDetails();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(leaveTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setOpaque(false);
        
        viewBtn = UITheme.createDashboardButton("VIEW DETAILS");
        viewBtn.addActionListener(e -> viewDetails());
        panel.add(viewBtn);
        
        approveBtn = UITheme.createPrimaryButton("APPROVE", UITheme.ACCENT_GREEN);
        approveBtn.addActionListener(e -> approveRequest());
        panel.add(approveBtn);
        
        rejectBtn = UITheme.createPrimaryButton("REJECT", UITheme.ACCENT_RED);
        rejectBtn.addActionListener(e -> rejectRequest());
        panel.add(rejectBtn);
        
        return panel;
    }
    
    private void loadData() {
        try {
            allRequests = employeeService.getAllLeaveRequests();
            updateTable(allRequests);
            updateStats();
        } catch (Exception e) {
            controller.showError("ERROR LOADING DATA: " + e.getMessage());
        }
    }
    
    private void updateTable(List<LeaveRequest> requests) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        
        for (LeaveRequest r : requests) {
            tableModel.addRow(new Object[]{
                r.getRequestId(),
                r.getEmployeeName(),
                r.getDepartment() != null ? r.getDepartment() : "—",
                r.getLeaveType(),
                r.getStartDate().format(fmt) + " - " + r.getEndDate().format(fmt),
                r.getNumberOfDays(),
                r.getStatus()
            });
        }
    }
    
    private void updateStats() {
        long pending = allRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.PENDING)
            .count();
        long approved = allRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
            .count();
        long rejected = allRequests.stream()
            .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.REJECTED)
            .count();
        
        pendingValueLabel.setText(String.valueOf(pending));
        approvedValueLabel.setText(String.valueOf(approved));
        rejectedValueLabel.setText(String.valueOf(rejected));
        totalValueLabel.setText(String.valueOf(allRequests.size()));
    }
    
    private void filterData() {
        String filter = (String) filterCombo.getSelectedItem();
        List<LeaveRequest> filtered;
        
        if ("ALL".equals(filter)) {
            filtered = allRequests;
        } else {
            LeaveRequest.LeaveStatus status = LeaveRequest.LeaveStatus.valueOf(filter.toUpperCase());
            filtered = allRequests.stream()
                .filter(r -> r.getStatus() == status)
                .toList();
        }
        
        // Apply search filter
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (!searchTerm.isEmpty()) {
            filtered = filtered.stream()
                .filter(r -> r.getEmployeeName().toLowerCase().contains(searchTerm) ||
                            r.getEmployeeId().toLowerCase().contains(searchTerm) ||
                            r.getRequestId().toLowerCase().contains(searchTerm))
                .toList();
        }
        
        updateTable(filtered);
    }
    
    private void searchRequests() {
        filterData();
    }
    
    private void viewDetails() {
        LeaveRequest request = getSelectedRequest();
        if (request == null) return;
        
        StringBuilder details = new StringBuilder();
        details.append("REQUEST DETAILS\n");
        details.append("=".repeat(50)).append("\n\n");
        details.append("Request ID: ").append(request.getRequestId()).append("\n");
        details.append("Employee: ").append(request.getEmployeeName()).append(" (").append(request.getEmployeeId()).append(")\n");
        details.append("Department: ").append(request.getDepartment() != null ? request.getDepartment() : "—").append("\n");
        details.append("Position: ").append(request.getPosition() != null ? request.getPosition() : "—").append("\n");
        details.append("Leave Type: ").append(request.getLeaveType()).append("\n");
        details.append("Period: ").append(request.getFormattedPeriodLong()).append("\n");
        details.append("Days: ").append(request.getNumberOfDays()).append("\n");
        details.append("Reason: ").append(request.getReason()).append("\n");
        details.append("Status: ").append(request.getStatus()).append("\n");
        details.append("Request Date: ").append(request.getFormattedRequestDate()).append("\n");
        
        if (request.getApprovedBy() != null) {
            details.append("Approved By: ").append(request.getApprovedBy()).append("\n");
            details.append("Approval Date: ").append(request.getFormattedApprovalDate()).append("\n");
        }
        
        if (request.getRemarks() != null) {
            details.append("Remarks: ").append(request.getRemarks()).append("\n");
        }
        
        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(UITheme.MONO_FONT);
        textArea.setBackground(UITheme.CARD_BG);
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 400));
        
        JOptionPane.showMessageDialog(this, scrollPane, "LEAVE REQUEST DETAILS", 
            JOptionPane.PLAIN_MESSAGE);
    }
    
    private void approveRequest() {
        LeaveRequest request = getSelectedRequest();
        if (request == null) return;
        
        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            controller.showError("Only pending requests can be approved");
            return;
        }
        
        String remarks = controller.showInput("Approval remarks (optional):", "");
        if (remarks == null) return; // User cancelled
        
        boolean confirmed = controller.showConfirm(
            "Approve leave request for " + request.getEmployeeName() + "?",
            "CONFIRM APPROVAL"
        );
        
        if (confirmed) {
            try {
                if (employeeService.approveLeaveRequest(
                    request.getRequestId(), 
                    currentUser.getFullName(), 
                    remarks)) {
                    controller.showInfo("Request approved successfully");
                    loadData();
                }
            } catch (Exception e) {
                controller.showError("Error approving request: " + e.getMessage());
            }
        }
    }
    
    private void rejectRequest() {
        LeaveRequest request = getSelectedRequest();
        if (request == null) return;
        
        if (request.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            controller.showError("Only pending requests can be rejected");
            return;
        }
        
        String reason = controller.showInput("Rejection reason:", "");
        if (reason == null || reason.trim().isEmpty()) {
            controller.showError("Rejection reason is required");
            return;
        }
        
        boolean confirmed = controller.showConfirm(
            "Reject leave request for " + request.getEmployeeName() + "?",
            "CONFIRM REJECTION"
        );
        
        if (confirmed) {
            try {
                if (employeeService.rejectLeaveRequest(
                    request.getRequestId(), 
                    currentUser.getFullName(), 
                    reason)) {
                    controller.showInfo("Request rejected successfully");
                    loadData();
                }
            } catch (Exception e) {
                controller.showError("Error rejecting request: " + e.getMessage());
            }
        }
    }
    
    private LeaveRequest getSelectedRequest() {
        int row = leaveTable.getSelectedRow();
        if (row == -1) {
            controller.showWarning("SELECT A REQUEST FIRST");
            return null;
        }
        
        String id = (String) tableModel.getValueAt(row, 0);
        return employeeService.getLeaveRequest(id);
    }
}