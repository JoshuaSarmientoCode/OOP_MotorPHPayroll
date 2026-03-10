package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class TicketManagementPanel extends JPanel {

    private final MainController controller;
    private final TicketService ticketService;
    private final SystemLogService logService;
    private final User currentUser;

    // UI Components
    private JTable ticketTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCombo;
    private JComboBox<Ticket.TicketPriority> priorityFilterCombo;
    private JTextField searchField;

    // Stats Labels
    private JLabel totalLabel;
    private JLabel openLabel;
    private JLabel criticalLabel;
    private JLabel resolvedLabel;

    // Buttons
    private JButton backBtn;
    private JButton refreshBtn;
    private JButton viewBtn;
    private JButton assignBtn;
    private JButton updateStatusBtn;
    private JButton exportBtn;

    private List<Ticket> allTickets;

    public TicketManagementPanel(MainController controller, TicketService ticketService,
                                 SystemLogService logService, User currentUser) {
        this.controller = controller;
        this.ticketService = ticketService;
        this.logService = logService;
        this.currentUser = currentUser;
        this.allTickets = new ArrayList<>();

        initializePanel();
        loadData();
    }

    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);

        // --- TOP SECTION CONTAINER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        topContainer.add(createHeaderPanel());
        topContainer.add(createStatsPanel());
        topContainer.add(createFilterPanel());

        add(topContainer, BorderLayout.NORTH);

        // Table
        add(createTablePanel(), BorderLayout.CENTER);

        // Bottom panel
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);

        // Bold Black Back Button
        backBtn = createDashboardStyleButton("← BACK");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.addActionListener(e -> controller.showDashboard());

        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));

        JLabel titleLabel = new JLabel("TICKET MANAGEMENT");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);

        headerPanel.add(leftHeader, BorderLayout.WEST);

        return headerPanel;
    }

    // --- DASHBOARD STYLE BUTTON HELPER ---
    private JButton createDashboardStyleButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(UITheme.TEXT_PRIMARY);
        btn.setBackground(UITheme.CARD_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(245, 245, 245));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Color.BLACK, 1),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(UITheme.CARD_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                        BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
        });
        return btn;
    }

    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        totalLabel = new JLabel("0", SwingConstants.CENTER);
        openLabel = new JLabel("0", SwingConstants.CENTER);
        criticalLabel = new JLabel("0", SwingConstants.CENTER);
        resolvedLabel = new JLabel("0", SwingConstants.CENTER);

        panel.add(createStatCard("TOTAL TICKETS", totalLabel, UITheme.TEXT_PRIMARY));
        panel.add(createStatCard("OPEN", openLabel, UITheme.ACCENT_ORANGE));
        panel.add(createStatCard("CRITICAL", criticalLabel, UITheme.ACCENT_RED));
        panel.add(createStatCard("RESOLVED", resolvedLabel, UITheme.ACCENT_GREEN));

        return panel;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UITheme.BOLD_SMALL_FONT);
        titleLabel.setForeground(UITheme.TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(color);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        panel.add(new JLabel("STATUS:"));
        filterCombo = new JComboBox<>(new String[]{"ALL", "OPEN", "IN PROGRESS", "RESOLVED", "CLOSED", "CRITICAL"});
        filterCombo.setPreferredSize(new Dimension(120, 35));
        filterCombo.addActionListener(e -> filterTickets());
        panel.add(filterCombo);

        panel.add(new JLabel("PRIORITY:"));
        priorityFilterCombo = new JComboBox<>(Ticket.TicketPriority.values());
        priorityFilterCombo.setPreferredSize(new Dimension(100, 35));
        priorityFilterCombo.addActionListener(e -> filterTickets());
        panel.add(priorityFilterCombo);

        panel.add(new JLabel("SEARCH:"));
        searchField = new JTextField(15);
        searchField.setBorder(UITheme.INPUT_BORDER);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTickets(); }
        });
        panel.add(searchField);

        refreshBtn = createDashboardStyleButton("REFRESH");
        refreshBtn.addActionListener(e -> loadData());
        panel.add(refreshBtn);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        String[] columns = {"ID", "DATE", "EMPLOYEE", "CATEGORY", "SUBJECT", "PRIORITY", "STATUS", "ASSIGNED TO"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        ticketTable = new JTable(tableModel);
        ticketTable.setRowHeight(40);
        ticketTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        ticketTable.setShowGrid(true);
        ticketTable.setGridColor(UITheme.BORDER_COLOR);
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Priority column renderer
        ticketTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String priority = value != null ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));

                if ("CRITICAL".equals(priority)) setForeground(UITheme.ACCENT_RED);
                else if ("HIGH".equals(priority)) setForeground(UITheme.ACCENT_ORANGE);
                else if ("MEDIUM".equals(priority)) setForeground(UITheme.ACCENT_BLUE);
                else setForeground(UITheme.ACCENT_GREEN);
                return c;
            }
        });

        // Status column renderer
        ticketTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String status = value != null ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);

                if ("OPEN".equals(status)) setForeground(UITheme.ACCENT_ORANGE);
                else if ("IN_PROGRESS".equals(status)) setForeground(UITheme.ACCENT_BLUE);
                else if ("RESOLVED".equals(status)) setForeground(UITheme.ACCENT_GREEN);
                else setForeground(UITheme.TEXT_SECONDARY);
                return c;
            }
        });

        ticketTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewTicketDetails();
            }
        });

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setOpaque(false);

        viewBtn = createDashboardStyleButton("VIEW DETAILS");
        viewBtn.addActionListener(e -> viewTicketDetails());
        panel.add(viewBtn);

        assignBtn = createDashboardStyleButton("ASSIGN");
        assignBtn.addActionListener(e -> assignTicket());
        panel.add(assignBtn);

        updateStatusBtn = createDashboardStyleButton("UPDATE STATUS");
        updateStatusBtn.addActionListener(e -> updateTicketStatus());
        panel.add(updateStatusBtn);

        exportBtn = createDashboardStyleButton("EXPORT");
        exportBtn.addActionListener(e -> exportTickets());
        panel.add(exportBtn);

        return panel;
    }

    private void loadData() {
        try {
            allTickets = ticketService.getAllTickets();
            updateTable(allTickets);
            updateStatistics();
        } catch (Exception e) {
            controller.showError("Error loading tickets: " + e.getMessage());
        }
    }

    private void updateTable(List<Ticket> tickets) {
        tableModel.setRowCount(0);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        for (Ticket t : tickets) {
            tableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getCreatedDate().format(fmt),
                    t.getEmployeeName(),
                    t.getCategoryDisplay(),
                    truncate(t.getSubject(), 30),
                    t.getPriority(),
                    t.getStatus(),
                    t.getAssignedTo() != null ? t.getAssignedTo() : "—"
            });
        }
    }

    private void updateStatistics() {
        Map<String, Object> stats = ticketService.getTicketStatistics();
        totalLabel.setText(String.valueOf(stats.getOrDefault("total", 0)));
        openLabel.setText(String.valueOf(stats.getOrDefault("open", 0)));
        criticalLabel.setText(String.valueOf(stats.getOrDefault("critical", 0)));
        resolvedLabel.setText(String.valueOf(stats.getOrDefault("resolved", 0)));
    }

    private void filterTickets() {
        String filter = (String) filterCombo.getSelectedItem();
        Ticket.TicketPriority priorityFilter = (Ticket.TicketPriority) priorityFilterCombo.getSelectedItem();
        String searchTerm = searchField.getText().trim().toLowerCase();

        List<Ticket> filtered = new ArrayList<>(allTickets);

        if (!"ALL".equals(filter)) {
            if ("CRITICAL".equals(filter)) {
                filtered.removeIf(t -> t.getPriority() != Ticket.TicketPriority.CRITICAL);
            } else {
                filtered.removeIf(t -> !t.getStatus().toString().replace("_", " ").equals(filter));
            }
        }

        if (priorityFilter != null) {
            filtered.removeIf(t -> t.getPriority() != priorityFilter);
        }

        if (!searchTerm.isEmpty()) {
            filtered.removeIf(t -> !t.getSubject().toLowerCase().contains(searchTerm) &&
                    !t.getEmployeeName().toLowerCase().contains(searchTerm));
        }

        updateTable(filtered);
    }

    private void viewTicketDetails() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;

        JTextArea area = new JTextArea(ticket.getDescription());
        area.setEditable(false);
        area.setFont(UITheme.MONO_FONT);
        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Ticket Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void assignTicket() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        String staff = JOptionPane.showInputDialog(this, "Assign to:", ticket.getAssignedTo());
        if (staff != null) {
            try {
                ticketService.assignTicket(ticket.getTicketId(), staff, currentUser);
                loadData();
            } catch (Exception e) { controller.showError(e.getMessage()); }
        }
    }

    private void updateTicketStatus() {
        Ticket ticket = getSelectedTicket();
        if (ticket == null) return;
        String[] options = {"OPEN", "IN_PROGRESS", "RESOLVED", "CLOSED"};
        String status = (String) JOptionPane.showInputDialog(this, "New Status:", "Update", JOptionPane.QUESTION_MESSAGE, null, options, ticket.getStatus().toString());
        if (status != null) {
            try {
                ticketService.updateTicketStatus(ticket.getTicketId(), Ticket.TicketStatus.valueOf(status), null, null, currentUser);
                loadData();
            } catch (Exception e) { controller.showError(e.getMessage()); }
        }
    }

    private void exportTickets() {
        JFileChooser fc = new JFileChooser();
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            controller.showInfo("Exported.");
        }
    }

    private Ticket getSelectedTicket() {
        int row = ticketTable.getSelectedRow();
        if (row == -1) { controller.showWarning("Select a ticket."); return null; }
        return ticketService.getTicketById((String) tableModel.getValueAt(row, 0));
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}