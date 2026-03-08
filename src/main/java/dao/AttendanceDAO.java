package dao;

import model.Attendance;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

public class AttendanceDAO extends BaseDAO<Attendance> {
    
    private static final String[] HEADERS = {
        "Employee #", "Last Name", "First Name", "Date", "Log In", "Log Out"
    };
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    
    public AttendanceDAO(String filePath) {
        super(filePath);
    }
    
    @Override
    public Attendance fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 5) return null;
        
        Attendance attendance = new Attendance();
        
        try {
            attendance.setEmployeeId(safeGet(data, 0));
            attendance.setLastName(safeGet(data, 1));
            attendance.setFirstName(safeGet(data, 2));
            
            String dateStr = safeGet(data, 3);
            if (!dateStr.isEmpty()) {
                attendance.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
            }
            
            String timeInStr = safeGet(data, 4);
            if (!timeInStr.isEmpty()) {
                attendance.setTimeIn(LocalTime.parse(timeInStr, TIME_FORMATTER));
            }
            
            if (data.length > 5 && !safeGet(data, 5).isEmpty()) {
                String timeOutStr = safeGet(data, 5);
                attendance.setTimeOut(LocalTime.parse(timeOutStr, TIME_FORMATTER));
                
                // Calculate hours worked
                if (attendance.getTimeIn() != null) {
                    long minutes = Duration.between(attendance.getTimeIn(), attendance.getTimeOut()).toMinutes();
                    double hours = minutes / 60.0;
                    attendance.setHoursWorked(hours);
                    
                    // Calculate overtime (hours beyond 8)
                    if (hours > 8.0) {
                        attendance.setOvertimeHours(hours - 8.0);
                    }
                    
                    // Calculate late (assuming 8:00 AM start)
                    LocalTime standardStart = LocalTime.of(8, 0);
                    if (attendance.getTimeIn().isAfter(standardStart)) {
                        long lateMinutes = Duration.between(standardStart, attendance.getTimeIn()).toMinutes();
                        attendance.setLateHours(lateMinutes / 60.0);
                    }
                    
                    // Set status
                    if (hours >= 8.0) {
                        attendance.setStatus("COMPLETED");
                    } else if (hours >= 4.0) {
                        attendance.setStatus("HALF_DAY");
                    } else {
                        attendance.setStatus("UNDERTIME");
                    }
                }
            } else {
                if (attendance.getTimeIn() != null) {
                    attendance.setStatus("CLOCKED_IN");
                } else {
                    attendance.setStatus("NO_TIME_IN");
                }
            }
            
        } catch (Exception e) {
            LOGGER.warning("Error parsing attendance: " + e.getMessage());
            return null;
        }
        
        return attendance;
    }
    
    @Override
    public String toCSV(Attendance att) {
        return String.join(",",
            att.getEmployeeId(),
            att.getLastName(),
            att.getFirstName(),
            att.getDate() != null ? att.getDate().format(DATE_FORMATTER) : "",
            att.getTimeIn() != null ? att.getTimeIn().format(TIME_FORMATTER) : "",
            att.getTimeOut() != null ? att.getTimeOut().format(TIME_FORMATTER) : ""
        );
    }
    
    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }
    
    @Override
    protected String getId(Attendance item) {
        return item.getEmployeeId() + "_" + 
               (item.getDate() != null ? item.getDate().toString() : "");
    }
    
    private String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }
    
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        
        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }
    
    // ========== BUSINESS METHODS ==========
    
    public List<Attendance> findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                .collect(Collectors.toList());
    }
    
    public List<Attendance> findByDateRange(LocalDate start, LocalDate end) {
        return cache.stream()
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .collect(Collectors.toList());
    }
    
    public List<Attendance> findByEmployeeAndDateRange(String employeeId, LocalDate start, LocalDate end) {
        return cache.stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                .collect(Collectors.toList());
    }
    
    public Attendance findTodayAttendance(String employeeId) {
        LocalDate today = LocalDate.now();
        return cache.stream()
                .filter(a -> a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null && a.getDate().equals(today))
                .findFirst()
                .orElse(null);
    }
    
    public Map<String, Object> getStatistics(String employeeId, YearMonth period) {
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        
        List<Attendance> records = findByEmployeeAndDateRange(employeeId, start, end);
        
        Map<String, Object> stats = new HashMap<>();
        
        double totalHours = 0;
        double totalOvertime = 0;
        double totalLate = 0;
        int presentDays = 0;
        int totalRecords = records.size();
        
        for (Attendance a : records) {
            if (a.getTimeOut() != null) {
                totalHours += a.getHoursWorked();
                totalOvertime += a.getOvertimeHours();
                totalLate += a.getLateHours();
                presentDays++;
            }
        }
        
        stats.put("totalRecords", totalRecords);
        stats.put("presentDays", presentDays);
        stats.put("totalHours", totalHours);
        stats.put("totalOvertime", totalOvertime);
        stats.put("totalLate", totalLate);
        stats.put("averageHoursPerDay", presentDays > 0 ? totalHours / presentDays : 0);
        
        return stats;
    }
    
    public boolean timeIn(String employeeId, String firstName, String lastName) {
        // Check if already timed in today
        Attendance existing = findTodayAttendance(employeeId);
        if (existing != null) {
            return false;
        }
        
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setFirstName(firstName);
        attendance.setLastName(lastName);
        attendance.setDate(LocalDate.now());
        attendance.setTimeIn(LocalTime.now());
        attendance.setStatus("CLOCKED_IN");
        
        return add(attendance);
    }
    
    public boolean timeOut(String employeeId) {
        Attendance today = findTodayAttendance(employeeId);
        if (today == null || today.getTimeOut() != null) {
            return false;
        }
        
        LocalTime timeOut = LocalTime.now();
        today.setTimeOut(timeOut);
        
        // Calculate hours worked
        long minutes = Duration.between(today.getTimeIn(), timeOut).toMinutes();
        double hours = minutes / 60.0;
        today.setHoursWorked(hours);
        
        // Calculate overtime
        if (hours > 8.0) {
            today.setOvertimeHours(hours - 8.0);
        }
        
        // Calculate late
        LocalTime standardStart = LocalTime.of(8, 0);
        if (today.getTimeIn().isAfter(standardStart)) {
            long lateMinutes = Duration.between(standardStart, today.getTimeIn()).toMinutes();
            today.setLateHours(lateMinutes / 60.0);
        }
        
        // Set status
        if (hours >= 8.0) {
            today.setStatus("COMPLETED");
        } else if (hours >= 4.0) {
            today.setStatus("HALF_DAY");
        } else {
            today.setStatus("UNDERTIME");
        }
        
        // Update in cache (no need to write to file)
        return true;
    }
}