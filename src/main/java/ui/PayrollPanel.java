package ui;

import main.MainController;
import model.*;
import service.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class PayrollPanel extends JPanel {
    private final MainController controller;
    private final PayrollService payrollService;
    private final User currentUser;
    
    // Monochrome theme
    private final Color BG_PRIMARY = new Color(242, 242, 245);
    private final Color CARD_BG = Color.WHITE;
    private final Color BORDER_COLOR = new Color(200, 200, 200);
    private final Color TEXT_PRIMARY = new Color(20, 20, 20);
    private final Color TEXT_SECONDARY = new Color(90, 90, 90);
    private final Color ACCENT_DARK = new Color(0, 0, 0);
    
    public PayrollPanel(MainController controller, PayrollService payrollService, User currentUser) {
        this.controller = controller;
        this.payrollService = payrollService;
        this.currentUser = currentUser;
        
        setLayout(new BorderLayout());
        setBackground(BG_PRIMARY);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_PRIMARY);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("PAYROLL SYSTEM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setForeground(TEXT_PRIMARY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BG_PRIMARY);
        
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(40, 60, 40, 60)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JButton viewBtn = createButton("VIEW PAYROLL HISTORY");
        viewBtn.addActionListener(e -> viewHistory());
        gbc.gridx = 0; gbc.gridy = 0;
        card.add(viewBtn, gbc);
        
        JButton generateBtn = createButton("GENERATE PAYSLIP");
        generateBtn.addActionListener(e -> generatePayslip());
        gbc.gridy = 1;
        card.add(generateBtn, gbc);
        
        // Check access using User's canAccess method or role check
        if (currentUser.canAccess("PAYROLL") || 
            currentUser.getRole() == User.Role.ADMIN || 
            currentUser.getRole() == User.Role.FINANCE) {
            
            JButton processBtn = createButton("PROCESS PAYROLL");
            processBtn.addActionListener(e -> processPayroll());
            gbc.gridy = 2;
            card.add(processBtn, gbc);
        }
        
        centerPanel.add(card);
        add(centerPanel, BorderLayout.CENTER);
    }
    
    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(250, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(40, 40, 40));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(ACCENT_DARK);
            }
        });
        
        return btn;
    }
    
    private void viewHistory() {
        JOptionPane.showMessageDialog(this, "PAYROLL HISTORY FEATURE COMING SOON");
    }
    
    private void generatePayslip() {
        JOptionPane.showMessageDialog(this, "PAYSLIP GENERATION COMING SOON");
    }
    
    private void processPayroll() {
        JOptionPane.showMessageDialog(this, "PAYROLL PROCESSING COMING SOON");
    }
}