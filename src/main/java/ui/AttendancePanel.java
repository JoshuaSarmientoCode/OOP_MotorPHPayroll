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

public class AttendancePanel extends JPanel {
    
    private final MainController controller;
    private final EmployeeService employeeService;
    private final AttendanceService attendanceService;
    private final User currentUser;
    
    // Constants
    private static final YearMonth COMPANY_FOUNDING_DATE = YearMonth.of(2024, 6);
    
    // UI Components
    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JComboBox<YearMonth> periodCombo;
    private JLabel summaryLabel;
    private JPanel recentHistoryPanel;
    private JButton backBtn;
    private JButton refreshBtn;
    private JButton exportBtn;
    private JButton viewBtn; // Added View button
    
    // Statistics
    private JLabel totalHoursLabel;
    private JLabel presentDaysLabel;
    private JLabel overtimeLabel;
    private JLabel lateLabel;
    
    public AttendancePanel(MainController controller, EmployeeService employeeService,
                          AttendanceService attendanceService, User currentUser) {
        this.controller = controller;
        this.employeeService = employeeService;
        this.attendanceService = attendanceService;
        this.currentUser = currentUser;
        
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Split content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.35);
        splitPane.setBorder(null);
        
        splitPane.setLeftComponent(createRecentHistoryPanel());
        splitPane.setRightComponent(createFullHistoryPanel());
        
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
        
        JLabel titleLabel = new JLabel("ATTENDANCE RECORDS");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        // Right side with controls
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false);
        
        refreshBtn = UITheme.createDashboardButton("REFRESH");
        refreshBtn.addActionListener(e -> loadAttendanceData());
        controls.add(refreshBtn);
        
        exportBtn = UITheme.createDashboardButton("EXPORT");
        exportBtn.addActionListener(e -> exportData());
        controls.add(exportBtn);
        
        headerPanel.add(controls, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createRecentHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title
        JLabel titleLabel = UITheme.createSectionHeader("RECENT ATTENDANCE");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        totalHoursLabel = new JLabel("0.0", SwingConstants.CENTER);
        presentDaysLabel = new JLabel("0", SwingConstants.CENTER);
        overtimeLabel = new JLabel("0.0", SwingConstants.CENTER);
        lateLabel = new JLabel("0.0", SwingConstants.CENTER);
        
        statsPanel.add(createStatCard("TOTAL HOURS", totalHoursLabel, UITheme.ACCENT_BLUE));
        statsPanel.add(createStatCard("DAYS PRESENT", presentDaysLabel, UITheme.ACCENT_GREEN));
        statsPanel.add(createStatCard("OVERTIME", overtimeLabel, UITheme.ACCENT_ORANGE));
        statsPanel.add(createStatCard("LATE HOURS", lateLabel, UITheme.ACCENT_RED));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Recent history cards
        recentHistoryPanel = new JPanel();
        recentHistoryPanel.setLayout(new BoxLayout(recentHistoryPanel, BoxLayout.Y_AXIS));
        recentHistoryPanel.setBackground(UITheme.CARD_BG);
        
        JScrollPane scrollPane = new JScrollPane(recentHistoryPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.BOLD_SMALL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        valueLabel.setForeground(color);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFullHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title and filters
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel titleLabel = UITheme.createSectionHeader("HISTORY");
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        // Period selector with View button
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        
        filterPanel.add(new JLabel("PERIOD:"));
        
        periodCombo = new JComboBox<>();
        periodCombo.setFont(UITheme.NORMAL_FONT);
        periodCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        periodCombo.setPreferredSize(new Dimension(150, 35));
        
        // Add months from June 2024 up to current month
        YearMonth current = YearMonth.now();
        YearMonth start = COMPANY_FOUNDING_DATE;
        
        YearMonth month = current;
        while (!month.isBefore(start)) {
            periodCombo.addItem(month);
            month = month.minusMonths(1);
        }
        
        filterPanel.add(periodCombo);
        
        // Add View button
        viewBtn = UITheme.createPrimaryButton("VIEW", UITheme.ACCENT_BLUE);
        viewBtn.setPreferredSize(new Dimension(80, 35));
        viewBtn.addActionListener(e -> loadAttendanceData());
        filterPanel.add(viewBtn);
        
        topPanel.add(filterPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table
        String[] columns = {"DATE", "DAY", "TIME IN", "TIME OUT", "HOURS", "OVERTIME", "LATE", "STATUS"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(35);
        historyTable.setFont(UITheme.NORMAL_FONT);
        historyTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        historyTable.getTableHeader().setBackground(UITheme.HEADER_BG);
        historyTable.getTableHeader().setForeground(UITheme.TEXT_SECONDARY);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(UITheme.BORDER_COLOR);
        
        // Status column renderer
        historyTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value != null ? value.toString() : "";
                
                if ("COMPLETED".equals(status)) {
                    c.setForeground(UITheme.ACCENT_GREEN);
                } else if ("HALF_DAY".equals(status)) {
                    c.setForeground(UITheme.ACCENT_ORANGE);
                } else if ("UNDERTIME".equals(status)) {
                    c.setForeground(UITheme.ACCENT_RED);
                } else if ("CLOCKED_IN".equals(status)) {
                    c.setForeground(UITheme.ACCENT_BLUE);
                } else {
                    c.setForeground(UITheme.TEXT_SECONDARY);
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Summary
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        summaryLabel = new JLabel("TOTAL HOURS: 0.0 | DAYS PRESENT: 0 | OVERTIME: 0.0 | LATE: 0.0");
        summaryLabel.setFont(UITheme.BOLD_SMALL_FONT);
        summaryLabel.setForeground(UITheme.TEXT_PRIMARY);
        summaryPanel.add(summaryLabel);
        
        panel.add(summaryPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadAttendanceData() {
        String employeeId = currentUser.getEmployeeId();
        YearMonth selectedPeriod = (YearMonth) periodCombo.getSelectedItem();
        
        if (selectedPeriod == null) {
            controller.showWarning("Please select a period");
            return;
        }
        
        LocalDate startDate = selectedPeriod.atDay(1);
        LocalDate endDate = selectedPeriod.atEndOfMonth();
        
        // Load full history for selected period
        loadFullHistory(employeeId, startDate, endDate);
        
        // Load recent history (last 7 days)
        loadRecentHistory(employeeId);
        
        // Update stats for selected period
        updateStatistics(employeeId, selectedPeriod);
    }
    
    private void loadFullHistory(String employeeId, LocalDate start, LocalDate end) {
        if (tableModel == null) return;
        
        tableModel.setRowCount(0);
        
        try {
            // Get records and create a mutable ArrayList
            List<Attendance> records = new ArrayList<>(attendanceService.getAttendanceForPeriod(employeeId, start, end));
            
            double totalHours = 0;
            double totalOvertime = 0;
            double totalLate = 0;
            int presentDays = 0;
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            
            // Sort records by date (newest first)
            records.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));
            
            for (Attendance a : records) {
                String day = a.getDate().getDayOfWeek().toString().substring(0, 3);
                String timeIn = a.getTimeIn() != null ? a.getTimeIn().format(timeFormatter) : "—";
                String timeOut = a.getTimeOut() != null ? a.getTimeOut().format(timeFormatter) : "—";
                String hours = a.getHoursWorked() > 0 ? String.format("%.1f", a.getHoursWorked()) : "—";
                String overtime = a.getOvertimeHours() > 0 ? String.format("%.1f", a.getOvertimeHours()) : "—";
                String late = a.getLateHours() > 0 ? String.format("%.1f", a.getLateHours()) : "—";
                
                tableModel.addRow(new Object[]{
                    a.getFormattedDate(),
                    day,
                    timeIn,
                    timeOut,
                    hours,
                    overtime,
                    late,
                    a.getStatus()
                });
                
                if (a.getTimeOut() != null) {
                    totalHours += a.getHoursWorked();
                    totalOvertime += a.getOvertimeHours();
                    totalLate += a.getLateHours();
                    presentDays++;
                }
            }
            
            summaryLabel.setText(String.format(
                "TOTAL HOURS: %.1f | DAYS PRESENT: %d | OVERTIME: %.1f | LATE: %.1f",
                totalHours, presentDays, totalOvertime, totalLate));
            
        } catch (Exception e) {
            controller.showError("Error loading attendance data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadRecentHistory(String employeeId) {
        if (recentHistoryPanel == null) return;
        
        recentHistoryPanel.removeAll();
        
        try {
            LocalDate today = LocalDate.now();
            LocalDate sevenDaysAgo = today.minusDays(7);
            
            // Get records and create a mutable ArrayList
            List<Attendance> recentRecords = new ArrayList<>(attendanceService.getAttendanceForPeriod(
                employeeId, sevenDaysAgo, today));
            
            if (recentRecords.isEmpty()) {
                JPanel emptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                emptyPanel.setBackground(UITheme.CARD_BG);
                
                JLabel emptyLabel = new JLabel("NO ATTENDANCE RECORDS IN THE LAST 7 DAYS");
                emptyLabel.setFont(UITheme.NORMAL_FONT);
                emptyLabel.setForeground(UITheme.TEXT_SECONDARY);
                emptyPanel.add(emptyLabel);
                
                recentHistoryPanel.add(emptyPanel);
            } else {
                // Sort records by date (newest first)
                recentRecords.sort((a1, a2) -> a2.getDate().compareTo(a1.getDate()));
                
                for (Attendance a : recentRecords) {
                    recentHistoryPanel.add(createRecentHistoryCard(a));
                    recentHistoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            
        } catch (Exception e) {
            JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            errorPanel.setBackground(UITheme.CARD_BG);
            JLabel errorLabel = new JLabel("ERROR LOADING DATA");
            errorLabel.setForeground(UITheme.ACCENT_RED);
            errorPanel.add(errorLabel);
            recentHistoryPanel.add(errorPanel);
        }
        
        recentHistoryPanel.revalidate();
        recentHistoryPanel.repaint();
    }
    
    private JPanel createRecentHistoryCard(Attendance attendance) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Date and day
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setBackground(UITheme.CARD_BG);
        
        String dayName = attendance.getDate().getDayOfWeek().toString().substring(0, 3) + ", " +
                        attendance.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        
        JLabel dateLabel = new JLabel(dayName);
        dateLabel.setFont(UITheme.SUBHEADER_FONT);
        dateLabel.setForeground(UITheme.TEXT_PRIMARY);
        headerRow.add(dateLabel, BorderLayout.WEST);
        
        String hoursStr = attendance.getHoursWorked() > 0 ?
            String.format("%.1f hrs", attendance.getHoursWorked()) : "—";
        JLabel hoursLabel = new JLabel(hoursStr);
        hoursLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        
        if ("COMPLETED".equals(attendance.getStatus())) {
            hoursLabel.setForeground(UITheme.ACCENT_GREEN);
        } else if (attendance.getTimeOut() == null && attendance.getTimeIn() != null) {
            hoursLabel.setForeground(UITheme.ACCENT_BLUE);
        } else {
            hoursLabel.setForeground(UITheme.TEXT_SECONDARY);
        }
        
        headerRow.add(hoursLabel, BorderLayout.EAST);
        
        card.add(headerRow);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Time details
        JPanel timeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        timeRow.setBackground(UITheme.CARD_BG);
        
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
        
        String timeInStr = attendance.getTimeIn() != null ?
            attendance.getTimeIn().format(timeFormatter) : "—";
        JLabel timeInLabel = new JLabel("IN: " + timeInStr);
        timeInLabel.setFont(UITheme.MONO_FONT);
        timeInLabel.setForeground(UITheme.TEXT_SECONDARY);
        timeRow.add(timeInLabel);
        
        String timeOutStr = attendance.getTimeOut() != null ?
            attendance.getTimeOut().format(timeFormatter) : "—";
        JLabel timeOutLabel = new JLabel("OUT: " + timeOutStr);
        timeOutLabel.setFont(UITheme.MONO_FONT);
        timeOutLabel.setForeground(UITheme.TEXT_SECONDARY);
        timeRow.add(timeOutLabel);
        
        if (attendance.getOvertimeHours() > 0) {
            JLabel otLabel = new JLabel("OT: " + String.format("%.1f", attendance.getOvertimeHours()) + "h");
            otLabel.setFont(UITheme.MONO_FONT);
            otLabel.setForeground(UITheme.ACCENT_ORANGE);
            timeRow.add(otLabel);
        }
        
        if (attendance.getLateHours() > 0) {
            JLabel lateLabel = new JLabel("LATE: " + String.format("%.1f", attendance.getLateHours()) + "h");
            lateLabel.setFont(UITheme.MONO_FONT);
            lateLabel.setForeground(UITheme.ACCENT_RED);
            timeRow.add(lateLabel);
        }
        
        card.add(timeRow);
        
        return card;
    }
    
    private void updateStatistics(String employeeId, YearMonth period) {
        try {
            Map<String, Object> summary = attendanceService.getMonthlySummary(employeeId, period);
            
            if (summary != null) {
                totalHoursLabel.setText(String.format("%.1f", (double) summary.getOrDefault("totalHours", 0.0)));
                presentDaysLabel.setText(String.valueOf((long) summary.getOrDefault("presentDays", 0L)));
                overtimeLabel.setText(String.format("%.1f", (double) summary.getOrDefault("totalOvertime", 0.0)));
                lateLabel.setText(String.format("%.1f", (double) summary.getOrDefault("totalLate", 0.0)));
            }
            
        } catch (Exception e) {
            // Keep default values
        }
    }
    
    private void exportData() {
        try {
            YearMonth period = (YearMonth) periodCombo.getSelectedItem();
            if (period == null) {
                controller.showWarning("Please select a period");
                return;
            }
            
            String filename = String.format("Attendance_%s_%s.csv",
                currentUser.getEmployeeId(),
                period.toString().replace("-", "_"));
            
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new java.io.File(filename));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                    // Write header
                    writer.println("Date,Day,Time In,Time Out,Hours,Overtime,Late,Status");
                    
                    // Write data
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        writer.println(
                            tableModel.getValueAt(i, 0) + "," +
                            tableModel.getValueAt(i, 1) + "," +
                            tableModel.getValueAt(i, 2) + "," +
                            tableModel.getValueAt(i, 3) + "," +
                            tableModel.getValueAt(i, 4) + "," +
                            tableModel.getValueAt(i, 5) + "," +
                            tableModel.getValueAt(i, 6) + "," +
                            tableModel.getValueAt(i, 7)
                        );
                    }
                    
                    controller.showInfo("Attendance data exported successfully");
                }
            }
            
        } catch (Exception e) {
            controller.showError("Error exporting data: " + e.getMessage());
        }
    }
}