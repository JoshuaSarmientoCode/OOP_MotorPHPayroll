package ui;

import main.MainController;
import ui.components.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainFrame extends JFrame {
    
    private final MainController controller;
    private JPanel contentPanel;  
    private CardLayout cardLayout; 
    
    private JPanel currentPanel;
    private String currentPanelName;
    private Object currentPanelState;
    
    private JLabel statusBar;
    
    // ========== CONSTRUCTOR ==========
    
    public MainFrame(MainController controller) {
        this.controller = controller;
        
        initializeFrame();
        initializeComponents();
        
        // Add window listener for shutdown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.shutdown();
            }
        });
    }
    
    // ========== INITIALIZATION ==========
    
    private void initializeFrame() {
        setTitle("MotorPH Payroll System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 600));
        setLocationRelativeTo(null);
        
        // Set icon (if available)
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/resources/icon.png"));
            setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without it
        }
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Initialize content panel with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(UITheme.BG_PRIMARY);
        
        // Create status bar
        statusBar = createStatusBar();
        
        // Add components
        add(contentPanel, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JLabel createStatusBar() {
        JLabel status = new JLabel(" Ready");
        status.setFont(UITheme.SMALL_FONT);
        status.setForeground(UITheme.TEXT_SECONDARY);
        status.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        status.setBackground(UITheme.HEADER_BG);
        status.setOpaque(true);
        return status;
    }
    
    // ========== CONTENT MANAGEMENT ==========
    
    /**
     * Set main content panel
     */
    public void setMainContent(JPanel panel) {
        setMainContent(panel, null, null);
    }
    
    /**
     * Set main content with name
     */
    public void setMainContent(JPanel panel, String name) {
        setMainContent(panel, name, null);
    }
    
    /**
     * Set main content with name and state
     */
    public void setMainContent(JPanel panel, String name, Object state) {
        // Clear previous content
        contentPanel.removeAll();
        
        // Add new panel
        String id = name != null ? name : "CURRENT";
        contentPanel.add(panel, id);
        cardLayout.show(contentPanel, id);
        
        // Update current references
        currentPanel = panel;
        currentPanelName = name;
        currentPanelState = state;
        
        // Update status bar
        updateStatusBar(name);
        
        // Refresh UI
        revalidate();
        repaint();
    }
    
    /**
     * Update status bar text
     */
    private void updateStatusBar(String panelName) {
        StringBuilder status = new StringBuilder(" ");
        
        if (panelName != null) {
            status.append(panelName.replace("_", " ").toLowerCase());
            status.append(" • ");
        }
        
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        status.append(now.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")));
        
        statusBar.setText(status.toString());
    }
    
    // ========== GETTERS AND SETTERS ==========
    
    public JPanel getCurrentPanel() {
        return currentPanel;
    }
    
    public String getCurrentPanelName() {
        return currentPanelName;
    }
    
    public void setCurrentPanelName(String name) {
        this.currentPanelName = name;
        updateStatusBar(name);
    }
    
    public Object getCurrentPanelState() {
        return currentPanelState;
    }
    
    public void setCurrentPanelState(Object state) {
        this.currentPanelState = state;
    }
    
    public void setStatusMessage(String message) {
        statusBar.setText(" " + message);
    }
}