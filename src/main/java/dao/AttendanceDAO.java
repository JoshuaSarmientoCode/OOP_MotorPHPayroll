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
        if (data.length < 5) return null;

        Attendance attendance = new Attendance();

        try {
            attendance.setEmployeeId(safeGet(data, 0));
            attendance.setLastName(safeGet(data, 1));
            attendance.setFirstName(safeGet(data, 2));

            String dateStr = safeGet(data, 3);
            if (!dateStr.isEmpty()) {
                try {
                    attendance.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
                } catch (Exception e) {
                    LOGGER.warning("Error parsing date: " + dateStr);
                }
            }

            String timeInStr = safeGet(data, 4);
            if (!timeInStr.isEmpty()) {
                try {
                    attendance.setTimeIn(LocalTime.parse(timeInStr, TIME_FORMATTER));
                } catch (Exception e) {
                    LOGGER.warning("Error parsing time in: " + timeInStr);
                }
            }

            if (data.length > 5 && !safeGet(data, 5).isEmpty()) {
                String timeOutStr = safeGet(data, 5);
                try {
                    attendance.setTimeOut(LocalTime.parse(timeOutStr, TIME_FORMATTER));
                    if (attendance.getTimeIn() != null) {
                        long minutes = Duration.between(attendance.getTimeIn(), attendance.getTimeOut()).toMinutes();
                        double hours = minutes / 60.0;
                        attendance.setHoursWorked(hours);
                        attendance.setOvertimeHours(hours > 8.0 ? hours - 8.0 : 0.0);

                        LocalTime standardStart = LocalTime.of(8, 0);
                        if (attendance.getTimeIn().isAfter(standardStart)) {
                            long lateMinutes = Duration.between(standardStart, attendance.getTimeIn()).toMinutes();
                            attendance.setLateHours(lateMinutes / 60.0);
                        } else {
                            attendance.setLateHours(0.0);
                        }

                        if (hours >= 8.0) attendance.setStatus("COMPLETED");
                        else if (hours >= 4.0) attendance.setStatus("HALF_DAY");
                        else attendance.setStatus("UNDERTIME");
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error parsing time out: " + e.getMessage());
                }
            } else {
                attendance.setStatus(attendance.getTimeIn() != null ? "CLOCKED_IN" : "NO_TIME_IN");
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
                att.getEmployeeId() != null ? att.getEmployeeId() : "",
                att.getLastName() != null ? att.getLastName() : "",
                att.getFirstName() != null ? att.getFirstName() : "",
                att.getDate() != null ? att.getDate().format(DATE_FORMATTER) : "",
                att.getTimeIn() != null ? att.getTimeIn().format(TIME_FORMATTER) : "",
                att.getTimeOut() != null ? att.getTimeOut().format(TIME_FORMATTER) : ""
        );
    }

    @Override
    protected String[] getHeaders() { return HEADERS; }

    @Override
    protected String getId(Attendance item) {
        return item.getEmployeeId() + "_" +
                (item.getDate() != null ? item.getDate().toString() : "");
    }

    public List<Attendance> findByEmployeeId(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) return new ArrayList<>();
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
        if (start == null || end == null) return new ArrayList<>();
        return cache.stream()
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .sorted((a1, a2) -> a2.getDate().compareTo(a1.getDate()))
                .collect(Collectors.toList());
    }

    public List<Attendance> findByEmployeeAndDateRange(String employeeId, LocalDate start, LocalDate end) {
        if (employeeId == null || employeeId.trim().isEmpty() || start == null || end == null)
            return new ArrayList<>();
        return cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null)
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .sorted((a1, a2) -> a1.getDate().compareTo(a2.getDate()))
                .collect(Collectors.toList());
    }

    public Attendance findTodayAttendance(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) return null;
        LocalDate today = LocalDate.now();
        return cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .filter(a -> a.getDate() != null && a.getDate().equals(today))
                .findFirst().orElse(null);
    }

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

        List<Attendance> records = findByEmployeeAndDateRange(
                employeeId, period.atDay(1), period.atEndOfMonth());

        double totalHours = 0, totalOvertime = 0, totalLate = 0;
        int presentDays = 0;

        for (Attendance a : records) {
            if (a.getTimeOut() != null) {
                totalHours += a.getHoursWorked();
                totalOvertime += a.getOvertimeHours();
                totalLate += a.getLateHours();
                presentDays++;
            }
        }

        stats.put("totalRecords", records.size());
        stats.put("presentDays", presentDays);
        stats.put("totalHours", totalHours);
        stats.put("totalOvertime", totalOvertime);
        stats.put("totalLate", totalLate);
        stats.put("averageHoursPerDay", presentDays > 0 ? totalHours / presentDays : 0);
        return stats;
    }

    public boolean addAttendance(Attendance attendance) {
        if (attendance == null) throw new IllegalArgumentException("Attendance cannot be null");
        if (attendance.getEmployeeId() == null || attendance.getEmployeeId().trim().isEmpty())
            throw new IllegalArgumentException("Employee ID is required");
        if (attendance.getDate() == null) attendance.setDate(LocalDate.now());
        if (attendance.getTimeIn() == null) throw new IllegalArgumentException("Time in is required");

        attendance.setStatus("CLOCKED_IN");

        Attendance existing = findTodayAttendance(attendance.getEmployeeId());
        if (existing != null && existing.getTimeOut() == null)
            throw new IllegalArgumentException("Employee already clocked in today");

        return add(attendance);
    }

    public boolean updateTimeOut(String employeeId, LocalTime timeOut) {
        if (employeeId == null || employeeId.trim().isEmpty())
            throw new IllegalArgumentException("Employee ID cannot be empty");
        if (timeOut == null)
            throw new IllegalArgumentException("Time out cannot be null");

        Attendance today = findTodayAttendance(employeeId);
        if (today == null)
            throw new IllegalArgumentException("No time in record found for today. Please time in first.");
        if (today.getTimeOut() != null)
            throw new IllegalArgumentException("Already timed out today at " + today.getTimeOut().format(TIME_DISPLAY_FORMATTER));

        today.setTimeOut(timeOut);
        long minutes = Duration.between(today.getTimeIn(), timeOut).toMinutes();
        double hours = minutes / 60.0;
        today.setHoursWorked(hours);
        today.setOvertimeHours(hours > 8.0 ? hours - 8.0 : 0.0);

        LocalTime standardStart = LocalTime.of(8, 0);
        if (today.getTimeIn().isAfter(standardStart)) {
            long lateMinutes = Duration.between(standardStart, today.getTimeIn()).toMinutes();
            today.setLateHours(lateMinutes / 60.0);
        } else {
            today.setLateHours(0.0);
        }

        if (hours >= 8.0) today.setStatus("COMPLETED");
        else if (hours >= 4.0) today.setStatus("HALF_DAY");
        else today.setStatus("UNDERTIME");

        return update(today);
    }

    public String getTodayStatus(String employeeId) {
        Attendance today = findTodayAttendance(employeeId);
        if (today == null) return "NOT CLOCKED IN";
        if (today.getTimeOut() == null) return "CLOCKED_IN";
        return today.getStatus();
    }

    public boolean isClockedIn(String employeeId) {
        Attendance today = findTodayAttendance(employeeId);
        return today != null && today.getTimeOut() == null;
    }

    public List<Attendance> getAllAttendance() { return new ArrayList<>(cache); }

    public int getAttendanceCount(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) return 0;
        return (int) cache.stream()
                .filter(a -> a.getEmployeeId() != null && a.getEmployeeId().equals(employeeId))
                .count();
    }
}