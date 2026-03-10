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
import java.util.List;

public class MainDashboardPanel extends JPanel {

    private final MainController controller;
    private final UserService userService;
    private final EmployeeService employeeService;
    private final PayrollService payrollService;
    private final AttendanceService attendanceService;
    private final TicketService ticketService;
    private final SystemLogService systemLogService;
    private final User currentUser;
    private final Employee currentEmployee;

    private JPanel contentArea;
    private CardLayout contentLayout;
    private JLabel clockLabel;
    private JLabel dateLabel;
    private javax.swing.Timer clockTimer;
    private JLabel attendanceStatusLabel;
    private JButton timeInBtn;
    private JButton timeOutBtn;
    private JButton logoutBtn;

    public MainDashboardPanel(MainController controller, UserService userService,
                              EmployeeService employeeService, PayrollService payrollService,
                              AttendanceService attendanceService, TicketService ticketService,
                              SystemLogService systemLogService, User currentUser,
                              Employee currentEmployee) {
        this.controller = controller;
        this.userService = userService;
        this.employeeService = employeeService;
        this.payrollService = payrollService;
        this.attendanceService = attendanceService;
        this.ticketService = ticketService;
        this.systemLogService = systemLogService;
        this.currentUser = currentUser;
        this.currentEmployee = currentEmployee;

        initializePanel();
        startClock();
        updateAttendanceButtonStates();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);

        // ===== SIDEBAR =====
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.CARD_BG);
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR));

        JLabel brandLabel = new JLabel("MOTORPH");
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        brandLabel.setForeground(UITheme.ACCENT_DARK);
        brandLabel.setBorder(BorderFactory.createEmptyBorder(40, 30, 30, 0));
        brandLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brandLabel);

        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

        // ===== GENERAL NAV (all roles) =====
        addSectionLabel(sidebar, "GENERAL");
        sidebar.add(createNavBtn("DASHBOARD",        e -> showDashboard()));
        sidebar.add(createNavBtn("ATTENDANCE",       e -> controller.showAttendance()));
        sidebar.add(createNavBtn("PAYSLIP",          e -> controller.showPayslip()));
        sidebar.add(createNavBtn("LEAVE REQUEST",    e -> controller.showLeaveRequest()));
        sidebar.add(createNavBtn("SUBMIT TICKET",    e -> controller.showSubmitTicket()));
        sidebar.add(createNavBtn("CHANGE PASSWORD",  e -> controller.showChangePasswordDialog()));

        // ===== ADMINISTRATION (Admin only for payroll; Admin + HR for employee/leave mgmt) =====
        if (currentEmployee instanceof AdminEmployee || currentEmployee instanceof HREmployee) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            addSectionLabel(sidebar, "ADMINISTRATION");
            sidebar.add(createNavBtn("EMPLOYEE MANAGEMENT", e -> controller.showEmployeeManagement()));
            sidebar.add(createNavBtn("LEAVE APPROVALS",     e -> controller.showLeaveApprovals()));
        }

        // Payroll Processing — Admin and Finance only
        if (currentEmployee instanceof AdminEmployee || currentEmployee instanceof FinanceEmployee) {
            if (!(currentEmployee instanceof AdminEmployee || currentEmployee instanceof HREmployee)) {
                // Finance-only: add section header if not already added
                sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
                addSectionLabel(sidebar, "ADMINISTRATION");
            }
            sidebar.add(createNavBtn("PAYROLL PROCESSING",  e -> controller.showPayrollProcessing()));
        }

        // ===== IT TOOLS (IT / Admin) =====
        if (currentEmployee instanceof ITEmployee || currentEmployee instanceof AdminEmployee) {
            sidebar.add(Box.createRigidArea(new Dimension(0, 15)));
            addSectionLabel(sidebar, "IT TOOLS");
            sidebar.add(createNavBtn("TICKET MANAGEMENT", e -> controller.showTicketManagement()));
            sidebar.add(createNavBtn("SYSTEM LOGS",       e -> controller.showSystemLogs()));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createFooter());
        add(sidebar, BorderLayout.WEST);

        // ===== MAIN CONTENT =====
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(UITheme.BG_PRIMARY);
        contentArea.add(createDashboardContent(), "DASHBOARD");
        add(contentArea, BorderLayout.CENTER);
    }

    // ===== SIDEBAR HELPERS =====

    private void addSectionLabel(JPanel panel, String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BOLD_SMALL_FONT);
        label.setForeground(UITheme.TEXT_SECONDARY);
        label.setBorder(BorderFactory.createEmptyBorder(0, 30, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
    }

    private JButton createNavBtn(String text, ActionListener al) {
        JButton btn = UITheme.createDashboardButton(text);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(240, 40));
        btn.addActionListener(al);
        return btn;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(UITheme.CARD_BG);
        footer.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));
        footer.setMaximumSize(new Dimension(280, 100));

        logoutBtn = new JButton("SIGN OUT");
        logoutBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBackground(new Color(33, 33, 33));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setOpaque(true);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(220, 45));

        logoutBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { logoutBtn.setBackground(new Color(55, 55, 55)); }
            public void mouseExited(MouseEvent e)  { logoutBtn.setBackground(new Color(33, 33, 33)); }
        });

        logoutBtn.addActionListener(e -> controller.handleLogout());
        footer.add(logoutBtn);
        return footer;
    }

    // ===== DASHBOARD CONTENT =====

    private JPanel createDashboardContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // User identity header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        String fullName = currentEmployee != null
                ? currentEmployee.getFullName() : currentUser.getUsername();
        String empId = currentEmployee != null
                ? currentEmployee.getEmployeeId() : "—";
        String department = currentEmployee != null && currentEmployee.getDepartment() != null
                ? currentEmployee.getDepartment() : "—";
        String position = currentEmployee != null && currentEmployee.getPosition() != null
                ? currentEmployee.getPosition() : "—";

        JLabel nameLabel = new JLabel(fullName);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);

        JLabel idLabel = new JLabel("Employee No. " + empId);
        idLabel.setFont(UITheme.NORMAL_FONT);
        idLabel.setForeground(UITheme.TEXT_SECONDARY);

        JLabel deptRoleLabel = new JLabel(department + "  ·  " + position);
        deptRoleLabel.setFont(UITheme.NORMAL_FONT);
        deptRoleLabel.setForeground(UITheme.TEXT_SECONDARY);

        header.add(nameLabel);
        header.add(Box.createRigidArea(new Dimension(0, 4)));
        header.add(idLabel);
        header.add(Box.createRigidArea(new Dimension(0, 2)));
        header.add(deptRoleLabel);

        panel.add(header);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));
        panel.add(createAttendanceCard());
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Metrics row
        JPanel metrics = new JPanel(new GridLayout(1, 2, 20, 0));
        metrics.setOpaque(false);
        metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        metrics.add(UITheme.createMetricCard("SALARY STATUS", getSalaryStatus()));
        metrics.add(UITheme.createMetricCard("TOTAL HOURS",
                String.format("%.1f Hours Logged", calculateMonthlyHours())));

        panel.add(metrics);
        panel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Info cards
        JPanel info = new JPanel(new GridLayout(1, 2, 20, 0));
        info.setOpaque(false);
        info.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        String bdayStr = (currentEmployee.getBirthDate() != null)
                ? currentEmployee.getBirthDate().toString() : "N/A";

        String[][] personalData = {
                {"Full Name",     currentEmployee.getFullName()},
                {"Birthday",      bdayStr},
                {"Phone Number",  currentEmployee.getPhoneNumber()},
                {"Email Address", currentEmployee.getEmail()},
                {"Home Address",  truncate(currentEmployee.getAddress(), 35)}
        };
        info.add(UITheme.createDataCard("PERSONAL INFORMATION", personalData));

        String[][] employmentData = {
                {"Work Status",    currentEmployee.getStatus().toString()},
                {"Position",       currentEmployee.getPosition()},
                {"Department",     currentEmployee.getDepartment()},
                {"Immediate Sup.", currentEmployee.getImmediateSupervisor()},
                {"Base Salary",    String.format("PHP %,.2f", currentEmployee.getBasicSalary())}
        };
        info.add(UITheme.createDataCard("EMPLOYMENT OVERVIEW", employmentData));

        panel.add(info);
        return panel;
    }

    private JPanel createAttendanceCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.add(UITheme.createSectionHeader("ATTENDANCE CONTROL"), BorderLayout.WEST);

        attendanceStatusLabel = new JLabel("SYNCING...");
        attendanceStatusLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleRow.add(attendanceStatusLabel, BorderLayout.EAST);

        card.add(titleRow);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel mainRow = new JPanel(new BorderLayout());
        mainRow.setOpaque(false);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnPanel.setOpaque(false);
        timeInBtn  = UITheme.createPrimaryButton("TIME IN",  UITheme.ACCENT_GREEN);
        timeOutBtn = UITheme.createPrimaryButton("TIME OUT", UITheme.ACCENT_RED);
        timeInBtn.setPreferredSize(new Dimension(110, 40));
        timeOutBtn.setPreferredSize(new Dimension(110, 40));
        btnPanel.add(timeInBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        btnPanel.add(timeOutBtn);
        mainRow.add(btnPanel, BorderLayout.WEST);

        JPanel clockPanel = new JPanel(new GridLayout(2, 1, 0, 0));
        clockPanel.setOpaque(false);
        dateLabel  = new JLabel("", SwingConstants.RIGHT);
        clockLabel = new JLabel("", SwingConstants.RIGHT);
        clockLabel.setFont(new Font("Monospaced", Font.BOLD, 22));
        clockPanel.add(dateLabel);
        clockPanel.add(clockLabel);
        mainRow.add(clockPanel, BorderLayout.EAST);

        card.add(mainRow);

        timeInBtn.addActionListener(e -> timeIn());
        timeOutBtn.addActionListener(e -> timeOut());

        return card;
    }

    // ===== ATTENDANCE LOGIC =====

    private void updateAttendanceButtonStates() {
        try {
            Attendance today = attendanceService.getTodayAttendance(currentUser.getEmployeeId());
            if (today == null) {
                timeInBtn.setEnabled(true);
                timeOutBtn.setEnabled(false);
                attendanceStatusLabel.setText("NOT CLOCKED IN");
                attendanceStatusLabel.setForeground(UITheme.TEXT_SECONDARY);
            } else if (today.getTimeOut() == null) {
                timeInBtn.setEnabled(false);
                timeOutBtn.setEnabled(true);
                attendanceStatusLabel.setText("SHIFT IN PROGRESS");
                attendanceStatusLabel.setForeground(UITheme.ACCENT_GREEN);
            } else {
                timeInBtn.setEnabled(false);
                timeOutBtn.setEnabled(false);
                attendanceStatusLabel.setText("SHIFT COMPLETED");
                attendanceStatusLabel.setForeground(UITheme.ACCENT_BLUE);
            }
        } catch (Exception e) {
            timeInBtn.setEnabled(true);
        }
    }

    private void timeIn() {
        try {
            if (attendanceService.timeIn(currentUser.getEmployeeId()))
                updateAttendanceButtonStates();
        } catch (Exception e) { controller.showError(e.getMessage()); }
    }

    private void timeOut() {
        try {
            if (attendanceService.timeOut(currentUser.getEmployeeId()))
                updateAttendanceButtonStates();
        } catch (Exception e) { controller.showError(e.getMessage()); }
    }

    // ===== CLOCK =====

    private void startClock() {
        clockTimer = new javax.swing.Timer(1000, e -> {
            LocalDateTime now = LocalDateTime.now();
            dateLabel.setText(now.format(
                    DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy")).toUpperCase());
            clockLabel.setText(now.format(DateTimeFormatter.ofPattern("hh:mm:ss a")));
        });
        clockTimer.start();
    }

    // ===== HELPERS =====

    private void showDashboard() {
        contentArea.revalidate();
        contentArea.repaint();
        contentLayout.show(contentArea, "DASHBOARD");
    }

    private int calculateDaysPresent() {
        try {
            return attendanceService.getAttendanceForMonth(
                    currentUser.getEmployeeId(),
                    YearMonth.now()).size();
        } catch (Exception e) { return 0; }
    }

    private double calculateMonthlyHours() {
        try {
            return attendanceService.getTotalHoursWorked(
                    currentUser.getEmployeeId(),
                    YearMonth.now().atDay(1),
                    YearMonth.now().atEndOfMonth());
        } catch (Exception e) { return 0.0; }
    }

    /**
     * Shows salary readiness for the current month.
     * - If payroll is processed → shows net pay
     * - If not yet processed → "Pending"
     */
    private String getSalaryStatus() {
        try {
            YearMonth currentMonth = YearMonth.now();
            boolean payrollReady = payrollService.hasPayroll(
                    currentUser.getEmployeeId(), currentMonth);

            if (payrollReady) {
                List<Payslip> payslips = payrollService.getEmployeePayslips(
                        currentUser.getEmployeeId());
                if (!payslips.isEmpty() && payslips.get(0).getPeriod().equals(currentMonth)) {
                    return String.format("Ready — ₱%,.0f Net", payslips.get(0).getNetPay());
                }
                return "Salary Ready!";
            }

            return "Pending";

        } catch (Exception e) {
            return "—";
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}