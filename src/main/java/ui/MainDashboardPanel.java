package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class MainDashboardPanel extends JPanel {

    private final MainController controller;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final PayrollService payrollService;
    private final AttendanceService attendanceService;
    private final User currentUser;
    private final Employee currentEmployee;

    // UI Components
    private JPanel contentArea;
    private CardLayout contentLayout;
    private JLabel clockLabel;
    private JLabel dateLabel;
    private javax.swing.Timer clockTimer;
    private JLabel attendanceStatusLabel;
    private JButton timeInBtn;
    private JButton timeOutBtn;

    public MainDashboardPanel(MainController controller, UserService userService,
                              EmployeeService employeeService, PayrollService payrollService,
                              AttendanceService attendanceService, User currentUser,
                              Employee currentEmployee) {
        this.controller = controller;
        this.userService = userService;
        this.employeeService = employeeService;
        this.payrollService = payrollService;
        this.attendanceService = attendanceService;
        this.currentUser = currentUser;
        this.currentEmployee = currentEmployee;

        // Debug output
        System.out.println("=== MAIN DASHBOARD INITIALIZATION ===");
        System.out.println("Current User: " + (currentUser != null ? currentUser.getUsername() : "null"));
        System.out.println("Current Employee: " + (currentEmployee != null ? currentEmployee.getFullName() : "null"));
        System.out.println("User has employee: " + (currentUser != null && currentUser.getEmployee() != null));
        if (currentUser != null) {
            System.out.println("User employee ID: " + currentUser.getEmployeeId());
        }
        if (currentEmployee != null) {
            System.out.println("Employee ID: " + currentEmployee.getEmployeeId());
        }

        initializePanel();
        startClock();

        // Initial button state update
        SwingUtilities.invokeLater(() -> {
            updateAttendanceButtonStates();
        });
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);

        // Left sidebar with navigation
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);

        // Main content area
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(UITheme.BG_PRIMARY);
        contentArea.add(createDashboardContent(), "DASHBOARD");

        add(contentArea, BorderLayout.CENTER);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.CARD_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        // Brand
        JLabel brandLabel = new JLabel("MOTORPH");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        brandLabel.setForeground(UITheme.ACCENT_DARK);
        brandLabel.setBorder(BorderFactory.createEmptyBorder(40, 30, 30, 0));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brandLabel);

        // User info
        JPanel userInfo = createUserInfoPanel();
        userInfo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(userInfo);
        sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

        // Navigation sections
        addNavigationSection(sidebar, "GENERAL", new String[][]{
                {"DASHBOARD", "showDashboard"},
                {"ATTENDANCE", "showAttendance"},
                {"PAYSLIP", "showPayslip"},
                {"LEAVE REQUEST", "showLeaveRequest"}
        });

        // Role-specific sections
        if (currentEmployee instanceof AdminEmployee || currentEmployee instanceof HREmployee) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            addNavigationSection(sidebar, "ADMINISTRATION", new String[][]{
                    {"EMPLOYEE MANAGEMENT", "showEmployeeManagement"},
                    {"LEAVE APPROVALS", "showLeaveApprovals"},
                    {"PAYROLL PROCESSING", "showPayrollProcessing"}
            });
        }

        if (currentEmployee instanceof FinanceEmployee) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            addNavigationSection(sidebar, "FINANCE", new String[][]{
                    {"PAYROLL PROCESSING", "showPayrollProcessing"}
            });
        }

        if (currentEmployee instanceof ITEmployee) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            addNavigationSection(sidebar, "IT TOOLS", new String[][]{
                    {"USER ACCOUNTS", "showUserAccounts"},
                    {"SYSTEM LOGS", "showSystemLogs"}
            });
        }

        sidebar.add(Box.createVerticalGlue());

        // Footer with clock and logout
        JPanel footer = createFooter();
        footer.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footer);

        return sidebar;
    }

    private JPanel createUserInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        String roleName = currentEmployee != null ? currentEmployee.getRoleName() : "EMPLOYEE";
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : "N/A";

        JLabel nameLabel = new JLabel(currentEmployee != null ? currentEmployee.getFullName() : "Unknown");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(nameLabel);

        JLabel roleLabel = new JLabel(roleName + " | ID: " + employeeId);
        roleLabel.setFont(UITheme.SMALL_FONT);
        roleLabel.setForeground(UITheme.TEXT_SECONDARY);
        roleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(roleLabel);

        return panel;
    }

    private void addNavigationSection(JPanel sidebar, String sectionTitle, String[][] items) {
        JLabel sectionLabel = new JLabel(sectionTitle);
        sectionLabel.setFont(UITheme.BOLD_SMALL_FONT);
        sectionLabel.setForeground(UITheme.TEXT_SECONDARY);
        sectionLabel.setBorder(BorderFactory.createEmptyBorder(0, 30, 5, 0));
        sectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sectionLabel);

        for (String[] item : items) {
            JButton btn = createNavButton(item[0], item[1]);
            sidebar.add(btn);
            sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
        }
    }

    private JButton createNavButton(String text, String action) {
        JButton btn = UITheme.createDashboardButton(text);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(250, 45));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);

        btn.addActionListener(e -> {
            switch (action) {
                case "showDashboard": showDashboard(); break;
                case "showAttendance": controller.showAttendance(); break;
                case "showPayslip": controller.showPayslip(); break;
                case "showLeaveRequest": controller.showLeaveRequest(); break;
                case "showEmployeeManagement": controller.showEmployeeManagement(); break;
                case "showLeaveApprovals": controller.showLeaveApprovals(); break;
                case "showPayrollProcessing": controller.showPayrollProcessing(); break;
                case "showUserAccounts": showUserAccounts(); break;
                case "showSystemLogs": showSystemLogs(); break;
            }
        });

        return btn;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
        footer.setMaximumSize(new Dimension(280, 150));

        dateLabel = new JLabel();
        dateLabel.setFont(UITheme.BOLD_SMALL_FONT);
        dateLabel.setForeground(UITheme.TEXT_SECONDARY);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(dateLabel);

        footer.add(Box.createRigidArea(new Dimension(0, 5)));

        clockLabel = new JLabel();
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        clockLabel.setForeground(UITheme.TEXT_PRIMARY);
        clockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(clockLabel);

        footer.add(Box.createRigidArea(new Dimension(0, 20)));

        JButton logoutBtn = UITheme.createPrimaryButton("SIGN OUT", UITheme.ACCENT_DARK);
        logoutBtn.setMaximumSize(new Dimension(220, 45));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.addActionListener(e -> controller.handleLogout());
        footer.add(logoutBtn);

        return footer;
    }

    private JPanel createDashboardContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_PRIMARY);
        panel.setBorder(UITheme.PANEL_PADDING);

        // Welcome header
        panel.add(createWelcomeHeader());
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Attendance control card
        panel.add(createAttendanceCard());
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Metrics row (with 2 cards)
        panel.add(createMetricsRow());
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Info cards row
        panel.add(createInfoCards());

        return panel;
    }

    private JPanel createWelcomeHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        String firstName = currentEmployee != null ? currentEmployee.getFirstName() : "User";
        JLabel welcomeLabel = new JLabel("Welcome, " + firstName + "!");
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        welcomeLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(welcomeLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createAttendanceCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        // Title
        JLabel titleLabel = UITheme.createSectionHeader("ATTENDANCE CONTROL");
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        // Create status label first
        attendanceStatusLabel = new JLabel();
        attendanceStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statusPanel.setBackground(UITheme.CARD_BG);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Get current shift info
        Map<String, String> shiftInfo = new HashMap<>();
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if (employeeId != null) {
            try {
                shiftInfo = attendanceService.getCurrentShiftInfo(employeeId);
            } catch (Exception e) {
                e.printStackTrace();
                shiftInfo.put("status", "NOT CLOCKED IN");
                shiftInfo.put("timeIn", "—");
                shiftInfo.put("elapsed", "—");
            }
        } else {
            shiftInfo.put("status", "NO EMPLOYEE ID");
            shiftInfo.put("timeIn", "—");
            shiftInfo.put("elapsed", "—");
        }

        // Update status label
        String status = shiftInfo.get("status");
        attendanceStatusLabel.setText(status);

        // Set color based on status
        if ("CLOCKED_IN".equals(status) || "CLOCKED IN".equals(status)) {
            attendanceStatusLabel.setForeground(UITheme.ACCENT_GREEN);
        } else if ("COMPLETED".equals(status)) {
            attendanceStatusLabel.setForeground(UITheme.ACCENT_BLUE);
        } else {
            attendanceStatusLabel.setForeground(UITheme.TEXT_SECONDARY);
        }

        statusPanel.add(attendanceStatusLabel);

        // Time display
        if (!shiftInfo.get("timeIn").equals("—")) {
            JLabel timeInLabel = new JLabel("IN: " + shiftInfo.get("timeIn"));
            timeInLabel.setFont(UITheme.MONO_FONT);
            timeInLabel.setForeground(UITheme.TEXT_SECONDARY);
            statusPanel.add(timeInLabel);
        }

        if (shiftInfo.containsKey("timeOut")) {
            JLabel timeOutLabel = new JLabel("OUT: " + shiftInfo.get("timeOut"));
            timeOutLabel.setFont(UITheme.MONO_FONT);
            timeOutLabel.setForeground(UITheme.TEXT_SECONDARY);
            statusPanel.add(timeOutLabel);
        }

        if (shiftInfo.containsKey("elapsed") && !shiftInfo.get("elapsed").equals("—")) {
            JLabel elapsedLabel = new JLabel("ELAPSED: " + shiftInfo.get("elapsed"));
            elapsedLabel.setFont(UITheme.MONO_FONT);
            elapsedLabel.setForeground(UITheme.TEXT_SECONDARY);
            statusPanel.add(elapsedLabel);
        }

        card.add(statusPanel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(UITheme.CARD_BG);
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        timeInBtn = UITheme.createPrimaryButton("TIME IN", UITheme.ACCENT_GREEN);
        timeInBtn.setPreferredSize(new Dimension(100, 35));
        timeInBtn.addActionListener(e -> timeIn());
        buttonPanel.add(timeInBtn);

        timeOutBtn = UITheme.createPrimaryButton("TIME OUT", UITheme.ACCENT_RED);
        timeOutBtn.setPreferredSize(new Dimension(100, 35));
        timeOutBtn.addActionListener(e -> timeOut());
        buttonPanel.add(timeOutBtn);

        // Update button states based on status
        updateAttendanceButtonStates();

        card.add(buttonPanel);

        return card;
    }

    private JPanel createMetricsRow() {
        // GridLayout with 2 cards
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        YearMonth currentMonth = YearMonth.now();

        panel.add(UITheme.createMetricCard(
                "ATTENDANCE",
                getAttendanceMetric()
        ));

        double totalHours = 0;
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        if (employeeId != null) {
            try {
                totalHours = attendanceService.getTotalHoursWorked(
                        employeeId,
                        currentMonth.atDay(1),
                        currentMonth.atEndOfMonth()
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        panel.add(UITheme.createMetricCard(
                "HOURS THIS MONTH",
                String.format("%.1f hrs", totalHours)
        ));

        return panel;
    }

    private JPanel createInfoCards() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 15, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        // Personal details
        String[][] personalData = {
                {"Email", currentEmployee != null && currentEmployee.getEmail() != null ? currentEmployee.getEmail() : "—"},
                {"Contact", formatPhone(currentEmployee != null ? currentEmployee.getPhoneNumber() : null)},
                {"Birthdate", currentEmployee != null && currentEmployee.getBirthDate() != null ?
                        currentEmployee.getBirthDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "—"},
                {"Address", truncate(currentEmployee != null ? currentEmployee.getAddress() : "", 30)}
        };

        // Employment details
        String[][] employmentData = {
                {"Position", currentEmployee != null && currentEmployee.getPosition() != null ? currentEmployee.getPosition() : "—"},
                {"Department", currentEmployee != null ? currentEmployee.getDepartment() : "—"},
                {"Status", currentEmployee != null && currentEmployee.getStatus() != null ?
                        currentEmployee.getStatus().getDisplayName() : "—"},
                {"Supervisor", currentEmployee != null && currentEmployee.getImmediateSupervisor() != null ?
                        currentEmployee.getImmediateSupervisor() : "—"}
        };

        panel.add(UITheme.createDataCard("PERSONAL DETAILS", personalData));
        panel.add(UITheme.createDataCard("EMPLOYMENT DETAILS", employmentData));

        return panel;
    }

    // ========== ATTENDANCE METHODS ==========

    private String getAttendanceMetric() {
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        if (employeeId == null) return "N/A";

        try {
            Attendance today = attendanceService.getTodayAttendance(employeeId);
            if (today == null) return "NOT IN";
            if (today.getTimeOut() == null) return "ACTIVE";
            return "PRESENT";
        } catch (Exception e) {
            e.printStackTrace();
            return "NOT IN";
        }
    }

    private void timeIn() {
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        if (employeeId == null) {
            controller.showError("No employee ID found");
            return;
        }

        try {
            System.out.println("Attempting time in for employee: " + employeeId);

            // Disable buttons during operation
            timeInBtn.setEnabled(false);
            timeOutBtn.setEnabled(false);

            boolean success = attendanceService.timeIn(employeeId);

            if (success) {
                controller.showInfo("TIME IN RECORDED AT " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
                refreshDashboard();
            }
        } catch (IllegalArgumentException e) {
            controller.showError(e.getMessage());
            // Re-enable buttons based on current state
            updateAttendanceButtonStates();
        } catch (Exception e) {
            e.printStackTrace();
            controller.showError("Error during time in: " + e.getMessage());
        } finally {
            // Ensure buttons are re-enabled
            updateAttendanceButtonStates();
        }
    }

    private void timeOut() {
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;
        if (employeeId == null) {
            controller.showError("No employee ID found");
            return;
        }

        try {
            System.out.println("Attempting time out for employee: " + employeeId);

            // Disable buttons during operation
            timeInBtn.setEnabled(false);
            timeOutBtn.setEnabled(false);

            boolean success = attendanceService.timeOut(employeeId);

            if (success) {
                controller.showInfo("TIME OUT RECORDED AT " +
                        LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
                refreshDashboard();
            }
        } catch (IllegalArgumentException e) {
            controller.showError(e.getMessage());
            // Re-enable buttons based on current state
            updateAttendanceButtonStates();
        } catch (Exception e) {
            e.printStackTrace();
            controller.showError("Error during time out: " + e.getMessage());
        } finally {
            // Ensure buttons are re-enabled
            updateAttendanceButtonStates();
        }
    }

    private void updateAttendanceButtonStates() {
        String employeeId = currentUser != null ? currentUser.getEmployeeId() : null;

        try {
            Map<String, String> shiftInfo = attendanceService.getCurrentShiftInfo(employeeId);
            String status = shiftInfo.get("status");

            System.out.println("Updating button states based on status: '" + status + "'");

            // Update button states based on current status
            boolean timeInEnabled = "NOT CLOCKED IN".equals(status) || "NO_TIME_IN".equals(status);
            boolean timeOutEnabled = "CLOCKED_IN".equals(status) || "CLOCKED IN".equals(status);

            System.out.println("Time In enabled: " + timeInEnabled);
            System.out.println("Time Out enabled: " + timeOutEnabled);

            timeInBtn.setEnabled(timeInEnabled);
            timeOutBtn.setEnabled(timeOutEnabled);

            // Update status label
            if (attendanceStatusLabel != null) {
                attendanceStatusLabel.setText(status);
                if ("CLOCKED_IN".equals(status) || "CLOCKED IN".equals(status)) {
                    attendanceStatusLabel.setForeground(UITheme.ACCENT_GREEN);
                } else if ("COMPLETED".equals(status)) {
                    attendanceStatusLabel.setForeground(UITheme.ACCENT_BLUE);
                } else {
                    attendanceStatusLabel.setForeground(UITheme.TEXT_SECONDARY);
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating attendance button states: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshDashboard() {
        // Refresh the entire dashboard content
        contentArea.removeAll();
        contentArea.add(createDashboardContent(), "DASHBOARD");
        contentLayout.show(contentArea, "DASHBOARD");
        contentArea.revalidate();
        contentArea.repaint();
    }

    // ========== NAVIGATION METHODS ==========

    private void showDashboard() {
        contentLayout.show(contentArea, "DASHBOARD");
    }

    private void showUserAccounts() {
        controller.showInfo("User accounts feature coming soon");
    }

    private void showSystemLogs() {
        controller.showInfo("System logs feature coming soon");
    }

    // ========== CLOCK ==========

    private void startClock() {
        clockTimer = new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")).toUpperCase());
            clockLabel.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        });
        clockTimer.start();
    }

    // ========== UTILITY METHODS ==========

    private String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "—";
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.length() == 11) {
            return clean.substring(0, 4) + "-" + clean.substring(4, 7) + "-" + clean.substring(7);
        }
        if (clean.length() == 10) {
            return clean.substring(0, 3) + "-" + clean.substring(3, 6) + "-" + clean.substring(6);
        }
        return phone;
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}