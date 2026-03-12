package ui;

import model.*;
import model.Employee.Employee;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;

public class EmployeeDetailsDialog extends JDialog {

    private final Employee employee;

    public EmployeeDetailsDialog(Frame parent, Employee employee) {
        super(parent, "EMPLOYEE DETAILS", true);
        this.employee = employee;

        initializeDialog();
        initializeComponents();
    }

    private void initializeDialog() {
        setSize(750, 650);
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
                {"Employee ID", employee.getEmployeeId()},
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
        double basicSalary = employee.getBasicSalary();
        double riceSubsidy = employee.getRiceSubsidy();
        double phoneAllowance = employee.getPhoneAllowance();
        double clothingAllowance = employee.getClothingAllowance();
        double totalAllowances = riceSubsidy + phoneAllowance + clothingAllowance;
        double grossSalary = basicSalary + totalAllowances;
        double hourlyRate = employee.getHourlyRate();
        double dailyRate = employee.getDailyRate();
        double semiMonthlyRate = basicSalary / 2;

        String[][] data = {
                {"Basic Salary", formatCurrency(basicSalary)},
                {"Rice Subsidy", formatCurrency(riceSubsidy)},
                {"Phone Allowance", formatCurrency(phoneAllowance)},
                {"Clothing Allowance", formatCurrency(clothingAllowance)},
                {"Total Allowances", formatCurrency(totalAllowances)},
                {"Gross Salary", formatCurrency(grossSalary)},
                {"Semi-monthly Rate", formatCurrency(semiMonthlyRate)},
                {"Hourly Rate", formatCurrency(hourlyRate)},
                {"Daily Rate", formatCurrency(dailyRate)}
        };

        return createDataPanel("SALARY INFORMATION", data);
    }

    private JPanel createDataPanel(String title, String[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.SUBHEADER_FONT);
        titleLabel.setForeground(UITheme.ACCENT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

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

        // Format as XXX-XXX-XXXX if it's 10 digits
        String clean = phone.replaceAll("[^0-9]", "");
        if (clean.length() == 10) {
            return clean.substring(0, 3) + "-" + clean.substring(3, 6) + "-" + clean.substring(6);
        }
        if (clean.length() == 11) {
            return clean.substring(0, 4) + "-" + clean.substring(4, 7) + "-" + clean.substring(7);
        }
        return phone;
    }

    private int calculateAge() {
        if (employee.getBirthDate() == null) return 0;
        return LocalDate.now().getYear() - employee.getBirthDate().getYear();
    }

    private String calculateTenure() {
        if (employee.getHireDate() == null) return "—";
        long years = java.time.temporal.ChronoUnit.YEARS.between(
                employee.getHireDate(), LocalDate.now());
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
                employee.getHireDate(), LocalDate.now()) % 12;

        if (years == 0) {
            return months + " month" + (months != 1 ? "s" : "");
        } else if (months == 0) {
            return years + " year" + (years != 1 ? "s" : "");
        } else {
            return years + " year" + (years != 1 ? "s" : "") + ", " +
                    months + " month" + (months != 1 ? "s" : "");
        }
    }
}