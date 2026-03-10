package ui;

import main.MainController;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {

    private final MainController controller;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private JButton loginButton;

    public LoginPanel(MainController controller) {
        this.controller = controller;
        initializePanel();
    }

    private void initializePanel() {
        setLayout(new GridBagLayout());
        setBackground(UITheme.BG_PRIMARY);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(createLoginCard(), gbc);
    }

    private JPanel createLoginCard() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        int row = 0;

        // Brand
        JLabel brandLabel = new JLabel("MOTORPH", SwingConstants.CENTER);
        brandLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        brandLabel.setForeground(UITheme.ACCENT_DARK);
        gbc.gridy = row++;
        panel.add(brandLabel, gbc);

        JLabel subLabel = new JLabel("PAYROLL SYSTEM", SwingConstants.CENTER);
        subLabel.setFont(UITheme.BOLD_SMALL_FONT);
        subLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = row++;
        panel.add(subLabel, gbc);

        // Separator
        gbc.gridy = row++;
        panel.add(new JSeparator(), gbc);

        // Employee ID / Username label
        gbc.gridwidth = 1;
        gbc.gridy = row++;

        JLabel empLabel = new JLabel("EMPLOYEE ID / USERNAME");
        empLabel.setFont(UITheme.BOLD_SMALL_FONT);
        empLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        panel.add(empLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(UITheme.NORMAL_FONT);
        usernameField.setBorder(UITheme.INPUT_BORDER);
        gbc.gridx = 1;
        panel.add(usernameField, gbc);

        // Password
        gbc.gridy = row++;

        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(UITheme.BOLD_SMALL_FONT);
        passLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(UITheme.NORMAL_FONT);
        passwordField.setBorder(UITheme.INPUT_BORDER);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        // Message label
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(UITheme.SMALL_FONT);
        messageLabel.setForeground(UITheme.ACCENT_RED);
        panel.add(messageLabel, gbc);

        // Default password hint
        JLabel hintLabel = new JLabel(
                "<html><center>Default password: emp + last 2 digits of ID<br>(e.g., emp01, emp09, emp33)</center></html>",
                SwingConstants.CENTER
        );
        hintLabel.setFont(UITheme.SMALL_FONT);
        hintLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridy = row++;
        panel.add(hintLabel, gbc);

        // Login button
        loginButton = UITheme.createPrimaryButton("SIGN IN", UITheme.ACCENT_DARK);
        loginButton.setPreferredSize(new Dimension(200, 45));
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        loginButton.addActionListener(e -> attemptLogin());

        gbc.insets = new Insets(20, 8, 4, 8);
        gbc.gridy = row++;
        panel.add(loginButton, gbc);

        // ===== FORGOT PASSWORD LINK =====
        JLabel forgotLink = new JLabel("Forgot password?", SwingConstants.CENTER);
        forgotLink.setFont(UITheme.SMALL_FONT);
        forgotLink.setForeground(UITheme.ACCENT_DARK);
        forgotLink.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Underline on hover
        forgotLink.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotLink.setText("<html><u>Forgot password?</u></html>");
            }
            @Override
            public void mouseExited(MouseEvent e) {
                forgotLink.setText("Forgot password?");
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                showForgotPasswordDialog();
            }
        });

        gbc.insets = new Insets(0, 8, 8, 8);
        gbc.gridy = row++;
        panel.add(forgotLink, gbc);

        // Enter key listeners
        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> attemptLogin());

        return panel;
    }

    // ===== FORGOT PASSWORD DIALOG =====

    private void showForgotPasswordDialog() {
        // Step 1 — ask for employee ID
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;

        JLabel instruction = new JLabel(
                "<html>Enter your <b>Employee ID</b> to reset your password.</html>");
        instruction.setFont(UITheme.SMALL_FONT);
        gbc.gridy = 0;
        inputPanel.add(instruction, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel idLabel = new JLabel("Employee ID:");
        idLabel.setFont(UITheme.BOLD_SMALL_FONT);
        idLabel.setForeground(UITheme.TEXT_SECONDARY);
        gbc.gridx = 0;
        inputPanel.add(idLabel, gbc);

        JTextField idField = new JTextField(12);
        idField.setFont(UITheme.NORMAL_FONT);
        gbc.gridx = 1;
        inputPanel.add(idField, gbc);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
                this,
                inputPanel,
                "Forgot Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String employeeId = idField.getText().trim();

        // Step 2 — validate the ID format
        if (employeeId.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID is required.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!employeeId.matches("\\d{5}")) {
            JOptionPane.showMessageDialog(this,
                    "Employee ID must be 5 digits.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Step 3 — look up user and reset via UserService (uses Overload 1: changePassword(User, newPassword))
        model.User user = controller.getUserService().getUserByUsername(employeeId);

        if (user == null) {
            JOptionPane.showMessageDialog(this,
                    "No account found for Employee ID: " + employeeId + ".\n"
                            + "Please contact your HR or IT department.",
                    "Account Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Reset password to: "emp" + last 2 digits of employee ID (e.g. emp09) — satisfies 6-char minimum
        String resetPassword = "emp" + employeeId.substring(employeeId.length() - 2);

        // Calls Overload 1 — changePassword(User user, String newPassword)
        boolean success = controller.getUserService().changePassword(user, resetPassword);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "<html>Your password has been reset.<br><br>"
                            + "Your new password is: <b>" + resetPassword + "</b><br><br>"
                            + "Please log in and change it immediately.</html>",
                    "Password Reset Successful", JOptionPane.INFORMATION_MESSAGE);

            // Pre-fill the employee ID field for convenience
            usernameField.setText(employeeId);
            passwordField.setText("");
            passwordField.requestFocus();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Password reset failed. Please contact IT support.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== LOGIN =====

    private void attemptLogin() {
        String empId    = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        messageLabel.setText(" ");

        if (empId.isEmpty() || password.isEmpty()) {
            messageLabel.setText("ALL FIELDS REQUIRED");
            return;
        }

        // Accept 5-digit employee IDs or alphanumeric usernames (admin)
        if (!empId.matches("\\d{5}") && !empId.matches("[a-zA-Z0-9_]{3,20}")) {
            messageLabel.setText("INVALID EMPLOYEE ID OR USERNAME");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("LOGGING IN...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                controller.handleLogin(empId, password);
                return true;
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                loginButton.setText("SIGN IN");
            }
        };

        worker.execute();
    }

    public void resetForm() {
        usernameField.setText("");
        passwordField.setText("");
        messageLabel.setText(" ");
        usernameField.requestFocus();
    }
}