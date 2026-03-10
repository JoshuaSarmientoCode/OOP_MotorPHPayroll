package dao;

import model.SystemLog;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SystemLogDAO extends BaseDAO<SystemLog> {

    private static final String[] HEADERS = {
            "Log ID", "Timestamp", "Level", "Source", "User ID", "User Name",
            "Action", "Details", "IP Address", "Session ID"
    };

    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public SystemLogDAO(String filePath) {
        super(filePath);
    }

    @Override
    public SystemLog fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 8) return null;

        SystemLog log = new SystemLog();

        try {
            log.setLogId(safeGet(data, 0));

            if (!safeGet(data, 1).isEmpty())
                log.setTimestamp(LocalDateTime.parse(safeGet(data, 1), DATETIME_FORMATTER));

            if (!safeGet(data, 2).isEmpty())
                log.setLevel(SystemLog.LogLevel.valueOf(safeGet(data, 2)));

            if (data.length > 3) log.setSource(safeGet(data, 3));
            if (data.length > 4) log.setUserId(safeGet(data, 4));
            if (data.length > 5) log.setUserName(safeGet(data, 5));
            if (data.length > 6) log.setAction(safeGet(data, 6));
            if (data.length > 7) log.setDetails(safeGet(data, 7).replace("\"", ""));
            if (data.length > 8) log.setIpAddress(safeGet(data, 8));
            if (data.length > 9) log.setSessionId(safeGet(data, 9));

        } catch (Exception e) {
            LOGGER.warning("Error parsing system log: " + e.getMessage());
            return null;
        }

        return log;
    }

    @Override
    public String toCSV(SystemLog log) {
        List<String> fields = new ArrayList<>();
        fields.add(log.getLogId());
        fields.add(log.getTimestamp() != null ? log.getTimestamp().format(DATETIME_FORMATTER) : "");
        fields.add(log.getLevel() != null ? log.getLevel().toString() : "INFO");
        fields.add(log.getSource() != null ? log.getSource() : "");
        fields.add(log.getUserId() != null ? log.getUserId() : "");
        fields.add(log.getUserName() != null ? log.getUserName() : "");
        fields.add(log.getAction() != null ? log.getAction() : "");
        fields.add(log.getDetails() != null ? "\"" + log.getDetails() + "\"" : "");
        fields.add(log.getIpAddress() != null ? log.getIpAddress() : "");
        fields.add(log.getSessionId() != null ? log.getSessionId() : "");
        return String.join(",", fields);
    }

    @Override
    protected String[] getHeaders() { return HEADERS; }

    @Override
    protected String getId(SystemLog item) { return item.getLogId(); }

    public List<SystemLog> findByUserId(String userId) {
        return cache.stream()
                .filter(l -> userId.equals(l.getUserId()))
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<SystemLog> findByLevel(SystemLog.LogLevel level) {
        return cache.stream()
                .filter(l -> l.getLevel() == level)
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<SystemLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        return cache.stream()
                .filter(l -> !l.getTimestamp().isBefore(start) && !l.getTimestamp().isAfter(end))
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public List<SystemLog> findAll() {
        return cache.stream()
                .sorted((l1, l2) -> l2.getTimestamp().compareTo(l1.getTimestamp()))
                .collect(Collectors.toList());
    }

    public boolean addLog(SystemLog log) { return add(log); }

    public void logInfo(String source, String userId, String userName, String action, String details) {
        SystemLog log = new SystemLog(SystemLog.LogLevel.INFO, source, userId, action, details);
        log.setUserName(userName);
        add(log);
    }

    public void logWarning(String source, String userId, String userName, String action, String details) {
        SystemLog log = new SystemLog(SystemLog.LogLevel.WARNING, source, userId, action, details);
        log.setUserName(userName);
        add(log);
    }

    public void logError(String source, String userId, String userName, String action, String details) {
        SystemLog log = new SystemLog(SystemLog.LogLevel.ERROR, source, userId, action, details);
        log.setUserName(userName);
        add(log);
    }

    public void logAudit(String source, String userId, String userName, String action, String details) {
        SystemLog log = new SystemLog(SystemLog.LogLevel.AUDIT, source, userId, action, details);
        log.setUserName(userName);
        add(log);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", cache.size());
        stats.put("info", cache.stream().filter(l -> l.getLevel() == SystemLog.LogLevel.INFO).count());
        stats.put("warning", cache.stream().filter(l -> l.getLevel() == SystemLog.LogLevel.WARNING).count());
        stats.put("error", cache.stream().filter(l -> l.getLevel() == SystemLog.LogLevel.ERROR).count());
        stats.put("audit", cache.stream().filter(l -> l.getLevel() == SystemLog.LogLevel.AUDIT).count());

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        stats.put("today", cache.stream()
                .filter(l -> l.getTimestamp().isAfter(startOfDay)).count());

        return stats;
    }

    public void clearOldLogs(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        cache.removeIf(l -> l.getTimestamp().isBefore(cutoff));
        writeToFile();
    }
}