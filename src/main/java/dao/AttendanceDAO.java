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
    private static final DateTimeFormatter TIME_DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");

    public AttendanceDAO(String filePath) {
        super(filePath);
    }

    @Override
    public Attendance fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 5) {
            System.out.println("Insufficient fields in attendance line: " + csvLine);
            return null;
        }

        Attendance attendance = new Attendance();

        try {
            // Basic employee info
            attendance.setEmployeeId(safeGet(data, 0));
            attendance.setLastName(safeGet(data, 1));
            attendance.setFirstName(safeGet(data, 2));

            // Parse date
            String dateStr = safeGet(data, 3);
            if (!dateStr.isEmpty()) {
                try {
                    attendance.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
                } catch (Exception e) {
                    System.out.println("Error parsing date: " + dateStr + " - " + e.getMessage());
                }
            }

            // Parse time in
            String timeInStr = safeGet(data, 4);
            if (!timeInStr.isEmpty()) {
                try {
                    attendance.setTimeIn(LocalTime.parse(timeInStr, TIME_FORMATTER));
                } catch (Exception e) {
                    System.out.println("Error parsing time in: " + timeInStr + " - " + e.getMessage());
                }
            }

            // Parse time out if present
            if (data.length > 5 && !safeGet(data, 5).isEmpty()) {
                String timeOutStr = safeGet(data, 5);
                try {
                    attendance.setTimeOut(LocalTime.parse(timeOutStr, TIME_FORMATTER));

                    // Calculate hours worked
                    if (attendance.getTimeIn() != null) {
                        long minutes = Duration.between(attendance.getTimeIn(), attendance.getTimeOut()).toMinutes();
                        double hours = minutes / 60.0;
                        attendance.setHoursWorked(hours);

                        // Calculate overtime (hours beyond 8)
                        if (hours > 8.0) {
                            attendance.setOvertimeHours(hours - 8.0);
                        } else {
                            attendance.setOvertimeHours(0.0);
                        }

                        // Calculate late (assuming 8:00 AM start)
                        LocalTime standardStart = LocalTime.of(8, 0);
                        if (attendance.getTimeIn().isAfter(standardStart)) {
                            long lateMinutes = Duration.between(standardStart, attendance.getTimeIn()).toMinutes();
                            attendance.setLateHours(lateMinutes / 60.0);
                        } else {
                            attendance.setLateHours(0.0);
                        }

                        // Set status based on hours worked
                        if (hours >= 8.0) {
                            attendance.setStatus("COMPLETED");
                        } else if (hours >= 4.0) {
                            attendance.setStatus("HALF_DAY");
                        } else {
                            attendance.setStatus("UNDERTIME");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing time out: " + timeOutStr + " - " + e.getMessage());
                }
            } else {
                // No time out yet
                if (attendance.getTimeIn() != null) {
                    attendance.setStatus("CLOCKED_IN");
                    System.out.println("Setting status to CLOCKED_IN for employee: " + attendance.getEmployeeId());
                } else {
                    attendance.setStatus("NO_TIME_IN");
                }
            }

        } catch (Exception e) {
            LOGGER.warning("Error parsing attendance: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return attendance;
    }

    @Override
    public String toCSV(Attendance att) {
        return String.join(",",
                att.getEmployeeId() != null ? att.getEmployeeId() : "",
                att.getLastName() != null ? att.getLastName() : "",
                att.getFirstName() != null ? att.getFirstName() : "",
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
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .sorted((a1, a2) -> {
                    if (a1.getDate() == null) return 1;
                    if (a2.getDate() == null) return -1;
                    return a2.getDate().compareTo(a1.getDate());
                })
                .collect(Collectors.toList());
    }

    public List<Attendance> findByDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            return new ArrayList<>();
        }

        return cache.stream()
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                .collect(Collectors.toList());
    }

    public List<Attendance> findByEmployeeAndDateRange(String employeeId, LocalDate start, LocalDate end) {
        if (employeeId == null || employeeId.trim().isEmpty() || start == null || end == null) {
            return new ArrayList<>();
        }

        System.out.println("Finding attendance for employee: " + employeeId +
                " from " + start + " to " + end);

        List<Attendance> results = cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                .collect(Collectors.toList());

        System.out.println("Found " + results.size() + " records");
        return results;
    }

    public Attendance findTodayAttendance(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("findTodayAttendance: Employee ID is null or empty");
            return null;
        }

        LocalDate today = LocalDate.now();
        System.out.println("Looking for today's attendance for employee: " + employeeId + " on date: " + today);

        Optional<Attendance> result = cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null && a.getDate().equals(today))
                .findFirst();

        if (result.isPresent()) {
            Attendance att = result.get();
            System.out.println("Found attendance record for today. Status: " + att.getStatus() +
                    ", TimeIn: " + att.getTimeIn() +
                    ", TimeOut: " + att.getTimeOut());
            return att;
        } else {
            System.out.println("No attendance record found for today");
            return null;
        }
    }

    /**
     * Get statistics for an employee for a specific period
     */
    public Map<String, Object> getStatistics(String employeeId, YearMonth period) {
        Map<String, Object> stats = new HashMap<>();

        if (employeeId == null || employeeId.trim().isEmpty() || period == null) {
            stats.put("totalHours", 0.0);
            stats.put("totalOvertime", 0.0);
            stats.put("totalLate", 0.0);
            stats.put("presentDays", 0);
            stats.put("totalRecords", 0);
            stats.put("averageHoursPerDay", 0.0);
            return stats;
        }

        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();

        List<Attendance> records = findByEmployeeAndDateRange(employeeId, start, end);

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

    public boolean addAttendance(Attendance attendance) {
        if (attendance == null) {
            throw new IllegalArgumentException("Attendance cannot be null");
        }

        if (attendance.getEmployeeId() == null || attendance.getEmployeeId().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required");
        }

        if (attendance.getDate() == null) {
            attendance.setDate(LocalDate.now());
        }

        if (attendance.getTimeIn() == null) {
            throw new IllegalArgumentException("Time in is required");
        }

        // Set status to CLOCKED_IN
        attendance.setStatus("CLOCKED_IN");
        System.out.println("Setting status to CLOCKED_IN for new attendance record");

        // Check if already exists
        Attendance existing = findTodayAttendance(attendance.getEmployeeId());
        if (existing != null) {
            if (existing.getTimeOut() == null) {
                throw new IllegalArgumentException("Employee already clocked in today");
            }
        }

        System.out.println("Adding attendance record for employee: " + attendance.getEmployeeId());
        return add(attendance);
    }

    public boolean updateTimeOut(String employeeId, LocalTime timeOut) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }

        if (timeOut == null) {
            throw new IllegalArgumentException("Time out cannot be null");
        }

        System.out.println("=== UPDATE TIME OUT ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Time Out: " + timeOut.format(TIME_DISPLAY_FORMATTER));

        // Find the attendance record for today
        Attendance today = findTodayAttendance(employeeId);
        if (today == null) {
            System.out.println("No attendance record found for today");
            throw new IllegalArgumentException("No time in record found for today. Please time in first.");
        }

        System.out.println("Found attendance record:");
        System.out.println("  - Current Status: " + today.getStatus());
        System.out.println("  - Time In: " + (today.getTimeIn() != null ? today.getTimeIn().format(TIME_DISPLAY_FORMATTER) : "null"));
        System.out.println("  - Time Out: " + (today.getTimeOut() != null ? today.getTimeOut().format(TIME_DISPLAY_FORMATTER) : "null"));

        if (today.getTimeOut() != null) {
            System.out.println("Already timed out at: " + today.getTimeOut().format(TIME_DISPLAY_FORMATTER));
            throw new IllegalArgumentException("Already timed out today at " + today.getTimeOut().format(TIME_DISPLAY_FORMATTER));
        }

        // Update the time out
        today.setTimeOut(timeOut);

        // Calculate hours worked
        long minutes = Duration.between(today.getTimeIn(), timeOut).toMinutes();
        double hours = minutes / 60.0;
        today.setHoursWorked(hours);

        // Calculate overtime (hours beyond 8)
        if (hours > 8.0) {
            today.setOvertimeHours(hours - 8.0);
        } else {
            today.setOvertimeHours(0.0);
        }

        // Calculate late (assuming 8:00 AM start)
        LocalTime standardStart = LocalTime.of(8, 0);
        if (today.getTimeIn().isAfter(standardStart)) {
            long lateMinutes = Duration.between(standardStart, today.getTimeIn()).toMinutes();
            today.setLateHours(lateMinutes / 60.0);
        } else {
            today.setLateHours(0.0);
        }

        // Set status based on hours worked
        String newStatus;
        if (hours >= 8.0) {
            newStatus = "COMPLETED";
        } else if (hours >= 4.0) {
            newStatus = "HALF_DAY";
        } else {
            newStatus = "UNDERTIME";
        }
        today.setStatus(newStatus);

        System.out.println("Calculated values:");
        System.out.println("  - Hours Worked: " + hours);
        System.out.println("  - Overtime: " + today.getOvertimeHours());
        System.out.println("  - Late: " + today.getLateHours());
        System.out.println("  - New Status: " + newStatus);

        // Update the record in cache and file
        boolean updated = update(today);
        if (updated) {
            System.out.println("Successfully updated time out in database");

            // Verify the update
            Attendance verify = findTodayAttendance(employeeId);
            if (verify != null) {
                System.out.println("Verification - Status: " + verify.getStatus());
                System.out.println("Verification - Time Out: " +
                        (verify.getTimeOut() != null ? verify.getTimeOut().format(TIME_DISPLAY_FORMATTER) : "null"));
            }
        } else {
            System.out.println("Failed to update time out in database");
        }
        return updated;
    }

    /**
     * Get today's attendance status for an employee
     */
    public String getTodayStatus(String employeeId) {
        Attendance today = findTodayAttendance(employeeId);
        if (today == null) {
            return "NOT CLOCKED IN";
        }
        if (today.getTimeOut() == null) {
            return "CLOCKED_IN";
        }
        return today.getStatus();
    }

    /**
     * Check if employee is clocked in today
     */
    public boolean isClockedIn(String employeeId) {
        Attendance today = findTodayAttendance(employeeId);
        return today != null && today.getTimeOut() == null;
    }

    /**
     * Get all attendance records for all employees
     */
    public List<Attendance> getAllAttendance() {
        return new ArrayList<>(cache);
    }

    /**
     * Get attendance count for an employee
     */
    public int getAttendanceCount(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return 0;
        }
        return (int) cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .count();
    }

    /**
     * Format time for display
     */
    private String formatTime(LocalTime time) {
        if (time == null) return "—";
        return time.format(TIME_DISPLAY_FORMATTER);
    }
}