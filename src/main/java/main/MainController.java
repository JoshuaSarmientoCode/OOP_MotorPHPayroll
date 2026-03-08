package main;

import ui.*;
import model.*;
import service.*;
import dao.*;
import javax.swing.*;
import java.io.File;
import java.util.*;

public class MainController {
    
    // ========== DEPENDENCIES ==========
    private UserService userService;
    private EmployeeService employeeService;
    private PayrollService payrollService;
    private AttendanceService attendanceService;
    private ValidationService validationService;
    
    // ========== DAOs ==========
    private EmployeeDAO employeeDAO;
    private AttendanceDAO attendanceDAO;
    private LeaveRequestDAO leaveDAO;
    private PayrollDAO payrollDAO;
    private UserDAO userDAO;
    
    // ========== UI COMPONENTS ==========
    private MainFrame mainFrame;
    private User currentUser;
    private Employee currentEmployee;
    
    // ========== NAVIGATION HISTORY ==========
    private Stack<NavigationState> navigationHistory = new Stack<>();
    private static final int MAX_HISTORY_SIZE = 10;
    
    // ========== CONFIGURATION ==========
    private static final String DATA_DIR = "data/";
    private static final String EMPLOYEE_FILE = "MotorPH_Employee_Details.csv";
    private static final String ATTENDANCE_FILE = "MotorPH_Attendance_Record.csv";
    private static final String LEAVE_FILE = "leave_requests.csv";
    private static final String PAYROLL_FILE = "payroll.csv";
    private static final String USERS_FILE = "users.csv";
    
    // ========== INNER CLASS FOR NAVIGATION ==========
    
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
    
    // ========== CONSTRUCTOR ==========
    
    public MainController() {
        System.out.println("Initializing MainController...");
        initializeDataDirectory();
        initializeDAOs();
        initializeServices();
        System.out.println("MainController initialized successfully");
    }
    
    // ========== INITIALIZATION ==========
    
    /**
     * Create data directory if it doesn't exist
     */
    private void initializeDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                System.out.println("Created data directory: " + DATA_DIR);
            } else {
                System.err.println("Failed to create data directory: " + DATA_DIR);
            }
        }
    }
    
    /**
     * Initialize all DAOs
     */
    private void initializeDAOs() {
        System.out.println("Initializing DAOs...");
        
        String employeePath = DATA_DIR + EMPLOYEE_FILE;
        String attendancePath = DATA_DIR + ATTENDANCE_FILE;
        String leavePath = DATA_DIR + LEAVE_FILE;
        String payrollPath = DATA_DIR + PAYROLL_FILE;
        String usersPath = DATA_DIR + USERS_FILE;
        
        // Create DAOs
        this.employeeDAO = new EmployeeDAO(employeePath);
        this.attendanceDAO = new AttendanceDAO(attendancePath);
        this.leaveDAO = new LeaveRequestDAO(leavePath);
        this.payrollDAO = new PayrollDAO(payrollPath);
        this.userDAO = new UserDAO(usersPath);
        
        System.out.println("DAOs initialized successfully");
        System.out.println("EmployeeDAO loaded: " + employeeDAO.count() + " employees");
        System.out.println("AttendanceDAO loaded: " + attendanceDAO.count() + " records");
    }
    
    /**
     * Initialize all services with dependency injection
     */
    private void initializeServices() {
        System.out.println("Initializing services...");
        
        this.validationService = new ValidationService();
        
        // Link UserDAO to EmployeeDAO
        this.userDAO.setEmployeeDAO(this.employeeDAO);
        
        // Initialize EmployeeService first (needed by AttendanceService)
        this.employeeService = new EmployeeService(employeeDAO, attendanceDAO, leaveDAO, validationService);
        
        // Initialize AttendanceService with EmployeeService
        this.attendanceService = new AttendanceService(attendanceDAO, this.employeeService);
        
        // Initialize other services
        this.payrollService = new PayrollService(payrollDAO, employeeDAO, attendanceDAO);
        this.userService = new UserService(userDAO, employeeDAO, validationService);
        
        System.out.println("Services initialized successfully");
    }
    
    // ========== APPLICATION LIFECYCLE ==========
    
    /**
     * Start the application
     */
    public void start() {
        System.out.println("Starting MotorPH Payroll System...");
        
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }
            
            mainFrame = new MainFrame(this);
            showLogin();
            mainFrame.setVisible(true);
            
            System.out.println("Application started successfully");
        });
    }
    
    /**
     * Shutdown application gracefully
     */
    public void shutdown() {
        System.out.println("Shutting down application...");
        
        // Clear navigation history
        navigationHistory.clear();
        
        System.out.println("Application shutdown complete");
    }
    
    // ========== NAVIGATION ==========
    
    /**
     * Show login panel
     */
    public void showLogin() {
        System.out.println("Navigating to Login panel");
        navigationHistory.clear();
        currentUser = null;
        currentEmployee = null;
        setMainContent(new LoginPanel(this), "LOGIN");
    }
    
    /**
     * Navigate to a panel
     */
    public void navigateTo(JPanel panel, String name) {
        navigateTo(panel, name, null);
    }
    
    /**
     * Navigate to a panel with state
     */
    public void navigateTo(JPanel panel, String name, Object state) {
        System.out.println("Navigating to: " + name);
        
        // Save current panel to history if it exists
        if (mainFrame.getCurrentPanel() != null) {
            NavigationState currentState = new NavigationState(
                mainFrame.getCurrentPanel(), 
                getCurrentPanelName(),
                getCurrentPanelState()
            );
            
            navigationHistory.push(currentState);
            
            // Limit history size
            if (navigationHistory.size() > MAX_HISTORY_SIZE) {
                navigationHistory.removeElementAt(0);
            }
        }
        
        setMainContent(panel, name, state);
    }
    
    /**
     * Go back to previous panel
     */
    public void goBack() {
        if (!navigationHistory.isEmpty()) {
            NavigationState previous = navigationHistory.pop();
            System.out.println("Going back to: " + previous.name);
            setMainContent(previous.panel, previous.name, previous.state);
        } else {
            System.out.println("No history, going to dashboard");
            showDashboard();
        }
    }
    
    /**
     * Set main content with name
     */
    private void setMainContent(JPanel panel, String name) {
        setMainContent(panel, name, null);
    }
    
    /**
     * Set main content with name and state
     */
    private void setMainContent(JPanel panel, String name, Object state) {
        mainFrame.setMainContent(panel);
        mainFrame.setCurrentPanelName(name);
        mainFrame.setCurrentPanelState(state);
    }
    
    /**
     * Get current panel name
     */
    private String getCurrentPanelName() {
        return mainFrame.getCurrentPanelName();
    }
    
    /**
     * Get current panel state
     */
    private Object getCurrentPanelState() {
        return mainFrame.getCurrentPanelState();
    }
    
    // ========== AUTHENTICATION ==========
    
    /**
     * Handle login attempt
     */
    public void handleLogin(String employeeId, String password) {
        System.out.println("Login attempt for employee ID: " + employeeId);
        
        try {
            // Attempt login
            boolean success = userService.login(employeeId, password);
            
            if (success) {
                currentUser = userService.getCurrentUser();
                currentEmployee = employeeService.getEmployeeById(employeeId);
                
                if (currentEmployee == null) {
                    System.err.println("Employee profile not found for ID: " + employeeId);
                    showError("Employee profile not found!");
                    showLogin();
                } else {
                    System.out.println("Login successful for: " + currentEmployee.getFullName() + 
                                       " (Role: " + currentUser.getRole() + ")");
                    showDashboard();
                }
            } else {
                System.err.println("Login failed for ID: " + employeeId);
                showError("Invalid Employee ID or Password!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Login error: " + e.getMessage());
        }
    }
    
    /**
     * Handle logout
     */
    public void handleLogout() {
        System.out.println("Logging out user: " + 
                   (currentUser != null ? currentUser.getUsername() : "unknown"));
        
        userService.logout();
        currentUser = null;
        currentEmployee = null;
        navigationHistory.clear();
        showLogin();
    }
    
    // ========== DASHBOARD NAVIGATION ==========
    
    /**
     * Show main dashboard
     */
    public void showDashboard() {
        if (currentUser == null || currentEmployee == null) {
            System.err.println("Cannot show dashboard: No user logged in");
            showLogin();
            return;
        }
        
        System.out.println("Showing dashboard for: " + currentEmployee.getFullName());
        
        MainDashboardPanel dashboard = new MainDashboardPanel(
            this, userService, employeeService, payrollService, 
            attendanceService, currentUser, currentEmployee
        );
        
        navigateTo(dashboard, "DASHBOARD");
    }
    
    /**
     * Show employee management panel
     */
    public void showEmployeeManagement() {
        try {
            checkAccess("EMPLOYEE_MANAGEMENT");
            
            System.out.println("Showing Employee Management panel");
            
            EmployeeManagementPanel panel = new EmployeeManagementPanel(
                this, employeeService, userService
            );
            
            navigateTo(panel, "EMPLOYEE_MANAGEMENT");
        } catch (SecurityException e) {
            showError("Access Denied!");
        }
    }
    
    /**
     * Show attendance panel
     */
    public void showAttendance() {
        System.out.println("Showing Attendance panel");
        
        AttendancePanel panel = new AttendancePanel(
            this, employeeService, attendanceService, currentUser
        );
        
        navigateTo(panel, "ATTENDANCE");
    }
    
    /**
     * Show payslip panel
     */
    public void showPayslip() {
        System.out.println("Showing Payslip panel");
        
        PayslipPanel panel = new PayslipPanel(
            this, payrollService, employeeService, currentUser, currentEmployee
        );
        
        navigateTo(panel, "PAYSLIP");
    }
    
    /**
     * Show payroll processing panel
     */
    public void showPayrollProcessing() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.HR, User.Role.FINANCE);
            
            System.out.println("Showing Payroll Processing panel");
            
            PayrollProcessingPanel panel = new PayrollProcessingPanel(
                this, payrollService, employeeService, currentUser
            );
            
            navigateTo(panel, "PAYROLL_PROCESSING");
        } catch (SecurityException e) {
            showError("Access Denied!");
        }
    }
    
    /**
     * Show leave request panel
     */
    public void showLeaveRequest() {
        System.out.println("Showing Leave Request panel");
        
        LeaveRequestPanel panel = new LeaveRequestPanel(
            this, employeeService, currentUser
        );
        
        navigateTo(panel, "LEAVE_REQUEST");
    }
    
    /**
     * Show leave approvals panel
     */
    public void showLeaveApprovals() {
        try {
            checkAnyRole(User.Role.ADMIN, User.Role.HR);
            
            System.out.println("Showing Leave Approvals panel");
            
            LeaveApprovalsPanel panel = new LeaveApprovalsPanel(
                this, employeeService, currentUser
            );
            
            navigateTo(panel, "LEAVE_APPROVALS");
        } catch (SecurityException e) {
            showError("Access Denied!");
        }
    }
    
    // ========== DIALOGS ==========
    
    /**
     * Show employee details dialog
     */
    public void showEmployeeDetails(Employee employee) {
        System.out.println("Showing employee details for: " + employee.getEmployeeId());
        
        EmployeeDetailsDialog dialog = new EmployeeDetailsDialog(mainFrame, employee);
        dialog.setVisible(true);
    }
    
    /**
     * Show employee edit dialog
     */
    public void showEmployeeDialog(Employee employee, String title) {
        System.out.println("Showing employee dialog: " + title);
        
        EmployeeDialog dialog = new EmployeeDialog(
            mainFrame, title, employee, employeeService, validationService, this
        );
        
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            refreshCurrentPanel();
        }
    }
    
    /**
     * Show confirmation dialog
     */
    public boolean showConfirm(String message, String title) {
        return JOptionPane.showConfirmDialog(
            mainFrame, message, title, JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
    }
    
    /**
     * Show info message
     */
    public void showInfo(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Information", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show warning message
     */
    public void showWarning(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Warning", 
            JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show error message
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(mainFrame, message, "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show input dialog
     */
    public String showInput(String message, String initialValue) {
        return JOptionPane.showInputDialog(mainFrame, message, initialValue);
    }
    
    // ========== ACCESS CONTROL ==========
    
    /**
     * Check if current user has access to feature
     */
    private void checkAccess(String feature) {
        if (currentUser == null) {
            throw new SecurityException("Not logged in");
        }
        
        if (!userService.hasAccess(feature)) {
            System.err.println("Access denied for user " + currentUser.getUsername() + 
                              " to feature: " + feature);
            throw new SecurityException("Access denied to: " + feature);
        }
    }
    
    /**
     * Check if current user has any of the specified roles
     */
    private void checkAnyRole(User.Role... roles) {
        if (currentUser == null) {
            throw new SecurityException("Not logged in");
        }
        
        for (User.Role role : roles) {
            if (currentUser.getRole() == role) {
                return;
            }
        }
        
        System.err.println("Access denied for user " + currentUser.getUsername() + 
                          " - required roles: " + Arrays.toString(roles));
        throw new SecurityException("Insufficient privileges");
    }
    
    // ========== REFRESH ==========
    
    /**
     * Refresh current panel
     */
    public void refreshCurrentPanel() {
        JPanel current = mainFrame.getCurrentPanel();
        String name = mainFrame.getCurrentPanelName();
        
        if (current == null || name == null) {
            return;
        }
        
        System.out.println("Refreshing panel: " + name);
        
        // Recreate panel based on name
        JPanel newPanel = recreatePanel(name);
        
        if (newPanel != null) {
            setMainContent(newPanel, name, mainFrame.getCurrentPanelState());
        }
    }
    
    /**
     * Recreate panel by name
     */
    private JPanel recreatePanel(String name) {
        switch (name) {
            case "DASHBOARD":
                return new MainDashboardPanel(this, userService, employeeService, 
                    payrollService, attendanceService, currentUser, currentEmployee);
            case "EMPLOYEE_MANAGEMENT":
                return new EmployeeManagementPanel(this, employeeService, userService);
            case "ATTENDANCE":
                return new AttendancePanel(this, employeeService, attendanceService, currentUser);
            case "PAYSLIP":
                return new PayslipPanel(this, payrollService, employeeService, 
                    currentUser, currentEmployee);
            case "PAYROLL_PROCESSING":
                return new PayrollProcessingPanel(this, payrollService, employeeService, currentUser);
            case "LEAVE_REQUEST":
                return new LeaveRequestPanel(this, employeeService, currentUser);
            case "LEAVE_APPROVALS":
                return new LeaveApprovalsPanel(this, employeeService, currentUser);
            default:
                return null;
        }
    }
    
    // ========== GETTERS ==========
    
    public UserService getUserService() { return userService; }
    public EmployeeService getEmployeeService() { return employeeService; }
    public PayrollService getPayrollService() { return payrollService; }
    public AttendanceService getAttendanceService() { return attendanceService; }
    public ValidationService getValidationService() { return validationService; }
    public User getCurrentUser() { return currentUser; }
    public Employee getCurrentEmployee() { return currentEmployee; }
    public MainFrame getMainFrame() { return mainFrame; }
    
    // ========== DATA REFRESH ==========
    
    /**
     * Refresh all data from files
     */
    public void refreshAllData() {
        System.out.println("Refreshing all data from files");
        
        employeeDAO.refresh();
        attendanceDAO.refresh();
        leaveDAO.refresh();
        payrollDAO.refresh();
        userDAO.refresh();
        
        // Reload current employee data
        if (currentEmployee != null) {
            currentEmployee = employeeService.getEmployeeById(currentEmployee.getEmployeeId());
        }
        
        refreshCurrentPanel();
    }
    
    /**
     * Backup all data files (simplified - just refreshes for now)
     */
    public void backupData() {
        System.out.println("Backup requested - refreshing data instead");
        refreshAllData();
        showInfo("Data refreshed successfully");
    }
}