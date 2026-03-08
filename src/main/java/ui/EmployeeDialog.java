package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class EmployeeDialog extends JDialog {

    private boolean saved = false;
    private Employee employee;
    private final EmployeeService employeeService;
    private final ValidationService validator;
    private final MainController controller;

    // Form fields
    private JTextField empIdField;
    private JTextField lastNameField;
    private JTextField firstNameField;
    private JTextField addressField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField positionField;
    private JTextField salaryField;
    private JTextField sssField;
    private JTextField tinField;
    private JTextField philField;
    private JTextField pagibigField;
    private JTextField supervisorField;
    private JTextField birthDateField;
    private JComboBox<String> statusCombo;
    private JComboBox<String> employeeTypeCombo;

    // Validation tracking
    private Map<JTextField, Boolean> fieldValid = new HashMap<>();
    private JButton saveButton;

    public EmployeeDialog(Frame parent, String title, Employee employee,
                          EmployeeService service, ValidationService validator,
                          MainController controller) {
        super(parent, title, true);
        this.employee = employee;
        this.employeeService = service;
        this.validator = validator;
        this.controller = controller;

        initializeDialog();
        initializeComponents();

        if (employee != null) {
            loadData();
        } else {
            generateId();
            setDefaultValues();
        }

        // Request focus on first field after dialog is shown
        SwingUtilities.invokeLater(() -> lastNameField.requestFocusInWindow());
    }

    private void initializeDialog() {
        setLayout(new BorderLayout());
        setSize(700, 800);
        setLocationRelativeTo(getParent());
        setResizable(true);
        setModal(true);
    }

    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(UITheme.BG_PRIMARY);

        // Form panel with scroll
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ========== SECTION 1: EMPLOYEE INFORMATION ==========
        row = addSectionHeader(panel, gbc, row, "EMPLOYEE INFORMATION");

        // Employee ID (read-only)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(createLabel("EMPLOYEE ID:"), gbc);

        gbc.gridx = 1;
        empIdField = createTextField(15, false); // Set to editable false for ID (auto-generated)
        empIdField.setEditable(false);
        empIdField.setBackground(new Color(245, 245, 245));
        panel.add(empIdField, gbc);
        row++;

        // Employee Type
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("EMPLOYEE TYPE:"), gbc);

        gbc.gridx = 1;
        employeeTypeCombo = new JComboBox<>(new String[]{
                "REGULAR EMPLOYEE", "PROBATIONARY EMPLOYEE", "HR EMPLOYEE",
                "FINANCE EMPLOYEE", "IT EMPLOYEE", "ADMIN EMPLOYEE"
        });
        employeeTypeCombo.setFont(UITheme.NORMAL_FONT);
        employeeTypeCombo.setBackground(UITheme.CARD_BG);
        employeeTypeCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        employeeTypeCombo.setPreferredSize(new Dimension(200, 35));
        panel.add(employeeTypeCombo, gbc);
        row++;

        // ========== SECTION 2: PERSONAL DETAILS ==========
        row = addSectionHeader(panel, gbc, row, "PERSONAL DETAILS");

        // Last Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("LAST NAME:"), gbc);

        gbc.gridx = 1;
        lastNameField = createTextField(20, true);
        lastNameField.setEditable(true);
        lastNameField.addFocusListener(new ValidationFocusListener(f ->
                validateField("lastName", lastNameField.getText(),
                        validator.validateName(lastNameField.getText(), "Last name"))));
        panel.add(lastNameField, gbc);
        row++;

        // First Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("FIRST NAME:"), gbc);

        gbc.gridx = 1;
        firstNameField = createTextField(20, true);
        firstNameField.setEditable(true);
        firstNameField.addFocusListener(new ValidationFocusListener(f ->
                validateField("firstName", firstNameField.getText(),
                        validator.validateName(firstNameField.getText(), "First name"))));
        panel.add(firstNameField, gbc);
        row++;

        // Birth Date
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("BIRTH DATE (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        birthDateField = createTextField(15, true);
        birthDateField.setEditable(true);
        birthDateField.setText(LocalDate.now().minusYears(25).toString());
        birthDateField.addFocusListener(new ValidationFocusListener(f -> {
            try {
                LocalDate date = LocalDate.parse(birthDateField.getText().trim());
                validateField("birthDate", birthDateField.getText(),
                        validator.validateBirthDate(date));
            } catch (Exception e) {
                setFieldError(birthDateField, "Invalid date format (use YYYY-MM-DD)");
            }
        }));
        panel.add(birthDateField, gbc);
        row++;

        // Address
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("ADDRESS:"), gbc);

        gbc.gridx = 1;
        addressField = createTextField(25, true);
        addressField.setEditable(true);
        addressField.addFocusListener(new ValidationFocusListener(f ->
                validateField("address", addressField.getText(),
                        validator.validateAddress(addressField.getText()))));
        panel.add(addressField, gbc);
        row++;

        // Phone
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("PHONE NUMBER:"), gbc);

        gbc.gridx = 1;
        phoneField = createTextField(15, true);
        phoneField.setEditable(true);
        phoneField.addFocusListener(new ValidationFocusListener(f ->
                validateField("phone", phoneField.getText(),
                        validator.validatePhone(phoneField.getText()))));
        panel.add(phoneField, gbc);
        row++;

        // Email
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("EMAIL:"), gbc);

        gbc.gridx = 1;
        emailField = createTextField(25, true);
        emailField.setEditable(true);
        emailField.addFocusListener(new ValidationFocusListener(f ->
                validateField("email", emailField.getText(),
                        validator.validateEmail(emailField.getText()))));
        panel.add(emailField, gbc);
        row++;

        // ========== SECTION 3: EMPLOYMENT DETAILS ==========
        row = addSectionHeader(panel, gbc, row, "EMPLOYMENT DETAILS");

        // Position
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("POSITION:"), gbc);

        gbc.gridx = 1;
        positionField = createTextField(20, true);
        positionField.setEditable(true);
        panel.add(positionField, gbc);
        row++;

        // Status
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("STATUS:"), gbc);

        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"REGULAR", "PROBATIONARY", "CONTRACTUAL"});
        statusCombo.setFont(UITheme.NORMAL_FONT);
        statusCombo.setBackground(UITheme.CARD_BG);
        statusCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        statusCombo.setPreferredSize(new Dimension(200, 35));
        panel.add(statusCombo, gbc);
        row++;

        // Supervisor
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("SUPERVISOR:"), gbc);

        gbc.gridx = 1;
        supervisorField = createTextField(20, true);
        supervisorField.setEditable(true);
        panel.add(supervisorField, gbc);
        row++;

        // Basic Salary
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("BASIC SALARY:"), gbc);

        gbc.gridx = 1;
        salaryField = createTextField(15, true);
        salaryField.setEditable(true);
        salaryField.setText("25000");
        salaryField.addFocusListener(new ValidationFocusListener(f ->
                validateField("salary", salaryField.getText(),
                        validator.validateNumeric(salaryField.getText(), "Basic salary", 10000, 1000000))));
        panel.add(salaryField, gbc);
        row++;

        // ========== SECTION 4: GOVERNMENT IDS ==========
        row = addSectionHeader(panel, gbc, row, "GOVERNMENT IDS");

        // SSS
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("SSS #:"), gbc);

        gbc.gridx = 1;
        sssField = createTextField(20, true);
        sssField.setEditable(true);
        sssField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setSssNumber(sssField.getText().trim());
            validateField("sss", sssField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(sssField, gbc);
        row++;

        // TIN
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("TIN #:"), gbc);

        gbc.gridx = 1;
        tinField = createTextField(20, true);
        tinField.setEditable(true);
        tinField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setTinNumber(tinField.getText().trim());
            validateField("tin", tinField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(tinField, gbc);
        row++;

        // PhilHealth
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("PHILHEALTH #:"), gbc);

        gbc.gridx = 1;
        philField = createTextField(20, true);
        philField.setEditable(true);
        philField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setPhilHealthNumber(philField.getText().trim());
            validateField("philHealth", philField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(philField, gbc);
        row++;

        // Pag-IBIG
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("PAG-IBIG #:"), gbc);

        gbc.gridx = 1;
        pagibigField = createTextField(20, true);
        pagibigField.setEditable(true);
        pagibigField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setPagIbigNumber(pagibigField.getText().trim());
            validateField("pagIbig", pagibigField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(pagibigField, gbc);

        return panel;
    }

    private int addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String title) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 8, 8, 8);

        JLabel header = new JLabel(title);
        header.setFont(UITheme.SUBHEADER_FONT);
        header.setForeground(UITheme.ACCENT_DARK);
        panel.add(header, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(8, 8, 8, 8);
        return row + 1;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BOLD_SMALL_FONT);
        label.setForeground(UITheme.TEXT_SECONDARY);
        return label;
    }

    private JTextField createTextField(int cols, boolean editable) {
        JTextField field = new JTextField(cols);
        field.setFont(UITheme.NORMAL_FONT);
        field.setBorder(UITheme.INPUT_BORDER);
        field.setEditable(editable);
        field.setBackground(editable ? UITheme.CARD_BG : new Color(250, 250, 250));
        field.setEnabled(true);
        field.setFocusable(true);
        return field;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panel.setBackground(UITheme.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        saveButton = UITheme.createPrimaryButton("SAVE EMPLOYEE", UITheme.ACCENT_GREEN);
        saveButton.setPreferredSize(new Dimension(150, 40));
        saveButton.addActionListener(e -> saveEmployee());

        JButton cancelButton = UITheme.createDashboardButton("CANCEL");
        cancelButton.setPreferredSize(new Dimension(150, 40));
        cancelButton.addActionListener(e -> dispose());

        panel.add(saveButton);
        panel.add(cancelButton);

        return panel;
    }

    private void validateField(String fieldName, String value, ValidationService.ValidationResult result) {
        JTextField field = getFieldByName(fieldName);
        if (field == null) return;

        if (result.isValid()) {
            clearFieldError(field);
            fieldValid.put(field, true);
        } else {
            String error = result.getFieldErrors().get(fieldName);
            if (error != null) {
                setFieldError(field, error);
            }
            fieldValid.put(field, false);
        }

        updateSaveButtonState();
    }

    private JTextField getFieldByName(String fieldName) {
        switch (fieldName) {
            case "lastName": return lastNameField;
            case "firstName": return firstNameField;
            case "address": return addressField;
            case "phone": return phoneField;
            case "email": return emailField;
            case "salary": return salaryField;
            case "sss": return sssField;
            case "tin": return tinField;
            case "philHealth": return philField;
            case "pagIbig": return pagibigField;
            case "birthDate": return birthDateField;
            default: return null;
        }
    }

    private void setFieldError(JTextField field, String message) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.ACCENT_RED, 2),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)
        ));
        field.setToolTipText(message);
    }

    private void clearFieldError(JTextField field) {
        field.setBorder(UITheme.INPUT_BORDER);
        field.setToolTipText(null);
    }

    private void updateSaveButtonState() {
        boolean allValid = fieldValid.values().stream().allMatch(v -> v);
        saveButton.setEnabled(allValid);
    }

    private void generateId() {
        empIdField.setText(employeeService.getNextEmployeeId());
    }

    private void setDefaultValues() {
        lastNameField.setText("");
        firstNameField.setText("");
        addressField.setText("");
        phoneField.setText("");
        emailField.setText("");
        positionField.setText("");
        supervisorField.setText("");
        salaryField.setText("25000");
        birthDateField.setText(LocalDate.now().minusYears(25).toString());
        statusCombo.setSelectedIndex(0);
        employeeTypeCombo.setSelectedIndex(0);
        sssField.setText("");
        tinField.setText("");
        philField.setText("");
        pagibigField.setText("");
    }

    private void loadData() {
        if (employee == null) return;

        empIdField.setText(employee.getEmployeeId());
        lastNameField.setText(employee.getLastName());
        firstNameField.setText(employee.getFirstName());
        addressField.setText(employee.getAddress());
        phoneField.setText(employee.getPhoneNumber());
        emailField.setText(employee.getEmail());
        positionField.setText(employee.getPosition());
        supervisorField.setText(employee.getImmediateSupervisor());
        salaryField.setText(String.valueOf((int) employee.getBasicSalary()));

        // Set birth date
        if (employee.getBirthDate() != null) {
            birthDateField.setText(employee.getBirthDate().toString());
        }

        // Set status
        if (employee.getStatus() != null) {
            String status = employee.getStatus().toString();
            if ("REGULAR".equals(status)) {
                statusCombo.setSelectedIndex(0);
            } else if ("PROBATIONARY".equals(status)) {
                statusCombo.setSelectedIndex(1);
            } else {
                statusCombo.setSelectedIndex(2);
            }
        }

        // Set employee type based on instance
        if (employee instanceof ProbationaryEmployee) {
            employeeTypeCombo.setSelectedIndex(1);
        } else if (employee instanceof HREmployee) {
            employeeTypeCombo.setSelectedIndex(2);
        } else if (employee instanceof FinanceEmployee) {
            employeeTypeCombo.setSelectedIndex(3);
        } else if (employee instanceof ITEmployee) {
            employeeTypeCombo.setSelectedIndex(4);
        } else if (employee instanceof AdminEmployee) {
            employeeTypeCombo.setSelectedIndex(5);
        } else {
            employeeTypeCombo.setSelectedIndex(0);
        }

        // Set government IDs
        if (employee.getGovernmentIds() != null) {
            GovernmentIds gov = employee.getGovernmentIds();
            sssField.setText(gov.getSssNumber() != null ? gov.getSssNumber() : "");
            tinField.setText(gov.getTinNumber() != null ? gov.getTinNumber() : "");
            philField.setText(gov.getPhilHealthNumber() != null ? gov.getPhilHealthNumber() : "");
            pagibigField.setText(gov.getPagIbigNumber() != null ? gov.getPagIbigNumber() : "");
        }
    }

    private void saveEmployee() {
        try {
            Employee emp = buildEmployee();

            boolean success;
            if (employee == null) {
                success = employeeService.addEmployee(emp);
            } else {
                success = employeeService.updateEmployee(emp);
            }

            if (success) {
                saved = true;
                if (controller != null) {
                    controller.showInfo("EMPLOYEE SAVED SUCCESSFULLY");
                } else {
                    JOptionPane.showMessageDialog(this, "EMPLOYEE SAVED SUCCESSFULLY", "SUCCESS", JOptionPane.INFORMATION_MESSAGE);
                }
                dispose();
            } else {
                if (controller != null) {
                    controller.showError("FAILED TO SAVE EMPLOYEE");
                } else {
                    JOptionPane.showMessageDialog(this, "FAILED TO SAVE EMPLOYEE", "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            if (controller != null) {
                controller.showError("ERROR: " + e.getMessage());
            } else {
                JOptionPane.showMessageDialog(this, "ERROR: " + e.getMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private Employee buildEmployee() {
        String selectedType = (String) employeeTypeCombo.getSelectedItem();
        String selectedStatus = (String) statusCombo.getSelectedItem();

        Employee emp;
        if (employee != null) {
            emp = employee;
        } else {
            emp = createEmployeeByType(selectedType, selectedStatus);
        }

        // Set basic info
        emp.setEmployeeId(empIdField.getText().trim());
        emp.setLastName(lastNameField.getText().trim().toUpperCase());
        emp.setFirstName(firstNameField.getText().trim());
        emp.setAddress(addressField.getText().trim());
        emp.setPhoneNumber(phoneField.getText().trim());
        emp.setEmail(emailField.getText().trim());
        emp.setPosition(positionField.getText().trim());
        emp.setImmediateSupervisor(supervisorField.getText().trim());
        emp.setStatus(EmploymentStatus.fromString(selectedStatus));

        // Set salary
        double salary = Double.parseDouble(salaryField.getText().trim().replace(",", ""));
        emp.setBasicSalary(salary);
        emp.setRiceSubsidy(1500);
        emp.setPhoneAllowance(500);
        emp.setClothingAllowance(500);

        // Set birth date
        try {
            LocalDate birthDate = LocalDate.parse(birthDateField.getText().trim());
            emp.setBirthDate(birthDate);
            emp.setHireDate(birthDate.plusYears(20));
        } catch (Exception e) {
            emp.setBirthDate(LocalDate.of(1990, 1, 1));
            emp.setHireDate(LocalDate.now().minusYears(1));
        }

        // Set government IDs
        GovernmentIds govIds = new GovernmentIds();
        govIds.setSssNumber(sssField.getText().trim());
        govIds.setTinNumber(tinField.getText().trim());
        govIds.setPhilHealthNumber(philField.getText().trim());
        govIds.setPagIbigNumber(pagibigField.getText().trim());
        emp.setGovernmentIds(govIds);

        // Set probation details if applicable
        if (emp instanceof ProbationaryEmployee) {
            ProbationaryEmployee probEmp = (ProbationaryEmployee) emp;
            if (probEmp.getProbationDetails() == null) {
                probEmp.setProbationDetails(new ProbationDetails(emp.getHireDate()));
            }
            probEmp.getProbationDetails().setSupervisor(emp.getImmediateSupervisor());
        }

        return emp;
    }

    private Employee createEmployeeByType(String type, String status) {
        String typeUpper = type.toUpperCase();
        String statusUpper = status.toUpperCase();

        if ("PROBATIONARY".equals(statusUpper)) {
            return new ProbationaryEmployee();
        }

        if (typeUpper.contains("HR")) {
            return new HREmployee();
        } else if (typeUpper.contains("FINANCE")) {
            return new FinanceEmployee();
        } else if (typeUpper.contains("IT")) {
            return new ITEmployee();
        } else if (typeUpper.contains("ADMIN")) {
            return new AdminEmployee();
        } else {
            return new RegularEmployee();
        }
    }

    public boolean isSaved() {
        return saved;
    }

    // Inner class for validation focus listener
    private class ValidationFocusListener extends FocusAdapter {
        private final java.util.function.Consumer<JTextField> validator;

        ValidationFocusListener(java.util.function.Consumer<JTextField> validator) {
            this.validator = validator;
        }

        @Override
        public void focusLost(FocusEvent e) {
            validator.accept((JTextField) e.getSource());
        }
    }
}