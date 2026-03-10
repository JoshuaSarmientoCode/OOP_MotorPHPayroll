package ui;

import main.MainController;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;

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

        gbc.gridy = row++;
        panel.add(new JSeparator(), gbc);

        // Employee ID / Username field
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

        // Message area
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.gridx = 0;

        messageLabel = new JLabel(" ", SwingConstants.CENTER);
        messageLabel.setFont(UITheme.SMALL_FONT);
        messageLabel.setForeground(UITheme.ACCENT_RED);
        panel.add(messageLabel, gbc);

        // Hint
        JLabel hintLabel = new JLabel(
                "<html><center>Employees: use your 5-digit ID (e.g. 10001)<br>" +
                        "Default password: emp + last digit of ID (e.g. emp1)<br>" +
                        "Admin: username <b>admin</b>, password <b>admin123</b></center></html>",
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
        gbc.insets = new Insets(20, 8, 8, 8);
        gbc.gridy = row++;
        panel.add(loginButton, gbc);

        usernameField.addActionListener(e -> passwordField.requestFocus());
        passwordField.addActionListener(e -> attemptLogin());

        return panel;
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        messageLabel.setText(" ");

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("ALL FIELDS REQUIRED");
            return;
        }

        // Accept: 5-digit employee ID (10001) OR alphanumeric username (admin)
        boolean isEmployeeId = username.matches("\\d{5}");
        boolean isAdminUsername = username.matches("[a-zA-Z0-9_]{3,20}");

        if (!isEmployeeId && !isAdminUsername) {
            messageLabel.setText("ENTER A VALID EMPLOYEE ID OR USERNAME");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("LOGGING IN...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                controller.handleLogin(username, password);
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