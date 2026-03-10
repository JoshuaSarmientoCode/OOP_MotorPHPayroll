package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubmitTicketPanel extends JPanel {

    private final MainController controller;
    private final TicketService ticketService;
    private final SystemLogService logService;
    private final User currentUser;

    // UI Components
    private JComboBox<Ticket.TicketCategory> categoryCombo;
    private JComboBox<Ticket.TicketPriority> priorityCombo;
    private JTextField subjectField;
    private JTextArea descriptionArea;
    private JButton submitBtn;
    private JButton cancelBtn;
    private JLabel charCountLabel;

    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    public SubmitTicketPanel(MainController controller, TicketService ticketService,
                             SystemLogService logService, User currentUser) {
        this.controller = controller;
        this.ticketService = ticketService;
        this.logService = logService;
        this.currentUser = currentUser;

        initializePanel();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);

        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Form
        add(createFormPanel(), BorderLayout.CENTER);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);

        JButton backBtn = UITheme.createBackButton();
        backBtn.addActionListener(e -> controller.goBack());
        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));

        JLabel titleLabel = new JLabel("SUBMIT TICKET");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);

        JLabel userLabel = new JLabel(currentUser.getEmployeeId() + " | " + currentUser.getFullName());
        userLabel.setFont(UITheme.NORMAL_FONT);
        userLabel.setForeground(UITheme.TEXT_SECONDARY);
        headerPanel.add(userLabel, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Category
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(createLabel("Category:"), gbc);

        gbc.gridx = 1;
        categoryCombo = new JComboBox<>(Ticket.TicketCategory.values());
        categoryCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                if (value instanceof Ticket.TicketCategory) {
                    value = ((Ticket.TicketCategory) value).getDisplayName();
                }
                return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        });
        categoryCombo.setFont(UITheme.NORMAL_FONT);
        categoryCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        categoryCombo.setPreferredSize(new Dimension(300, 35));
        panel.add(categoryCombo, gbc);
        row++;

        // Priority
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Priority:"), gbc);

        gbc.gridx = 1;
        priorityCombo = new JComboBox<>(Ticket.TicketPriority.values());
        priorityCombo.setFont(UITheme.NORMAL_FONT);
        priorityCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        priorityCombo.setPreferredSize(new Dimension(300, 35));
        panel.add(priorityCombo, gbc);
        row++;

        // Subject
        gbc.gridx = 0; gbc.gridy = row;
        panel.add(createLabel("Subject:"), gbc);

        gbc.gridx = 1;
        subjectField = new JTextField();
        subjectField.setFont(UITheme.NORMAL_FONT);
        subjectField.setBorder(UITheme.INPUT_BORDER);
        subjectField.setPreferredSize(new Dimension(400, 35));
        panel.add(subjectField, gbc);
        row++;

        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(createLabel("Description:"), gbc);

        gbc.gridx = 1;
        descriptionArea = new JTextArea(8, 50);
        descriptionArea.setFont(UITheme.NORMAL_FONT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        descriptionArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int length = descriptionArea.getText().length();
                charCountLabel.setText(length + "/" + MAX_DESCRIPTION_LENGTH);
                if (length > MAX_DESCRIPTION_LENGTH) {
                    charCountLabel.setForeground(UITheme.ACCENT_RED);
                } else {
                    charCountLabel.setForeground(UITheme.TEXT_SECONDARY);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, gbc);
        row++;

        // Character count
        gbc.gridx = 1; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        charCountLabel = new JLabel("0/" + MAX_DESCRIPTION_LENGTH);
        charCountLabel.setFont(UITheme.SMALL_FONT);
        charCountLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(charCountLabel, gbc);
        row++;

        // Buttons
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(30, 10, 10, 10);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        submitBtn = UITheme.createPrimaryButton("SUBMIT TICKET", UITheme.ACCENT_GREEN);
        submitBtn.setPreferredSize(new Dimension(180, 45));
        submitBtn.addActionListener(e -> submitTicket());
        buttonPanel.add(submitBtn);

        cancelBtn = UITheme.createDashboardButton("CANCEL");
        cancelBtn.setPreferredSize(new Dimension(180, 45));
        cancelBtn.addActionListener(e -> controller.goBack());
        buttonPanel.add(cancelBtn);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.BOLD_SMALL_FONT);
        label.setForeground(UITheme.TEXT_SECONDARY);
        return label;
    }

    private void submitTicket() {
        // Validate inputs
        String subject = subjectField.getText().trim();
        if (subject.isEmpty()) {
            controller.showError("Please enter a subject");
            return;
        }

        String description = descriptionArea.getText().trim();
        if (description.isEmpty()) {
            controller.showError("Please enter a description");
            return;
        }

        if (description.length() < 10) {
            controller.showError("Description must be at least 10 characters");
            return;
        }

        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            controller.showError("Description exceeds maximum length of " + MAX_DESCRIPTION_LENGTH);
            return;
        }

        // Create ticket
        Ticket ticket = new Ticket();
        ticket.setEmployeeId(currentUser.getEmployeeId());
        ticket.setEmployeeName(currentUser.getFullName());
        ticket.setSubject(subject);
        ticket.setDescription(description);
        ticket.setCategory((Ticket.TicketCategory) categoryCombo.getSelectedItem());
        ticket.setPriority((Ticket.TicketPriority) priorityCombo.getSelectedItem());

        try {
            boolean success = ticketService.createTicket(ticket, currentUser);

            if (success) {
                logService.logInfo(
                        "SubmitTicketPanel",
                        currentUser,
                        "TICKET_CREATED",
                        "Created ticket: " + ticket.getSubject()
                );

                controller.showInfo("Ticket submitted successfully!\nTicket ID: " + ticket.getTicketId());

                // Clear form
                subjectField.setText("");
                descriptionArea.setText("");
                categoryCombo.setSelectedIndex(0);
                priorityCombo.setSelectedItem(Ticket.TicketPriority.MEDIUM);

                // Go back to previous panel
                controller.goBack();
            } else {
                controller.showError("Failed to submit ticket");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logService.logError(
                    "SubmitTicketPanel",
                    currentUser,
                    "TICKET_ERROR",
                    "Error submitting ticket: " + e.getMessage()
            );
            controller.showError("Error submitting ticket: " + e.getMessage());
        }
    }
}