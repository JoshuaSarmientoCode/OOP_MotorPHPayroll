package main;

import ui.*;
import model.*;
import service.*;
import dao.*;
import javax.swing.*;
import java.io.File;
import java.util.*;

public class MainController {

    private UserService userService;
    private EmployeeService employeeService;
    private PayrollService payrollService;
    private AttendanceService attendanceService;
    private ValidationService validationService;
    private TicketService ticketService;
    private SystemLogService systemLogService;

    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private LeaveRequestDAO leaveDAO;
    private PayrollDAO payrollDAO;
    private UserDAO userDAO;
    private TicketDAO ticketDAO;
    private SystemLogDAO systemLogDAO;

    private MainFrame mainFrame;
    private User currentUser;
    private Employee currentEmployee;

    private Stack<NavigationState> navigationHistory = new Stack<>();
    private static final int MAX_HISTORY_SIZE = 10;

    private static final String DATA_DIR = "data/";
    private static final String EMPLOYEE_FILE = "MotorPH_Employee_Details.csv";
    private static final String ATTENDANCE_FILE = "MotorPH_Attendance_Record.csv";
    private static final String LEAVE_FILE = "leave_requests.csv";
    private static final String PAYROLL_FILE = "payroll.csv";
    private static final String USERS_FILE = "users.csv";
    private static final String TICKET_FILE = "tickets.csv";
    private static final String SYSTEM_LOG_FILE = "system_logs.csv";

    private static class NavigationState {
        JPanel panel;
        String name;
        Object state;

        NavigationState(JPanel panel, String name) {
            this.panel = panel;
            this.name = name;
        }

        NavigationState(JPanel panel, String name, Object state) {
            this(panel, name);
            this.state = state;
        }
    }

    public MainController() {
        initializeDataDirectory();
        initializeDAOs();
        initializeServices();
    }

    private void initializeDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    private void initializeDAOs() {
        this.employeeDAO = new EmployeeDAO(DATA_DIR + EMPLOYEE_FILE);
        this.attendanceDAO = new AttendanceDAO(DATA_DIR + ATTENDANCE_FILE);
        this.leaveDAO = new LeaveRequestDAO(DATA_DIR + LEAVE_FILE);
        this.payrollDAO = new PayrollDAO(DATA_DIR + PAYROLL_FILE);
        this.userDAO = new UserDAO(DATA_DIR + USERS_FILE);
        this.ticketDAO = new TicketDAO(DATA_DIR + TICKET_FILE);
        this.systemLogDAO = new SystemLogDAO(DATA_DIR + SYSTEM_LOG_FILE);
    }

    private void initializeServices() {
        this.validationService = new ValidationService();
        this.userDAO.setEmployeeDAO(this.employeeDAO);
        this.employeeService = new EmployeeService(employeeDAO, attendanceDAO, leaveDAO, validationService);
        this.attendanceService = new AttendanceService(attendanceDAO, this.employeeService);
        this.payrollService = new PayrollService(payrollDAO, employeeDAO, attendanceDAO);
        this.userService = new UserService(userDAO, employeeDAO, validationService);
        this.ticketService = new TicketService(ticketDAO, systemLogDAO);
        this.systemLogService = new SystemLogService(systemLogDAO);
    }

    public void start() {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Use default look and feel
            }
            mainFrame = new MainFrame(this);
            showLogin();
            mainFrame.setVisible(true);
            systemLogService.logInfo("System", null, "APPLICATION_START", "MotorPH Payroll System started");
        });
    }

    public void shutdown() {
        if (currentUser != null) {
            systemLogService.logInfo("System", currentUser, "APPLICATION_SHUTDOWN",
                    "User " + currentUser.getUsername() + " logged out");
        }
        navigationHistory.clear();
    }

    public void showLogin() {
        navigationHistory.clear();
        currentUser = null;
        currentEmployee = null;
        setMainContent(new LoginPanel(this), "LOGIN");
    }

    public void navigateTo(JPanel panel, String name) {
        navigateTo(panel, name, null);
    }

    public void navigateTo(JPanel panel, String name, Object state) {
        if (mainFrame.getCurrentPanel() != null) {
            NavigationState currentState = new NavigationState(
                    mainFrame.getCurrentPanel(),
                    getCurrentPanelName(),
                    getCurrentPanelState()
            );
            navigationHistory.push(currentState);
            if (navigationHistory.size() > MAX_HISTORY_SIZE)
                navigationHistory.removeElementAt(0);
        }
        setMainContent(panel, name, state);
    }

    public void goBack() {
        if (!navigationHistory.isEmpty()) {
            NavigationState previous = navigationHistory.pop();
            setMainContent(previous.panel, previous.name, previous.state);
        } else {
            showDashboard();
        }
    }

    private void setMainContent(JPanel panel, String name) {
        setMainContent(panel, name, null);
    }

    private void setMainContent(JPanel panel, String name, Object state) {
        mainFrame.setMainContent(panel);
        mainFrame.setCurrentPanelName(name);
        mainFrame.setCurrentPanelState(state);
    }

    private String getCurrentPanelName() { return mainFrame.getCurrentPanelName(); }
    private Object getCurrentPanelState() { return mainFrame.getCurrentPanelState(); }

    public void handleLogin(String employeeId, String password) {
        try {
            boolean success = userService.login(employeeId, password);

            if (success) {
                currentUser = userService.getCurrentUser();

                if (currentUser != null && currentUser.getEmployee() == null) {
                    currentEmployee = employeeService.getEmployeeById(employeeId);
                    if (currentEmployee != null) currentUser.setEmployee(currentEmployee);
                } else if (currentUser != null) {
                    currentEmployee = currentUser.getEmployee();
                } else {
                    currentEmployee = employeeService.getEmployeeById(employeeId);
                }

                if (currentEmployee == null) {
                    showError("Employee profile not found!");
                    showLogin();
                } else {
                    systemLogService.logInfo("Authentication", currentUser,
                            "LOGIN_SUCCESS", "User logged in successfully");
                    showDashboard();
                }
            } else {
                systemLogService.logWarning("Authentication", null,
                        "LOGIN_FAILED", "Failed login attempt for ID: " + employeeId);
                showError("Invalid Employee ID or Password!");
            }
        } catch (Exception e) {
            showError("Login error: " + e.getMessage());
        }
    }

    public void handleLogout() {
        if (currentUser != null) {
            systemLogService.logInfo("Authentication", currentUser, "LOGOUT", "User logged out");
        }
        userService.logout();
        currentUser = null;
        currentEmployee = null;
        navigationHistory.clear();
        showLogin();
    }

    public void showDashboard() {
        if (currentUser == null || currentEmployee == null) {
            showLogin();
            return;
        }
        MainDashboardPanel dashboard = new MainDashboardPanel(
                this, userService, employeeService, payrollService,
                attendanceService, ticketService, systemLogService,
                currentUser, currentEmployee
        );
        navigateTo(dashboard, "DASHBOARD");
    }

    public void showEmployeeManagement() {
        try {
            checkAccess("EMPLOYEE_MANAGEMENT");
            EmployeeManagementPanel panel = new EmployeeManagementPanel(this, employeeService, userService);
            navigateTo(panel, "EMPLOYEE_MANAGEMENT");
            systemLogService.logAudit("Navigation", currentUser,
                    "ACCESS_EMPLOYEE_MANAGEMENT", "User accessed Employee Management panel");
        } catch (SecurityException e) {
            showError("Access Denied!");
        }
    }

    public void showAttendance() {
        AttendancePanel panel = new AttendancePanel(this, employeeService, attendanceService, currentUser);
        navigateTo(panel, "ATTENDANCE");
    }

    public void showPayslip() {
        PayslipPanel panel = new PayslipPanel(this, payrollService, employeeService, currentUser, currentEmployee);
        navigateTo(panel, "PAYSLIP");
    }

    public void showPayrollProcessing() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.FINANCE);
            PayrollProcessingPanel panel = new PayrollProcessingPanel(this, payrollService, employeeService, currentUser);
            navigateTo(panel, "PAYROLL_PROCESSING");
            systemLogService.logAudit("Navigation", currentUser,
                    "ACCESS_PAYROLL_PROCESSING", "User accessed Payroll Processing panel");
        } catch (SecurityException e) {
            showError("Access Denied! This feature is for Finance and Admin only.");
        }
    }

    public void showLeaveRequest() {
        LeaveRequestPanel panel = new LeaveRequestPanel(this, employeeService, currentUser);
        navigateTo(panel, "LEAVE_REQUEST");
    }

    public void showLeaveApprovals() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.HR);
            LeaveApprovalsPanel panel = new LeaveApprovalsPanel(this, employeeService, currentUser);
            navigateTo(panel, "LEAVE_APPROVALS");
            systemLogService.logAudit("Navigation", currentUser,
                    "ACCESS_LEAVE_APPROVALS", "User accessed Leave Approvals panel");
        } catch (SecurityException e) {
            showError("Access Denied!");
        }
    }

    public void showSubmitTicket() {
        SubmitTicketPanel panel = new SubmitTicketPanel(this, ticketService, systemLogService, currentUser);
        navigateTo(panel, "SUBMIT_TICKET");
    }

    public void showTicketManagement() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.IT);
            TicketManagementPanel panel = new TicketManagementPanel(this, ticketService, systemLogService, currentUser);
            navigateTo(panel, "TICKET_MANAGEMENT");
            systemLogService.logAudit("Navigation", currentUser,
                    "ACCESS_TICKET_MANAGEMENT", "User accessed Ticket Management panel");
        } catch (SecurityException e) {
            showError("Access Denied! This feature is for IT and Admin only.");
        }
    }

    public void showSystemLogs() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.IT);
            SystemLogsPanel panel = new SystemLogsPanel(this, systemLogService, currentUser);
            navigateTo(panel, "SYSTEM_LOGS");
            systemLogService.logAudit("Navigation", currentUser,
                    "ACCESS_SYSTEM_LOGS", "User accessed System Logs panel");
        } catch (SecurityException e) {
            showError("Access Denied! This feature is for IT and Admin only.");
        }
    }

    public void showEmployeeDetails(Employee employee) {
        EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(mainFrame, employee);
        dialog.setVisible(true);
        systemLogService.logInfo("Employee Management", currentUser,
                "VIEW_EMPLOYEE_DETAILS", "Viewed details for employee: " + employee.getEmployeeId());
    }

    public void showEmployeeDialog(Employee employee, String title) {
        EmployeeDialog dialog = new EmployeeDialog(
                mainFrame, title, employee, employeeService, validationService, this);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            String action = employee == null ? "ADD_EMPLOYEE" : "EDIT_EMPLOYEE";
            String details = employee == null ? "Added new employee" : "Edited employee: " + employee.getEmployeeId();
            systemLogService.logAudit("Employee Management", currentUser, action, details);
            refreshCurrentPanel();
        }
    }

    public boolean showConfirm(String message, String title) {
        return JOptionPane.showConfirmDialog(mainFrame, message, title,
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public void showInfo(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    public void showWarning(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public String showInput(String message, String initialValue) {
        return JOptionPane.showInputDialog(mainFrame, message, initialValue);
    }

    private void checkAccess(String feature) {
        if (currentUser == null) throw new SecurityException("Not logged in");
        if (!userService.hasAccess(feature)) {
            systemLogService.logWarning("Security", currentUser,
                    "ACCESS_DENIED", "Access denied to feature: " + feature);
            throw new SecurityException("Access denied to: " + feature);
        }
    }

    private void checkAnyRole(User.Role... roles) {
        if (currentUser == null) throw new SecurityException("Not logged in");
        for (User.Role role : roles) {
            if (currentUser.getRole() == role) return;
        }
        systemLogService.logWarning("Security", currentUser,
                "ROLE_ACCESS_DENIED", "Required roles: " + Arrays.toString(roles));
        throw new SecurityException("Insufficient privileges");
    }

    public void refreshCurrentPanel() {
        JPanel current = mainFrame.getCurrentPanel();
        String name = mainFrame.getCurrentPanelName();
        if (current == null || name == null) return;
        JPanel newPanel = recreatePanel(name);
        if (newPanel != null) setMainContent(newPanel, name, mainFrame.getCurrentPanelState());
    }

    private JPanel recreatePanel(String name) {
        switch (name) {
            case "DASHBOARD":
                return new MainDashboardPanel(this, userService, employeeService,
                        payrollService, attendanceService, ticketService, systemLogService,
                        currentUser, currentEmployee);
            case "EMPLOYEE_MANAGEMENT":
                return new EmployeeManagementPanel(this, employeeService, userService);
            case "ATTENDANCE":
                return new AttendancePanel(this, employeeService, attendanceService, currentUser);
            case "PAYSLIP":
                return new PayslipPanel(this, payrollService, employeeService, currentUser, currentEmployee);
            case "PAYROLL_PROCESSING":
                return new PayrollProcessingPanel(this, payrollService, employeeService, currentUser);
            case "LEAVE_REQUEST":
                return new LeaveRequestPanel(this, employeeService, currentUser);
            case "LEAVE_APPROVALS":
                return new LeaveApprovalsPanel(this, employeeService, currentUser);
            case "SUBMIT_TICKET":
                return new SubmitTicketPanel(this, ticketService, systemLogService, currentUser);
            case "TICKET_MANAGEMENT":
                return new TicketManagementPanel(this, ticketService, systemLogService, currentUser);
            case "SYSTEM_LOGS":
                return new SystemLogsPanel(this, systemLogService, currentUser);
            default:
                return null;
        }
    }

    public UserService getUserService() { return userService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public PayrollService getPayrollService() { return payrollService; }
    public AttendanceService getAttendanceService() { return attendanceService; }
    public ValidationService getValidationService() { return validationService; }
    public TicketService getTicketService() { return ticketService; }
    public SystemLogService getSystemLogService() { return systemLogService; }
    public User getCurrentUser() { return currentUser; }
    public Employee getCurrentEmployee() { return currentEmployee; }
    public MainFrame getMainFrame() { return mainFrame; }

    public void refreshAllData() {
        employeeDAO.refresh();
        attendanceDAO.refresh();
        leaveDAO.refresh();
        payrollDAO.refresh();
        userDAO.refresh();
        ticketDAO.refresh();
        systemLogDAO.refresh();

        if (currentEmployee != null) {
            currentEmployee = employeeService.getEmployeeById(currentEmployee.getEmployeeId());
            if (currentUser != null) currentUser.setEmployee(currentEmployee);
        }

        systemLogService.logInfo("System", currentUser, "DATA_REFRESH", "All data refreshed from files");
        refreshCurrentPanel();
    }

    public void backupData() {
        refreshAllData();
        showInfo("Data refreshed successfully");
    }
}