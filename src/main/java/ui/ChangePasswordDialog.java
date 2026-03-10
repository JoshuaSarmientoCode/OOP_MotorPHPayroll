package ui;

import main.MainController;
import model.User;
import service.UserService;
import service.ValidationService;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {

    private final MainController controller;
    private final UserService userService;
    private final ValidationService validationService;
    private final User currentUser;

    private JPasswordField currentPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;
    private JButton saveBtn;

    private boolean passwordChanged = false;

    public ChangePasswordDialog(Frame parent, MainController controller,
                                UserService userService,
                                ValidationService validationService,
                                User currentUser) {
        super(parent, "CHANGE PASSWORD", true);
        this.controller = controller;
        this.userService = userService;
        this.validationService = validationService;
        this.currentUser = currentUser;

        initializeDialog();
    }

    private void initializeDialog() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setResizable(false);

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(440, 400));
        setLocationRelativeTo(getOwner());
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.ACCENT_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel title = new JLabel("CHANGE PASSWORD");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Logged in as: " + currentUser.getFullName()
                + "  —  " + currentUser.getRoleName());
        subtitle.setFont(UITheme.SMALL_FONT);
        subtitle.setForeground(new Color(200, 200, 200));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(subtitle);

        panel.add(textPanel, BorderLayout.WEST);
        return panel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 35, 10, 35));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(9, 5, 9, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ---- Current Password ----
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.38;
        JLabel curLabel = new JLabel("Current Password:");
        curLabel.setFont(UITheme.BOLD_SMALL_FONT);
        curLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(curLabel, gbc);

        currentPasswordField = new JPasswordField();
        currentPasswordField.setFont(UITheme.NORMAL_FONT);
        currentPasswordField.setBorder(UITheme.INPUT_BORDER);
        gbc.gridx = 1; gbc.weightx = 0.62;
        panel.add(currentPasswordField, gbc);
        row++;

        // ---- Separator ----
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1;

        // ---- New Password ----
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.38;
        JLabel newLabel = new JLabel("New Password:");
        newLabel.setFont(UITheme.BOLD_SMALL_FONT);
        newLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(newLabel, gbc);

        newPasswordField = new JPasswordField();
        newPasswordField.setFont(UITheme.NORMAL_FONT);
        newPasswordField.setBorder(UITheme.INPUT_BORDER);
        gbc.gridx = 1; gbc.weightx = 0.62;
        panel.add(newPasswordField, gbc);
        row++;

        // ---- Confirm New Password ----
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.38;
        JLabel confirmLabel = new JLabel("Confirm Password:");
        confirmLabel.setFont(UITheme.BOLD_SMALL_FONT);
        confirmLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(confirmLabel, gbc);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(UITheme.NORMAL_FONT);
        confirmPasswordField.setBorder(UITheme.INPUT_BORDER);
        gbc.gridx = 1; gbc.weightx = 0.62;
        panel.add(confirmPasswordField, gbc);
        row++;

        // ---- Rules hint ----
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        JLabel rulesLabel = new JLabel(
                "<html><i>Min 6 characters, must include at least one letter and one number</i></html>");
        rulesLabel.setFont(UITheme.SMALL_FONT);
        rulesLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(rulesLabel, gbc);

        // ---- Status label ----
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.ACCENT_RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = row++;
        panel.add(statusLabel, gbc);

        // Enter on confirm field triggers save
        confirmPasswordField.addActionListener(e -> attemptChangePassword());

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR));

        JButton cancelBtn = UITheme.createDashboardButton("CANCEL");
        cancelBtn.setPreferredSize(new Dimension(100, 38));
        cancelBtn.addActionListener(e -> dispose());

        saveBtn = UITheme.createPrimaryButton("SAVE PASSWORD", UITheme.ACCENT_DARK);
        saveBtn.setPreferredSize(new Dimension(160, 38));
        saveBtn.addActionListener(e -> attemptChangePassword());

        panel.add(cancelBtn);
        panel.add(saveBtn);
        return panel;
    }

    private void attemptChangePassword() {
        String currentPass  = new String(currentPasswordField.getPassword());
        String newPass      = new String(newPasswordField.getPassword());
        String confirmPass  = new String(confirmPasswordField.getPassword());

        statusLabel.setForeground(UITheme.ACCENT_RED);

        // All fields required
        if (currentPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            statusLabel.setText("All fields are required.");
            return;
        }

        // New passwords must match
        if (!newPass.equals(confirmPass)) {
            statusLabel.setText("New passwords do not match.");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            newPasswordField.requestFocus();
            return;
        }

        // Must be different from current
        if (currentPass.equals(newPass)) {
            statusLabel.setText("New password must differ from current password.");
            return;
        }

        // Strength check via ValidationService
        ValidationService.ValidationResult result = validationService.validatePassword(newPass);
        if (!result.isValid()) {
            statusLabel.setText(result.getFieldErrors()
                    .getOrDefault("password", "Invalid password."));
            return;
        }

        saveBtn.setEnabled(false);
        saveBtn.setText("SAVING...");

        // Uses the 3-param overload: changePassword(username, oldPassword, newPassword)
        boolean success = userService.changePassword(
                currentUser.getUsername(), currentPass, newPass);

        saveBtn.setEnabled(true);
        saveBtn.setText("SAVE PASSWORD");

        if (success) {
            passwordChanged = true;
            statusLabel.setForeground(new Color(34, 139, 34));
            statusLabel.setText("Password changed successfully!");

            // Auto-close after short delay
            Timer timer = new Timer(1400, e -> dispose());
            timer.setRepeats(false);
            timer.start();
        } else {
            statusLabel.setText("Current password is incorrect.");
            currentPasswordField.setText("");
            currentPasswordField.requestFocus();
        }
    }

    /** Returns true if the password was successfully changed before dialog closed */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }
}