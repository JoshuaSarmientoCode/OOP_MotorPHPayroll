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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class SystemLogsPanel extends JPanel {

    private final MainController controller;
    private final SystemLogService logService;
    private final User currentUser;

    // UI Components
    private JTable logTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> levelFilterCombo;
    private JTextField searchField;
    private JComboBox<String> dateFilterCombo;

    // Stats Labels
    private JLabel totalLabel;
    private JLabel infoLabel;
    private JLabel warningLabel;
    private JLabel errorLabel;

    // Buttons
    private JButton backBtn;
    private JButton refreshBtn;
    private JButton exportBtn;
    private JButton clearOldBtn;

    private List<SystemLog> allLogs;

    public SystemLogsPanel(MainController controller, SystemLogService logService, User currentUser) {
        this.controller = controller;
        this.logService = logService;
        this.currentUser = currentUser;
        this.allLogs = new ArrayList<>();

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

        JLabel titleLabel = new JLabel("SYSTEM LOGS");
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
        infoLabel = new JLabel("0", SwingConstants.CENTER);
        warningLabel = new JLabel("0", SwingConstants.CENTER);
        errorLabel = new JLabel("0", SwingConstants.CENTER);

        panel.add(createStatCard("TOTAL LOGS", totalLabel, UITheme.TEXT_PRIMARY));
        panel.add(createStatCard("INFO", infoLabel, UITheme.ACCENT_BLUE));
        panel.add(createStatCard("WARNINGS", warningLabel, UITheme.ACCENT_ORANGE));
        panel.add(createStatCard("ERRORS", errorLabel, UITheme.ACCENT_RED));

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

        panel.add(new JLabel("LEVEL:"));
        levelFilterCombo = new JComboBox<>(new String[]{"ALL", "INFO", "WARNING", "ERROR", "AUDIT"});
        levelFilterCombo.setPreferredSize(new Dimension(100, 35));
        levelFilterCombo.addActionListener(e -> filterLogs());
        panel.add(levelFilterCombo);

        panel.add(new JLabel("DATE:"));
        dateFilterCombo = new JComboBox<>(new String[]{"ALL", "TODAY", "LAST 7 DAYS", "LAST 30 DAYS"});
        dateFilterCombo.setPreferredSize(new Dimension(120, 35));
        dateFilterCombo.addActionListener(e -> filterLogs());
        panel.add(dateFilterCombo);

        panel.add(new JLabel("SEARCH:"));
        searchField = new JTextField(20);
        searchField.setBorder(UITheme.INPUT_BORDER);
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterLogs(); }
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

        String[] columns = {"TIMESTAMP", "LEVEL", "SOURCE", "USER", "ACTION", "DETAILS"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        logTable = new JTable(tableModel);
        logTable.setRowHeight(35);
        logTable.getTableHeader().setFont(UITheme.BOLD_SMALL_FONT);
        logTable.setShowGrid(true);
        logTable.setGridColor(UITheme.BORDER_COLOR);

        logTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                String level = value != null ? value.toString() : "";
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(getFont().deriveFont(Font.BOLD));

                if ("ERROR".equals(level)) setForeground(UITheme.ACCENT_RED);
                else if ("WARNING".equals(level)) setForeground(UITheme.ACCENT_ORANGE);
                else if ("INFO".equals(level)) setForeground(UITheme.ACCENT_BLUE);
                else if ("AUDIT".equals(level)) setForeground(UITheme.ACCENT_PURPLE);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(logTable);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 15));
        panel.setOpaque(false);

        exportBtn = createDashboardStyleButton("EXPORT LOGS");
        exportBtn.addActionListener(e -> exportLogs());
        panel.add(exportBtn);

        clearOldBtn = createDashboardStyleButton("CLEAR OLD LOGS");
        clearOldBtn.addActionListener(e -> clearOldLogs());
        panel.add(clearOldBtn);

        return panel;
    }

    private void loadData() {
        try {
            allLogs = logService.getAllLogs();
            updateTable(allLogs);
            updateStatistics();
        } catch (Exception e) {
            controller.showError("Error loading logs: " + e.getMessage());
        }
    }

    private void updateTable(List<SystemLog> logs) {
        tableModel.setRowCount(0);
        for (SystemLog log : logs) {
            tableModel.addRow(new Object[]{
                    log.getFormattedTimestampReadable(),
                    log.getLevel(),
                    log.getSource(),
                    log.getUserName() != null ? log.getUserName() : log.getUserId(),
                    log.getAction(),
                    truncate(log.getDetails(), 50)
            });
        }
    }

    private void updateStatistics() {
        Map<String, Object> stats = logService.getLogStatistics();
        totalLabel.setText(String.valueOf(stats.getOrDefault("total", 0)));
        infoLabel.setText(String.valueOf(stats.getOrDefault("info", 0)));
        warningLabel.setText(String.valueOf(stats.getOrDefault("warning", 0)));
        errorLabel.setText(String.valueOf(stats.getOrDefault("error", 0)));
    }

    private void filterLogs() {
        String levelFilter = (String) levelFilterCombo.getSelectedItem();
        String dateFilter = (String) dateFilterCombo.getSelectedItem();
        String searchTerm = searchField.getText().trim().toLowerCase();

        List<SystemLog> filtered = new ArrayList<>(allLogs);

        if (!"ALL".equals(levelFilter)) {
            filtered.removeIf(l -> !l.getLevel().toString().equals(levelFilter));
        }

        LocalDateTime now = LocalDateTime.now();
        if ("TODAY".equals(dateFilter)) {
            filtered.removeIf(l -> l.getTimestamp().isBefore(now.toLocalDate().atStartOfDay()));
        } else if ("LAST 7 DAYS".equals(dateFilter)) {
            filtered.removeIf(l -> l.getTimestamp().isBefore(now.minusDays(7)));
        }

        if (!searchTerm.isEmpty()) {
            filtered.removeIf(l -> !l.getAction().toLowerCase().contains(searchTerm) &&
                    !l.getDetails().toLowerCase().contains(searchTerm));
        }

        updateTable(filtered);
    }

    private void exportLogs() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            controller.showInfo("Logs exported successfully.");
        }
    }

    private void clearOldLogs() {
        if (controller.showConfirm("Clear old logs?", "Confirm")) {
            try {
                logService.clearOldLogs(30);
                loadData();
                controller.showInfo("Old logs cleared.");
            } catch (Exception e) {
                controller.showError(e.getMessage());
            }
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}