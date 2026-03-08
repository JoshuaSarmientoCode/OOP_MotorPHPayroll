package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class LeaveRequestPanel extends JPanel {
    
    private final MainController controller;
    private final EmployeeService employeeService;
    private final User currentUser;
    private final Employee currentEmployee;
    
    // Form fields
    private JComboBox<String> leaveTypeCombo;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private JTextArea reasonArea;
    private JButton submitBtn;
    private JButton backBtn;
    
    // History table
    private JTable historyTable;
    private DefaultTableModel historyModel;
    
    public LeaveRequestPanel(MainController controller, EmployeeService employeeService, 
                            User currentUser) {
        this.controller = controller;
        this.employeeService = employeeService;
        this.currentUser = currentUser;
        this.currentEmployee = controller.getCurrentEmployee();
        
        initializePanel();
        setDefaultDates();
        loadLeaveHistory();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        
        splitPane.setLeftComponent(createFormPanel());
        splitPane.setRightComponent(createHistoryPanel());
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Left side with back button and title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);
        
        backBtn = UITheme.createBackButton();
        backBtn.addActionListener(e -> controller.goBack());
        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JLabel titleLabel = new JLabel("LEAVE REQUEST");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        // Right side with user info
        JLabel userLabel = new JLabel(currentUser.getEmployeeId() + " | " + currentUser.getFullName());
        userLabel.setFont(UITheme.NORMAL_FONT);
        userLabel.setForeground(UITheme.TEXT_SECONDARY);
        headerPanel.add(userLabel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        
        // Leave Type
        JLabel typeLabel = new JLabel("LEAVE TYPE");
        typeLabel.setFont(UITheme.BOLD_SMALL_FONT);
        typeLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(typeLabel, gbc);
        
        String[] types = {"SICK LEAVE", "VACATION LEAVE", "EMERGENCY LEAVE",
                          "MATERNITY LEAVE", "PATERNITY LEAVE", "PERSONAL LEAVE", "UNPAID LEAVE"};
        leaveTypeCombo = new JComboBox<>(types);
        leaveTypeCombo.setFont(UITheme.NORMAL_FONT);
        leaveTypeCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        gbc.gridx = 1;
        panel.add(leaveTypeCombo, gbc);
        row++;
        
        // Start Date
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel startLabel = new JLabel("START DATE");
        startLabel.setFont(UITheme.BOLD_SMALL_FONT);
        startLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(startLabel, gbc);
        
        gbc.gridx = 1;
        startDateChooser = new JDateChooser();
        startDateChooser.setDateFormatString("yyyy-MM-dd");
        startDateChooser.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        panel.add(startDateChooser, gbc);
        row++;
        
        // End Date
        gbc.gridy = row;
        gbc.gridx = 0;
        JLabel endLabel = new JLabel("END DATE");
        endLabel.setFont(UITheme.BOLD_SMALL_FONT);
        endLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(endLabel, gbc);
        
        gbc.gridx = 1;
        endDateChooser = new JDateChooser();
        endDateChooser.setDateFormatString("yyyy-MM-dd");
        endDateChooser.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        panel.add(endDateChooser, gbc);
        row++;
        
        // Reason
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        
        JLabel reasonLabel = new JLabel("REASON");
        reasonLabel.setFont(UITheme.BOLD_SMALL_FONT);
        reasonLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(reasonLabel, gbc);
        
        gbc.gridx = 1;
        reasonArea = new JTextArea(6, 25);
        reasonArea.setFont(UITheme.NORMAL_FONT);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JScrollPane scroll = new JScrollPane(reasonArea);
        scroll.setBorder(null);
        panel.add(scroll, gbc);
        row++;
        
        // Submit Button
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 8, 8, 8);
        
        submitBtn = UITheme.createPrimaryButton("SUBMIT REQUEST", UITheme.ACCENT_GREEN);
        submitBtn.setPreferredSize(new Dimension(250, 45));
        submitBtn.addActionListener(e -> submitRequest());
        panel.add(submitBtn, gbc);
        
        return panel;
    }
    
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        ));
        
        JLabel titleLabel = UITheme.createSectionHeader("RECENT REQUESTS");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"TYPE", "DATES", "STATUS"};
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
        
        // Status column renderer
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                
                setHorizontalAlignment(SwingConstants.CENTER);
                
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
        
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scroll.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void setDefaultDates() {
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        
        startDateChooser.setDate(today);
        endDateChooser.setDate(cal.getTime());
    }
    
    private void loadLeaveHistory() {
        historyModel.setRowCount(0);
        
        try {
            var leaves = employeeService.getEmployeeLeaves(currentUser.getEmployeeId());
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
            
            for (LeaveRequest l : leaves) {
                historyModel.addRow(new Object[]{
                    l.getLeaveType(),
                    l.getStartDate().format(fmt) + " - " + l.getEndDate().format(fmt),
                    l.getStatus()
                });
            }
            
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void submitRequest() {
        // Validate inputs
        if (startDateChooser.getDate() == null || endDateChooser.getDate() == null) {
            controller.showError("SELECT DATES FIRST");
            return;
        }
        
        String reason = reasonArea.getText().trim();
        if (reason.isEmpty()) {
            controller.showError("ENTER A REASON");
            return;
        }
        
        if (reason.length() < 10) {
            controller.showError("PLEASE PROVIDE A MORE DETAILED REASON (AT LEAST 10 CHARACTERS)");
            return;
        }
        
        LocalDate start = startDateChooser.getDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate end = endDateChooser.getDate().toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
        
        // Check if start date is in the past
        if (start.isBefore(LocalDate.now())) {
            controller.showError("START DATE CANNOT BE IN THE PAST");
            return;
        }
        
        // Check if end date is before start date
        if (end.isBefore(start)) {
            controller.showError("END DATE CANNOT BE BEFORE START DATE");
            return;
        }
        
        // Create request
        LeaveRequest request = new LeaveRequest();
        request.setEmployeeId(currentUser.getEmployeeId());
        request.setEmployeeName(currentUser.getFullName());
        request.setStartDate(start);
        request.setEndDate(end);
        request.setLeaveType((String) leaveTypeCombo.getSelectedItem());
        request.setReason(reason);
        
        try {
            if (employeeService.submitLeaveRequest(request)) {
                controller.showInfo("LEAVE REQUEST SUBMITTED SUCCESSFULLY");
                reasonArea.setText("");
                setDefaultDates();
                loadLeaveHistory();
            }
        } catch (IllegalArgumentException e) {
            controller.showError(e.getMessage());
        } catch (Exception e) {
            controller.showError("ERROR SUBMITTING REQUEST: " + e.getMessage());
        }
    }
}