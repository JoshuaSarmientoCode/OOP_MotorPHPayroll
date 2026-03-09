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

    // Form fields - All 19 fields from CSV
    private JTextField empIdField;                    // Employee #
    private JTextField lastNameField;                  // Last Name
    private JTextField firstNameField;                 // First Name
    private JTextArea addressArea;                     // Address (using JTextArea for multiline)
    private JTextField birthDateField;                 // Birthday
    private JTextField phoneField;                      // Phone Number
    private JTextField sssField;                        // SSS #
    private JTextField philHealthField;                 // Philhealth #
    private JTextField tinField;                         // TIN #
    private JTextField pagIbigField;                     // Pag-ibig #
    private JComboBox<String> departmentCombo;           // Department (replaces Status)
    private JTextField positionField;                    // Position
    private JTextField supervisorField;                  // Immediate Supervisor
    private JTextField basicSalaryField;                 // Basic Salary
    private JTextField riceSubsidyField;                 // Rice Subsidy
    private JTextField phoneAllowanceField;              // Phone Allowance
    private JTextField clothingAllowanceField;           // Clothing Allowance
    private JTextField grossSemiMonthlyField;            // Gross Semi-monthly Rate
    private JTextField hourlyRateField;                  // Hourly Rate
    private JComboBox<String> employeeTypeCombo;         // Employee Type (Regular, Probationary, Contractual)

    // Department options - These determine the role
    private static final String[] DEPARTMENTS = {
            "ADMINISTRATION", "HUMAN RESOURCES", "INFORMATION TECHNOLOGY", "FINANCE", "OPERATIONS"
    };

    // Employee Type options - Only Regular, Probationary, Contractual
    private static final String[] EMPLOYEE_TYPES = {
            "REGULAR", "PROBATIONARY", "CONTRACTUAL"
    };

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
        setSize(850, 900);
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
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ========== SECTION 1: EMPLOYEE ID AND TYPE ==========
        row = addSectionHeader(panel, gbc, row, "EMPLOYEE INFORMATION");

        // Employee # (read-only, auto-generated)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        panel.add(createLabel("Employee #:"), gbc);

        gbc.gridx = 1;
        empIdField = createTextField(15, false);
        empIdField.setEditable(false);
        empIdField.setBackground(new Color(245, 245, 245));
        panel.add(empIdField, gbc);
        row++;

        // Employee Type (Regular, Probationary, Contractual only)
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Employee Type:*"), gbc);

        gbc.gridx = 1;
        employeeTypeCombo = new JComboBox<>(EMPLOYEE_TYPES);
        employeeTypeCombo.setFont(UITheme.NORMAL_FONT);
        employeeTypeCombo.setBackground(UITheme.CARD_BG);
        employeeTypeCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        employeeTypeCombo.setPreferredSize(new Dimension(250, 35));
        panel.add(employeeTypeCombo, gbc);
        row++;

        // ========== SECTION 2: PERSONAL DETAILS ==========
        row = addSectionHeader(panel, gbc, row, "PERSONAL DETAILS");

        // Last Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Last Name:*"), gbc);

        gbc.gridx = 1;
        lastNameField = createTextField(25, true);
        lastNameField.setEditable(true);
        lastNameField.addFocusListener(new ValidationFocusListener(f ->
                validateField("lastName", lastNameField.getText(),
                        validator.validateName(lastNameField.getText(), "Last name"))));
        panel.add(lastNameField, gbc);
        row++;

        // First Name
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("First Name:*"), gbc);

        gbc.gridx = 1;
        firstNameField = createTextField(25, true);
        firstNameField.setEditable(true);
        firstNameField.addFocusListener(new ValidationFocusListener(f ->
                validateField("firstName", firstNameField.getText(),
                        validator.validateName(firstNameField.getText(), "First name"))));
        panel.add(firstNameField, gbc);
        row++;

        // Address
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(createLabel("Address:*"), gbc);

        gbc.gridx = 1;
        addressArea = new JTextArea(3, 25);
        addressArea.setFont(UITheme.NORMAL_FONT);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        addressArea.setBorder(UITheme.INPUT_BORDER);
        addressArea.addFocusListener(new ValidationFocusListener(f ->
                validateField("address", addressArea.getText(),
                        validator.validateAddress(addressArea.getText()))));

        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(null);
        addressScroll.setPreferredSize(new Dimension(300, 60));
        panel.add(addressScroll, gbc);
        gbc.anchor = GridBagConstraints.WEST;
        row++;

        // Birthday
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Birthday (MM/DD/YYYY):*"), gbc);

        gbc.gridx = 1;
        birthDateField = createTextField(15, true);
        birthDateField.setEditable(true);
        birthDateField.addFocusListener(new ValidationFocusListener(f -> {
            try {
                LocalDate date = LocalDate.parse(birthDateField.getText().trim(),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                validateField("birthDate", birthDateField.getText(),
                        validator.validateBirthDate(date));
            } catch (Exception e) {
                setFieldError(birthDateField, "Invalid date format (use MM/DD/YYYY)");
            }
        }));
        panel.add(birthDateField, gbc);
        row++;

        // Phone Number
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Phone Number:*"), gbc);

        gbc.gridx = 1;
        phoneField = createTextField(20, true);
        phoneField.setEditable(true);
        phoneField.addFocusListener(new ValidationFocusListener(f ->
                validateField("phone", phoneField.getText(),
                        validator.validatePhone(phoneField.getText()))));
        panel.add(phoneField, gbc);
        row++;

        // ========== SECTION 3: EMPLOYMENT DETAILS ==========
        row = addSectionHeader(panel, gbc, row, "EMPLOYMENT DETAILS");

        // Department
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Department:*"), gbc);

        gbc.gridx = 1;
        departmentCombo = new JComboBox<>(DEPARTMENTS);
        departmentCombo.setFont(UITheme.NORMAL_FONT);
        departmentCombo.setBackground(UITheme.CARD_BG);
        departmentCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        departmentCombo.setPreferredSize(new Dimension(250, 35));
        panel.add(departmentCombo, gbc);
        row++;

        // Position
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Position:*"), gbc);

        gbc.gridx = 1;
        positionField = createTextField(25, true);
        positionField.setEditable(true);
        panel.add(positionField, gbc);
        row++;

        // Immediate Supervisor
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Immediate Supervisor:"), gbc);

        gbc.gridx = 1;
        supervisorField = createTextField(25, true);
        supervisorField.setEditable(true);
        panel.add(supervisorField, gbc);
        row++;

        // ========== SECTION 4: SALARY AND ALLOWANCES ==========
        row = addSectionHeader(panel, gbc, row, "SALARY AND ALLOWANCES");

        // Basic Salary
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Basic Salary:*"), gbc);

        gbc.gridx = 1;
        basicSalaryField = createTextField(15, true);
        basicSalaryField.setEditable(true);
        basicSalaryField.addFocusListener(new ValidationFocusListener(f -> {
            validateField("salary", basicSalaryField.getText(),
                    validator.validateNumeric(basicSalaryField.getText(), "Basic salary", 10000, 1000000));
            calculateDerivedFields();
        }));
        basicSalaryField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateDerivedFields();
            }
        });
        panel.add(basicSalaryField, gbc);
        row++;

        // Rice Subsidy
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Rice Subsidy:"), gbc);

        gbc.gridx = 1;
        riceSubsidyField = createTextField(15, true);
        riceSubsidyField.setEditable(true);
        riceSubsidyField.addFocusListener(new ValidationFocusListener(f -> {
            validateField("riceSubsidy", riceSubsidyField.getText(),
                    validator.validateNumeric(riceSubsidyField.getText(), "Rice subsidy", 0, 10000));
            calculateDerivedFields();
        }));
        riceSubsidyField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateDerivedFields();
            }
        });
        panel.add(riceSubsidyField, gbc);
        row++;

        // Phone Allowance
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Phone Allowance:"), gbc);

        gbc.gridx = 1;
        phoneAllowanceField = createTextField(15, true);
        phoneAllowanceField.setEditable(true);
        phoneAllowanceField.addFocusListener(new ValidationFocusListener(f -> {
            validateField("phoneAllowance", phoneAllowanceField.getText(),
                    validator.validateNumeric(phoneAllowanceField.getText(), "Phone allowance", 0, 10000));
            calculateDerivedFields();
        }));
        phoneAllowanceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateDerivedFields();
            }
        });
        panel.add(phoneAllowanceField, gbc);
        row++;

        // Clothing Allowance
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Clothing Allowance:"), gbc);

        gbc.gridx = 1;
        clothingAllowanceField = createTextField(15, true);
        clothingAllowanceField.setEditable(true);
        clothingAllowanceField.addFocusListener(new ValidationFocusListener(f -> {
            validateField("clothingAllowance", clothingAllowanceField.getText(),
                    validator.validateNumeric(clothingAllowanceField.getText(), "Clothing allowance", 0, 10000));
            calculateDerivedFields();
        }));
        clothingAllowanceField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                calculateDerivedFields();
            }
        });
        panel.add(clothingAllowanceField, gbc);
        row++;

        // Gross Semi-monthly Rate
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Gross Semi-monthly Rate:"), gbc);

        gbc.gridx = 1;
        grossSemiMonthlyField = createTextField(15, false);
        grossSemiMonthlyField.setEditable(false);
        grossSemiMonthlyField.setBackground(new Color(245, 245, 245));
        panel.add(grossSemiMonthlyField, gbc);
        row++;

        // Hourly Rate
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Hourly Rate:"), gbc);

        gbc.gridx = 1;
        hourlyRateField = createTextField(15, false);
        hourlyRateField.setEditable(false);
        hourlyRateField.setBackground(new Color(245, 245, 245));
        panel.add(hourlyRateField, gbc);
        row++;

        // ========== SECTION 5: GOVERNMENT IDS ==========
        row = addSectionHeader(panel, gbc, row, "GOVERNMENT IDS");

        // SSS #
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

        // Philhealth #
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Philhealth #:"), gbc);

        gbc.gridx = 1;
        philHealthField = createTextField(20, true);
        philHealthField.setEditable(true);
        philHealthField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setPhilHealthNumber(philHealthField.getText().trim());
            validateField("philHealth", philHealthField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(philHealthField, gbc);
        row++;

        // TIN #
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

        // Pag-ibig #
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Pag-ibig #:"), gbc);

        gbc.gridx = 1;
        pagIbigField = createTextField(20, true);
        pagIbigField.setEditable(true);
        pagIbigField.addFocusListener(new ValidationFocusListener(f -> {
            GovernmentIds gov = new GovernmentIds();
            gov.setPagIbigNumber(pagIbigField.getText().trim());
            validateField("pagIbig", pagIbigField.getText(),
                    validator.validateGovernmentIds(gov));
        }));
        panel.add(pagIbigField, gbc);

        return panel;
    }

    private int addSectionHeader(JPanel panel, GridBagConstraints gbc, int row, String title) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);

        JLabel header = new JLabel(title);
        header.setFont(UITheme.SUBHEADER_FONT);
        header.setForeground(UITheme.ACCENT_DARK);
        panel.add(header, gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 8, 6, 8);
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

    private void calculateDerivedFields() {
        try {
            double basicSalary = Double.parseDouble(basicSalaryField.getText().trim().replace(",", ""));
            double grossSemiMonthly = basicSalary / 2;
            double hourlyRate = basicSalary / 168;

            grossSemiMonthlyField.setText(String.format("%,.0f", grossSemiMonthly));
            hourlyRateField.setText(String.format("%.2f", hourlyRate));
        } catch (NumberFormatException e) {
            // Ignore if basic salary not valid
        }
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
            case "address": return null; // Address is JTextArea
            case "phone": return phoneField;
            case "email": return null; // Email is auto-generated
            case "salary": return basicSalaryField;
            case "riceSubsidy": return riceSubsidyField;
            case "phoneAllowance": return phoneAllowanceField;
            case "clothingAllowance": return clothingAllowanceField;
            case "sss": return sssField;
            case "tin": return tinField;
            case "philHealth": return philHealthField;
            case "pagIbig": return pagIbigField;
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
        addressArea.setText("");
        birthDateField.setText(LocalDate.now().minusYears(25).format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        phoneField.setText("");

        departmentCombo.setSelectedIndex(0); // Default to ADMINISTRATION
        positionField.setText("");
        supervisorField.setText("");

        basicSalaryField.setText("25000");
        riceSubsidyField.setText("1500");
        phoneAllowanceField.setText("500");
        clothingAllowanceField.setText("500");

        calculateDerivedFields();

        employeeTypeCombo.setSelectedIndex(0); // Default to REGULAR

        sssField.setText("");
        philHealthField.setText("");
        tinField.setText("");
        pagIbigField.setText("");
    }

    private void loadData() {
        if (employee == null) return;

        empIdField.setText(employee.getEmployeeId());
        lastNameField.setText(employee.getLastName());
        firstNameField.setText(employee.getFirstName());
        addressArea.setText(employee.getAddress());

        // Birth date
        if (employee.getBirthDate() != null) {
            birthDateField.setText(employee.getBirthDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
        }

        phoneField.setText(employee.getPhoneNumber());
        positionField.setText(employee.getPosition());
        supervisorField.setText(employee.getImmediateSupervisor());

        // Set department based on employee class
        if (employee instanceof AdminEmployee) {
            departmentCombo.setSelectedItem("ADMINISTRATION");
        } else if (employee instanceof HREmployee) {
            departmentCombo.setSelectedItem("HUMAN RESOURCES");
        } else if (employee instanceof ITEmployee) {
            departmentCombo.setSelectedItem("INFORMATION TECHNOLOGY");
        } else if (employee instanceof FinanceEmployee) {
            departmentCombo.setSelectedItem("FINANCE");
        } else {
            departmentCombo.setSelectedItem("OPERATIONS");
        }

        // Salary and allowances
        basicSalaryField.setText(String.valueOf((int) employee.getBasicSalary()));
        riceSubsidyField.setText(String.valueOf((int) employee.getRiceSubsidy()));
        phoneAllowanceField.setText(String.valueOf((int) employee.getPhoneAllowance()));
        clothingAllowanceField.setText(String.valueOf((int) employee.getClothingAllowance()));

        calculateDerivedFields();

        // Employee type (Regular, Probationary, Contractual)
        if (employee.getStatus() == EmploymentStatus.PROBATIONARY) {
            employeeTypeCombo.setSelectedItem("PROBATIONARY");
        } else if (employee.getStatus() == EmploymentStatus.CONTRACTUAL) {
            employeeTypeCombo.setSelectedItem("CONTRACTUAL");
        } else {
            employeeTypeCombo.setSelectedItem("REGULAR");
        }

        // Government IDs
        if (employee.getGovernmentIds() != null) {
            GovernmentIds gov = employee.getGovernmentIds();
            sssField.setText(gov.getSssNumber() != null ? gov.getSssNumber() : "");
            philHealthField.setText(gov.getPhilHealthNumber() != null ? gov.getPhilHealthNumber() : "");
            tinField.setText(gov.getTinNumber() != null ? gov.getTinNumber() : "");
            pagIbigField.setText(gov.getPagIbigNumber() != null ? gov.getPagIbigNumber() : "");
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
        String selectedDepartment = (String) departmentCombo.getSelectedItem();
        String selectedType = (String) employeeTypeCombo.getSelectedItem();

        Employee emp;
        if (employee != null) {
            emp = employee;
        } else {
            emp = createEmployeeByDepartment(selectedDepartment);
        }

        // Set basic info
        emp.setEmployeeId(empIdField.getText().trim());
        emp.setLastName(lastNameField.getText().trim().toUpperCase());
        emp.setFirstName(firstNameField.getText().trim());
        emp.setAddress(addressArea.getText().trim());
        emp.setPhoneNumber(phoneField.getText().trim());

        // Generate email
        emp.setEmail(generateEmail(emp.getFirstName(), emp.getLastName()));

        // Position and supervisor
        emp.setPosition(positionField.getText().trim());
        emp.setImmediateSupervisor(supervisorField.getText().trim());

        // Set status based on employee type dropdown
        if ("PROBATIONARY".equals(selectedType)) {
            emp.setStatus(EmploymentStatus.PROBATIONARY);
        } else if ("CONTRACTUAL".equals(selectedType)) {
            emp.setStatus(EmploymentStatus.CONTRACTUAL);
        } else {
            emp.setStatus(EmploymentStatus.REGULAR);
        }

        // Parse birth date
        try {
            LocalDate birthDate = LocalDate.parse(birthDateField.getText().trim(),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            emp.setBirthDate(birthDate);
            emp.setHireDate(birthDate.plusYears(20));
        } catch (Exception e) {
            emp.setBirthDate(LocalDate.of(1990, 1, 1));
            emp.setHireDate(LocalDate.now().minusYears(1));
        }

        // Salary and allowances
        try {
            double basicSalary = Double.parseDouble(basicSalaryField.getText().trim().replace(",", ""));
            emp.setBasicSalary(basicSalary);
        } catch (NumberFormatException e) {
            // Keep existing value
        }

        try {
            double riceSubsidy = Double.parseDouble(riceSubsidyField.getText().trim().replace(",", ""));
            emp.setRiceSubsidy(riceSubsidy);
        } catch (NumberFormatException e) {
            // Keep existing value
        }

        try {
            double phoneAllowance = Double.parseDouble(phoneAllowanceField.getText().trim().replace(",", ""));
            emp.setPhoneAllowance(phoneAllowance);
        } catch (NumberFormatException e) {
            // Keep existing value
        }

        try {
            double clothingAllowance = Double.parseDouble(clothingAllowanceField.getText().trim().replace(",", ""));
            emp.setClothingAllowance(clothingAllowance);
        } catch (NumberFormatException e) {
            // Keep existing value
        }

        // Government IDs
        GovernmentIds govIds = new GovernmentIds();
        govIds.setSssNumber(sssField.getText().trim());
        govIds.setPhilHealthNumber(philHealthField.getText().trim());
        govIds.setTinNumber(tinField.getText().trim());
        govIds.setPagIbigNumber(pagIbigField.getText().trim());
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

    private Employee createEmployeeByDepartment(String department) {
        System.out.println("Creating employee with department: " + department);

        // Create based on department
        if (department != null) {
            switch (department) {
                case "ADMINISTRATION":
                    System.out.println("-> Creating AdminEmployee from department");
                    return new AdminEmployee();
                case "HUMAN RESOURCES":
                    System.out.println("-> Creating HREmployee from department");
                    return new HREmployee();
                case "INFORMATION TECHNOLOGY":
                    System.out.println("-> Creating ITEmployee from department");
                    return new ITEmployee();
                case "FINANCE":
                    System.out.println("-> Creating FinanceEmployee from department");
                    return new FinanceEmployee();
                case "OPERATIONS":
                    System.out.println("-> Creating RegularEmployee from department");
                    return new RegularEmployee();
            }
        }

        System.out.println("-> Creating RegularEmployee as default");
        return new RegularEmployee();
    }

    private String generateEmail(String firstName, String lastName) {
        if (firstName == null || lastName == null) return null;
        String cleanFirst = firstName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        String cleanLast = lastName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        return cleanFirst + "." + cleanLast + "@motorph.com";
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