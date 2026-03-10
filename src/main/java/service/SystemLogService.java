package service;

import dao.SystemLogDAO;
import model.SystemLog;
import model.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class SystemLogService {

    private final SystemLogDAO logDAO;

    public SystemLogService(SystemLogDAO logDAO) {
        this.logDAO = logDAO;
    }

    public List<SystemLog> getAllLogs() {
        return logDAO.findAll();
    }

    public List<SystemLog> getLogsByUser(String userId) {
        return logDAO.findByUserId(userId);
    }

    public List<SystemLog> getLogsByLevel(SystemLog.LogLevel level) {
        return logDAO.findByLevel(level);
    }

    public List<SystemLog> getLogsByDate(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return logDAO.findByDateRange(start, end);
    }

    public List<SystemLog> getRecentLogs(int limit) {
        List<SystemLog> all = logDAO.findAll();
        return all.size() > limit ? all.subList(0, limit) : all;
    }

    public Map<String, Object> getLogStatistics() {
        return logDAO.getStatistics();
    }

    public void logInfo(String source, User user, String action, String details) {
        logDAO.logInfo(
                source,
                user != null ? user.getEmployeeId() : "SYSTEM",
                user != null ? user.getFullName() : "System",
                action,
                details
        );
    }

    public void logWarning(String source, User user, String action, String details) {
        logDAO.logWarning(
                source,
                user != null ? user.getEmployeeId() : "SYSTEM",
                user != null ? user.getFullName() : "System",
                action,
                details
        );
    }

    public void logError(String source, User user, String action, String details) {
        logDAO.logError(
                source,
                user != null ? user.getEmployeeId() : "SYSTEM",
                user != null ? user.getFullName() : "System",
                action,
                details
        );
    }

    public void logAudit(String source, User user, String action, String details) {
        logDAO.logAudit(
                source,
                user != null ? user.getEmployeeId() : "SYSTEM",
                user != null ? user.getFullName() : "System",
                action,
                details
        );
    }

    public void clearOldLogs(int daysToKeep) {
        logDAO.clearOldLogs(daysToKeep);
    }

    public boolean exportLogs(String filePath) {
        // Implementation for exporting logs to file
        return true;
    }
}