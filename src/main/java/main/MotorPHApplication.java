package main;

import javax.swing.*;
import java.io.File;
import java.util.logging.*;

public class MotorPHApplication {

    private static final Logger LOGGER = Logger.getLogger(MotorPHApplication.class.getName());

    public static void main(String[] args) {
        // Configure logging
        configureLogging();

        LOGGER.info("Starting MotorPH Payroll System...");

        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.warning("Could not set system look and feel: " + e.getMessage());
        }

        // Create data directory
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                LOGGER.info("Created data directory");
            }
        }

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Application shutting down...");
        }));

        // Start application with controller
        SwingUtilities.invokeLater(() -> {
            try {
                MainController controller = new MainController();
                controller.start();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to start application", e);
                JOptionPane.showMessageDialog(null,
                        "Failed to start application: " + e.getMessage(),
                        "Fatal Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }

    /**
     * Configure logging for the application
     */
    private static void configureLogging() {
        // Set up console logging
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                return String.format("[%1$tF %1$tT] [%2$s] %3$s %n",
                        new java.util.Date(record.getMillis()),
                        record.getLevel().getName(),
                        record.getMessage());
            }
        });

        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(Level.INFO);
        rootLogger.addHandler(consoleHandler);

        // Remove default handlers
        Handler[] handlers = rootLogger.getHandlers();
        for (Handler handler : handlers) {
            if (!(handler instanceof ConsoleHandler)) {
                rootLogger.removeHandler(handler);
            }
        }

        // Set specific log levels for packages
        Logger.getLogger("main").setLevel(Level.FINE);
        Logger.getLogger("dao").setLevel(Level.FINE);
        Logger.getLogger("service").setLevel(Level.FINE);
        Logger.getLogger("ui").setLevel(Level.INFO);
    }
}