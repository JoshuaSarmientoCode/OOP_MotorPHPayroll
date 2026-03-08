package ui;

import model.*;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Map;

public class EmployeeDetailsDialog extends JDialog {
    
    private final Employee employee;
    
    public EmployeeDetailsDialog(Frame parent, Employee employee) {
        super(parent, "EMPLOYEE DETAILS", true);
        this.employee = employee;
        
        initializeDialog();
        initializeComponents();
    }
    
    private void initializeDialog() {
        setSize(750, 600);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        setResizable(false);
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_PRIMARY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Tabbed content
        JTabbedPane tabs = createTabbedPane();
        mainPanel.add(tabs, BorderLayout.CENTER);
        
        // Close button
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel nameLabel = new JLabel(employee.getFullName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        nameLabel.setForeground(UITheme.TEXT_PRIMARY);
        panel.add(nameLabel, BorderLayout.WEST);
        
        // Show full employee ID without masking
        JLabel idLabel = new JLabel("ID: " + employee.getEmployeeId());
        idLabel.setFont(UITheme.NORMAL_FONT);
        idLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(idLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JTabbedPane createTabbedPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.SUBHEADER_FONT);
        tabs.setBackground(UITheme.CARD_BG);
        
        tabs.addTab("PERSONAL", createPersonalPanel());
        tabs.addTab("EMPLOYMENT", createEmploymentPanel());
        tabs.addTab("GOVERNMENT IDs", createGovernmentPanel());
        tabs.addTab("SALARY", createSalaryPanel());
        tabs.addTab("STATISTICS", createStatisticsPanel());
        
        return tabs;
    }
    
    private JPanel createPersonalPanel() {
        String[][] data = {
            {"First Name", employee.getFirstName()},
            {"Last Name", employee.getLastName()},
            {"Birthdate", formatDate(employee.getBirthDate())},
            {"Age", calculateAge() + " years"},
            {"Address", employee.getAddress() != null ? employee.getAddress() : "—"},
            {"Phone", formatPhone(employee.getPhoneNumber())},
            {"Email", employee.getEmail() != null ? employee.getEmail() : "—"}
        };
        
        return createDataPanel("PERSONAL INFORMATION", data);
    }
    
    private JPanel createEmploymentPanel() {
        String[][] data = {
            {"Employee ID", employee.getEmployeeId()}, // Shows full ID
            {"Position", employee.getPosition() != null ? employee.getPosition() : "—"},
            {"Status", employee.getStatus() != null ? employee.getStatus().getDisplayName() : "—"},
            {"Department", employee.getDepartment()},
            {"Supervisor", employee.getImmediateSupervisor() != null ? employee.getImmediateSupervisor() : "—"},
            {"Hire Date", formatDate(employee.getHireDate())},
            {"Tenure", calculateTenure()}
        };
        
        return createDataPanel("EMPLOYMENT DETAILS", data);
    }
    
    private JPanel createGovernmentPanel() {
        GovernmentIds gov = employee.getGovernmentIds();
        
        String[][] data = {
            {"SSS Number", gov != null ? gov.getSssNumber() : "—"},
            {"TIN Number", gov != null ? gov.getTinNumber() : "—"},
            {"PhilHealth Number", gov != null ? gov.getPhilHealthNumber() : "—"},
            {"Pag-IBIG Number", gov != null ? gov.getPagIbigNumber() : "—"},
            {"Complete Set", gov != null && gov.isComplete() ? "YES" : "NO"},
            {"Missing Count", gov != null ? String.valueOf(gov.getMissingCount()) : "4"}
        };
        
        return createDataPanel("GOVERNMENT IDs", data);
    }
    
    private JPanel createSalaryPanel() {
        String[][] data = {
            {"Basic Salary", formatCurrency(employee.getBasicSalary())},
            {"Rice Subsidy", formatCurrency(employee.getRiceSubsidy())},
            {"Phone Allowance", formatCurrency(employee.getPhoneAllowance())},
            {"Clothing Allowance", formatCurrency(employee.getClothingAllowance())},
            {"Total Allowances", formatCurrency(employee.getTotalAllowances())},
            {"Gross Salary", formatCurrency(employee.getGrossSalary())},
            {"Hourly Rate", formatCurrency(employee.getHourlyRate())},
            {"Daily Rate", formatCurrency(employee.getDailyRate())}
        };
        
        return createDataPanel("SALARY INFORMATION", data);
    }
    
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        
        // Attendance stats
        int attendanceCount = employee.getAttendanceRecords() != null ? 
            employee.getAttendanceRecords().size() : 0;
        long presentCount = employee.getAttendanceRecords() != null ?
            employee.getAttendanceRecords().stream().filter(a -> a.getTimeOut() != null).count() : 0;
        
        // Leave stats
        int leaveCount = employee.getLeaveRequests() != null ? 
            employee.getLeaveRequests().size() : 0;
        long approvedLeaves = employee.getLeaveRequests() != null ?
            employee.getLeaveRequests().stream().filter(LeaveRequest::isApproved).count() : 0;
        
        String[][] stats = {
            {"Total Attendance Records", String.valueOf(attendanceCount)},
            {"Days Present", String.valueOf(presentCount)},
            {"Attendance Rate", attendanceCount > 0 ? 
                String.format("%.1f%%", (presentCount * 100.0 / attendanceCount)) : "0%"},
            {"Total Leave Requests", String.valueOf(leaveCount)},
            {"Approved Leaves", String.valueOf(approvedLeaves)},
            {"Leave Approval Rate", leaveCount > 0 ? 
                String.format("%.1f%%", (approvedLeaves * 100.0 / leaveCount)) : "0%"}
        };
        
        for (int i = 0; i < stats.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.4;
            
            JLabel keyLabel = new JLabel(stats[i][0] + ":");
            keyLabel.setFont(UITheme.BOLD_SMALL_FONT);
            keyLabel.setForeground(UITheme.TEXT_SECONDARY);
            panel.add(keyLabel, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.6;
            
            JLabel valueLabel = new JLabel(stats[i][1]);
            valueLabel.setFont(UITheme.NORMAL_FONT);
            valueLabel.setForeground(UITheme.TEXT_PRIMARY);
            panel.add(valueLabel, gbc);
        }
        
        // Probation details if applicable
        if (employee instanceof ProbationaryEmployee) {
            gbc.gridx = 0;
            gbc.gridy = stats.length;
            gbc.gridwidth = 2;
            
            JSeparator separator = new JSeparator();
            separator.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            panel.add(separator, gbc);
            
            gbc.gridy = stats.length + 1;
            JLabel probLabel = new JLabel("PROBATIONARY STATUS", SwingConstants.CENTER);
            probLabel.setFont(UITheme.SUBHEADER_FONT);
            probLabel.setForeground(UITheme.ACCENT_DARK);
            panel.add(probLabel, gbc);
            
            ProbationaryEmployee prob = (ProbationaryEmployee) employee;
            ProbationDetails details = prob.getProbationDetails();
            
            if (details != null) {
                String[][] probData = {
                    {"Start Date", details.getFormattedStartDate()},
                    {"End Date", details.getFormattedEndDate()},
                    {"Remaining Days", String.valueOf(details.getRemainingDays())},
                    {"Progress", details.getProgress()},
                    {"Performance", details.getPerformanceStatus()},
                    {"Eligible for Regularization", details.canBeRegularized() ? "YES" : "NO"}
                };
                
                for (int i = 0; i < probData.length; i++) {
                    gbc.gridwidth = 1;
                    gbc.gridy = stats.length + 2 + i;
                    
                    gbc.gridx = 0;
                    JLabel k = new JLabel(probData[i][0] + ":");
                    k.setFont(UITheme.BOLD_SMALL_FONT);
                    k.setForeground(UITheme.TEXT_SECONDARY);
                    panel.add(k, gbc);
                    
                    gbc.gridx = 1;
                    JLabel v = new JLabel(probData[i][1]);
                    v.setFont(UITheme.NORMAL_FONT);
                    v.setForeground(UITheme.TEXT_PRIMARY);
                    panel.add(v, gbc);
                }
            }
        }
        
        return panel;
    }
    
    private JPanel createDataPanel(String title, String[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        for (int i = 0; i < data.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 0.3;
            
            JLabel keyLabel = new JLabel(data[i][0]);
            keyLabel.setFont(UITheme.BOLD_SMALL_FONT);
            keyLabel.setForeground(UITheme.TEXT_SECONDARY);
            contentPanel.add(keyLabel, gbc);
            
            gbc.gridx = 1;
            gbc.weightx = 0.7;
            
            JLabel valueLabel = new JLabel(data[i][1]);
            valueLabel.setFont(UITheme.NORMAL_FONT);
            valueLabel.setForeground(UITheme.TEXT_PRIMARY);
            contentPanel.add(valueLabel, gbc);
        }
        
        panel.add(contentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JButton closeButton = UITheme.createPrimaryButton("CLOSE", UITheme.ACCENT_DARK);
        closeButton.setPreferredSize(new Dimension(120, 40));
        closeButton.addActionListener(e -> dispose());
        
        panel.add(closeButton);
        
        return panel;
    }
    
    private String formatDate(LocalDate date) {
        return date != null ? 
            date.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) : "—";
    }
    
    private String formatCurrency(double amount) {
        return String.format("₱ %,.2f", amount);
    }
    
    private String formatPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "—";
        // Just return the phone as is, no formatting
        return phone;
    }
    
    private String calculateAge() {
        if (employee.getBirthDate() == null) return "—";
        return String.valueOf(LocalDate.now().getYear() - employee.getBirthDate().getYear());
    }
    
    private String calculateTenure() {
        if (employee.getHireDate() == null) return "—";
        long years = java.time.temporal.ChronoUnit.YEARS.between(
            employee.getHireDate(), LocalDate.now());
        return years + " year" + (years != 1 ? "s" : "");
    }
}