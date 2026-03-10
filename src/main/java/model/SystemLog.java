package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SystemLog {
    private String logId;
    private LocalDateTime timestamp;
    private LogLevel level;
    private String source;
    private String userId;
    private String userName;
    private String action;
    private String details;
    private String ipAddress;
    private String sessionId;

    public enum LogLevel {
        INFO, WARNING, ERROR, DEBUG, AUDIT
    }

    public SystemLog() {
        this.logId = "LOG" + System.currentTimeMillis();
        this.timestamp = LocalDateTime.now();
        this.level = LogLevel.INFO;
    }

    public SystemLog(LogLevel level, String source, String userId, String action, String details) {
        this.logId = "LOG" + System.currentTimeMillis();
        this.timestamp = LocalDateTime.now();
        this.level = level;
        this.source = source;
        this.userId = userId;
        this.action = action;
        this.details = details;
    }

    // ========== GETTERS ==========

    public String getLogId() { return logId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LogLevel getLevel() { return level; }
    public String getSource() { return source; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public String getIpAddress() { return ipAddress; }
    public String getSessionId() { return sessionId; }

    // ========== SETTERS ==========

    public void setLogId(String logId) { this.logId = logId; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public void setLevel(LogLevel level) { this.level = level; }
    public void setSource(String source) { this.source = source; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setAction(String action) { this.action = action; }
    public void setDetails(String details) { this.details = details; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    // ========== BUSINESS METHODS ==========

    public String getFormattedTimestamp() {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getFormattedTimestampReadable() {
        return timestamp.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a"));
    }

    public String getLevelBadge() {
        switch (level) {
            case INFO: return "INFO";
            case WARNING: return "WARNING";
            case ERROR: return "ERROR";
            case DEBUG: return "DEBUG";
            case AUDIT: return "AUDIT";
            default: return level.toString();
        }
    }

    public String getLevelColor() {
        switch (level) {
            case INFO: return "BLUE";
            case WARNING: return "ORANGE";
            case ERROR: return "RED";
            case DEBUG: return "GREEN";
            case AUDIT: return "PURPLE";
            default: return "GRAY";
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] [%s] %s - %s",
                getFormattedTimestamp(), level, source, action);
    }
}