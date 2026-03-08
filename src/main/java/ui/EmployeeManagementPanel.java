package ui;

import main.MainController;
import model.*;
import service.*;
import ui.components.UITheme;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class EmployeeManagementPanel extends JPanel {
    
    private final MainController controller;
    private final EmployeeService employeeService;
    private final UserService userService;
    
    private JTable employeeTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JLabel statusLabel;
    private JButton backBtn;
    private JButton addBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton viewBtn;
    private JButton refreshBtn;
    
    private List<Employee> allEmployees;
    
    public EmployeeManagementPanel(MainController controller, EmployeeService employeeService, 
                                   UserService userService) {
        this.controller = controller;
        this.employeeService = employeeService;
        this.userService = userService;
        this.allEmployees = new ArrayList<>();
        
        initializePanel();
        loadEmployeeData();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG_PRIMARY);
        setBorder(UITheme.PANEL_PADDING);
        
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);
        
        // Table
        add(createTablePanel(), BorderLayout.CENTER);
        
        // Bottom panel
        add(createBottomPanel(), BorderLayout.SOUTH);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Left side with back button and title
        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);
        
        backBtn = UITheme.createBackButton();
        backBtn.addActionListener(e -> controller.goBack());
        leftHeader.add(backBtn);
        leftHeader.add(Box.createRigidArea(new Dimension(20, 0)));
        
        JLabel titleLabel = new JLabel("EMPLOYEE MANAGEMENT");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_PRIMARY);
        leftHeader.add(titleLabel);
        
        headerPanel.add(leftHeader, BorderLayout.WEST);
        
        // Right side with toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);
        
        searchField = new JTextField(15);
        searchField.setFont(UITheme.NORMAL_FONT);
        searchField.setBorder(UITheme.INPUT_BORDER);
        searchField.putClientProperty("JTextField.placeholderText", "SEARCH");
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { searchEmployees(); }
        });
        toolbar.add(searchField);
        
        filterCombo = new JComboBox<>(new String[]{"ALL", "REGULAR", "PROBATIONARY", "CONTRACTUAL"});
        filterCombo.setFont(UITheme.NORMAL_FONT);
        filterCombo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        filterCombo.addActionListener(e -> filterEmployees());
        toolbar.add(filterCombo);
        
        refreshBtn = UITheme.createDashboardButton("REFRESH");
        refreshBtn.addActionListener(e -> refreshData());
        toolbar.add(refreshBtn);
        
        if (userService.hasAccess("EMPLOYEE_MANAGEMENT")) {
            addBtn = UITheme.createPrimaryButton("+ ADD EMPLOYEE", UITheme.ACCENT_GREEN);
            addBtn.addActionListener(e -> addEmployee());
            toolbar.add(addBtn);
        }
        
        headerPanel.add(toolbar, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.CARD_BG);
        panel.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        
        String[] columns = {"ID", "LAST NAME", "FIRST NAME", "POSITION", "STATUS", "DEPARTMENT", "BASIC SALARY"};
        tableModel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        
        employeeTable = new JTable(tableModel);
        employeeTable.setRowHeight(45);
        employeeTable.setFont(UITheme.NORMAL_FONT);
        employeeTable.setSelectionBackground(UITheme.SELECTION_BG);
        employeeTable.setSelectionForeground(UITheme.TEXT_PRIMARY);
        employeeTable.setShowGrid(true);
        employeeTable.setGridColor(UITheme.BORDER_COLOR);
        
        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(UITheme.BOLD_SMALL_FONT);
        header.setBackground(UITheme.HEADER_BG);
        header.setForeground(UITheme.TEXT_SECONDARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), 40));
        
        // Right align salary
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        employeeTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        
        // Double click to view details
        employeeTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) viewEmployeeDetails();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.CARD_BG);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        statusLabel = new JLabel("LOADING...");
        statusLabel.setFont(UITheme.SMALL_FONT);
        statusLabel.setForeground(UITheme.TEXT_SECONDARY);
        bottomPanel.add(statusLabel, BorderLayout.WEST);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);
        
        viewBtn = UITheme.createDashboardButton("VIEW DETAILS");
        viewBtn.addActionListener(e -> viewEmployeeDetails());
        actionPanel.add(viewBtn);
        
        if (userService.hasAccess("EMPLOYEE_MANAGEMENT")) {
            editBtn = UITheme.createDashboardButton("EDIT");
            editBtn.addActionListener(e -> editEmployee());
            actionPanel.add(editBtn);
            
            deleteBtn = UITheme.createDashboardButton("DELETE");
            deleteBtn.addActionListener(e -> deleteEmployee());
            actionPanel.add(deleteBtn);
        }
        
        bottomPanel.add(actionPanel, BorderLayout.EAST);
        
        return bottomPanel;
    }
    
    private void loadEmployeeData() {
        try {
            allEmployees = employeeService.getAllEmployees();
            updateTableData(allEmployees);
            statusLabel.setText(allEmployees.size() + " EMPLOYEES FOUND");
        } catch (Exception e) {
            statusLabel.setText("ERROR LOADING DATA");
            controller.showError("Failed to load employee data");
        }
    }
    
    private void updateTableData(List<Employee> employees) {
        tableModel.setRowCount(0);
        
        for (Employee emp : employees) {
            tableModel.addRow(new Object[]{
                emp.getEmployeeId(),
                emp.getLastName(),
                emp.getFirstName(),
                emp.getPosition(),
                emp.getStatus() != null ? emp.getStatus().getDisplayName() : "—",
                UITheme.getDepartment(emp.getPosition()),
                UITheme.formatCurrency(emp.getBasicSalary())
            });
        }
    }
    
    private void viewEmployeeDetails() {
        Employee emp = getSelectedEmployee();
        if (emp != null) {
            controller.showEmployeeDetails(emp);
        }
    }
    
    private void addEmployee() {
        controller.showEmployeeDialog(null, "ADD EMPLOYEE");
    }
    
    private void editEmployee() {
        Employee emp = getSelectedEmployee();
        if (emp != null) {
            controller.showEmployeeDialog(emp, "EDIT EMPLOYEE");
        }
    }
    
    private void deleteEmployee() {
        Employee emp = getSelectedEmployee();
        if (emp == null) return;
        
        String message = String.format("Delete employee %s, %s %s?",
            emp.getEmployeeId(), emp.getLastName(), emp.getFirstName());
        
        if (controller.showConfirm(message, "CONFIRM DELETE")) {
            try {
                if (employeeService.deleteEmployee(emp.getEmployeeId())) {
                    refreshData();
                    controller.showInfo("Employee deleted successfully");
                }
            } catch (Exception e) {
                controller.showError("Failed to delete employee: " + e.getMessage());
            }
        }
    }
    
    private void searchEmployees() {
        String keyword = searchField.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            updateTableData(allEmployees);
            statusLabel.setText(allEmployees.size() + " EMPLOYEES FOUND");
            return;
        }
        
        List<Employee> filtered = allEmployees.stream()
            .filter(e -> e.getEmployeeId().toLowerCase().contains(keyword) ||
                        e.getFirstName().toLowerCase().contains(keyword) ||
                        e.getLastName().toLowerCase().contains(keyword))
            .toList();
        
        updateTableData(filtered);
        statusLabel.setText(filtered.size() + " EMPLOYEES FOUND");
    }
    
    private void filterEmployees() {
        String filter = (String) filterCombo.getSelectedItem();
        if ("ALL".equals(filter)) {
            updateTableData(allEmployees);
            statusLabel.setText(allEmployees.size() + " EMPLOYEES FOUND");
            return;
        }
        
        EmploymentStatus status = EmploymentStatus.fromString(filter);
        List<Employee> filtered = allEmployees.stream()
            .filter(e -> e.getStatus() == status)
            .toList();
        
        updateTableData(filtered);
        statusLabel.setText(filtered.size() + " " + filter + " EMPLOYEES");
    }
    
    private Employee getSelectedEmployee() {
        int row = employeeTable.getSelectedRow();
        if (row == -1) {
            controller.showWarning("SELECT AN EMPLOYEE FIRST");
            return null;
        }
        
        String id = (String) tableModel.getValueAt(row, 0);
        return employeeService.getEmployeeById(id);
    }
    
    public void refreshData() {
        loadEmployeeData();
        searchField.setText("");
        filterCombo.setSelectedIndex(0);
    }
}