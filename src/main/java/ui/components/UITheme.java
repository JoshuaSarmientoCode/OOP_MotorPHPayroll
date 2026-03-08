package ui.components;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UITheme {
    
    // ========== COLORS ==========
    public static final Color BG_PRIMARY = new Color(242, 242, 245);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(200, 200, 200);
    public static final Color HEADER_BG = new Color(245, 245, 245);
    public static final Color SELECTION_BG = new Color(230, 230, 230);
    public static final Color TEXT_PRIMARY = new Color(20, 20, 20);
    public static final Color TEXT_SECONDARY = new Color(90, 90, 90);
    public static final Color ACCENT_DARK = new Color(0, 0, 0);
    public static final Color ACCENT_GREEN = new Color(0, 150, 0);
    public static final Color ACCENT_RED = new Color(200, 0, 0);
    public static final Color ACCENT_BLUE = new Color(0, 102, 204);
    public static final Color ACCENT_ORANGE = new Color(255, 140, 0);
    public static final Color ACCENT_PURPLE = new Color(128, 0, 128);
    
    // ========== FONTS ==========
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font SUBHEADER_FONT = new Font("SansSerif", Font.BOLD, 14);
    public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font SMALL_FONT = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font BOLD_SMALL_FONT = new Font("SansSerif", Font.BOLD, 11);
    public static final Font MONO_FONT = new Font("Monospaced", Font.PLAIN, 12);
    
    // ========== BORDERS ==========
    public static final Border CARD_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(20, 20, 20, 20)
    );
    
    public static final Border INPUT_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR),
        BorderFactory.createEmptyBorder(8, 10, 8, 10)
    );
    
    public static final Border PANEL_PADDING = BorderFactory.createEmptyBorder(20, 40, 30, 40);
    
    // ========== BUTTON FACTORIES ==========
    
    /**
     * Create a standard dashboard button with black text
     */
    public static JButton createDashboardButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { 
                btn.setBackground(new Color(245, 245, 245));
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(ACCENT_DARK, 1),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            public void mouseExited(MouseEvent e) { 
                btn.setBackground(CARD_BG);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
        });
        
        return btn;
    }
    
    /**
     * Create a primary action button (colored background)
     */
    public static JButton createPrimaryButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bgColor.darker());
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bgColor);
            }
        });
        
        return btn;
    }
    
    /**
     * Create a back button with arrow
     */
    public static JButton createBackButton() {
        JButton btn = createDashboardButton("← BACK");
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        return btn;
    }
    
    // ========== PANEL FACTORIES ==========
    
    /**
     * Create a metric card (for dashboard stats)
     */
    public static JPanel createMetricCard(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(BOLD_SMALL_FONT);
        titleLabel.setForeground(TEXT_SECONDARY);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setForeground(TEXT_PRIMARY);
        panel.add(valueLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create a data card (for displaying information in 2-column format)
     */
    public static JPanel createDataCard(String title, String[][] data) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(CARD_BORDER);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(SUBHEADER_FONT);
        titleLabel.setForeground(ACCENT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel content = new JPanel(new GridLayout(data.length, 2, 0, 10));
        content.setOpaque(false);
        
        for (String[] row : data) {
            JLabel keyLabel = new JLabel(row[0]);
            keyLabel.setFont(BOLD_SMALL_FONT);
            keyLabel.setForeground(TEXT_SECONDARY);
            
            JLabel valueLabel = new JLabel(row[1]);
            valueLabel.setFont(NORMAL_FONT);
            valueLabel.setForeground(TEXT_PRIMARY);
            
            content.add(keyLabel);
            content.add(valueLabel);
        }
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Create a section header
     */
    public static JLabel createSectionHeader(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SUBHEADER_FONT);
        label.setForeground(ACCENT_DARK);
        return label;
    }
    
    // ========== UTILITY METHODS ==========
    
    /**
     * Format phone number
     */
    public static String formatPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone != null ? phone : "—";
        return phone.substring(0, 4) + "-" + phone.substring(4, 7) + "-" + phone.substring(7);
    }
    
    /**
     * Get department from position
     */
    public static String getDepartment(String position) {
        if (position == null) return "—";
        if (position.contains("HR")) return "HUMAN RESOURCES";
        if (position.contains("IT")) return "INFORMATION TECHNOLOGY";
        if (position.contains("Account") || position.contains("Finance")) return "FINANCE";
        if (position.contains("Sales")) return "SALES & MARKETING";
        if (position.contains("Chief") || position.contains("CEO") || 
            position.contains("CFO") || position.contains("COO")) return "EXECUTIVE";
        return "OPERATIONS";
    }
    
    /**
     * Truncate string with ellipsis
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
    
    /**
     * Format currency
     */
    public static String formatCurrency(double amount) {
        return String.format("₱ %,.2f", amount);
    }
}