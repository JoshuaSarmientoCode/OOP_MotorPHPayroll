package service;

import dao.AttendanceDAO;
import model.Attendance;
import model.Employee.Employee;
import java.time.*;
import java.util.*;
import java.util.logging.*;

public class AttendanceService {

    private static final Logger LOGGER = Logger.getLogger(AttendanceService.class.getName());

    private final AttendanceDAO attendanceDAO;
    private final EmployeeService employeeService;

    public AttendanceService(AttendanceDAO attendanceDAO, EmployeeService employeeService) {
        this.attendanceDAO = attendanceDAO;
        this.employeeService = employeeService;
    }

    public List<Attendance> getEmployeeAttendance(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return attendanceDAO.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAttendanceForPeriod(String employeeId, LocalDate start, LocalDate end) {
        if (employeeId == null || employeeId.trim().isEmpty() || start == null || end == null) {
            return new ArrayList<>();
        }
        return attendanceDAO.findByEmployeeAndDateRange(employeeId, start, end);
    }

    public List<Attendance> getAttendanceForMonth(String employeeId, YearMonth month) {
        if (employeeId == null || employeeId.trim().isEmpty() || month == null) {
            return new ArrayList<>();
        }
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return getAttendanceForPeriod(employeeId, start, end);
    }

    public Attendance getTodayAttendance(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return null;
        }
        return attendanceDAO.findTodayAttendance(employeeId);
    }

    public String getAttendanceStatus(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            return "INVALID EMPLOYEE ID";
        }
        Attendance today = getTodayAttendance(employeeId);
        if (today == null) return "NOT CLOCKED IN";
        if (today.getTimeOut() == null) {
            return "CLOCKED IN at " + formatTime(today.getTimeIn());
        }
        return String.format("COMPLETED - %.1f hours", today.getHoursWorked());
    }

    public Map<String, String> getCurrentShiftInfo(String employeeId) {
        Map<String, String> info = new HashMap<>();

        if (employeeId == null || employeeId.trim().isEmpty()) {
            LOGGER.fine("getCurrentShiftInfo: Employee ID is null or empty");
            info.put("status", "INVALID EMPLOYEE ID");
            info.put("timeIn", "—");
            info.put("elapsed", "—");
            return info;
        }

        LOGGER.fine("Getting shift info for employee: " + employeeId);

        try {
            Attendance today = getTodayAttendance(employeeId);

            if (today == null) {
                LOGGER.fine("No attendance found for today");
                info.put("status", "NOT CLOCKED IN");
                info.put("timeIn", "—");
                info.put("elapsed", "—");
                return info;
            }

            String status = today.getStatus();
            LOGGER.fine("Today's attendance status from DAO: " + status);

            info.put("status", status != null ? status : "UNKNOWN");
            info.put("timeIn", formatTime(today.getTimeIn()));

            if (today.getTimeOut() == null) {
                if (today.getTimeIn() != null) {
                    long minutes = Duration.between(today.getTimeIn(), LocalTime.now()).toMinutes();
                    double hours = minutes / 60.0;
                    info.put("elapsed", String.format("%.1f hours", hours));
                    LOGGER.fine("Elapsed time: " + hours + " hours");
                } else {
                    info.put("elapsed", "—");
                }
            } else {
                info.put("timeOut", formatTime(today.getTimeOut()));
                info.put("elapsed", String.format("%.1f hours", today.getHoursWorked()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            info.put("status", "ERROR");
            info.put("timeIn", "—");
            info.put("elapsed", "—");
        }

        return info;
    }

    public double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end) {
        if (employeeId == null || employeeId.trim().isEmpty() || start == null || end == null) {
            return 0.0;
        }
        return getAttendanceForPeriod(employeeId, start, end).stream()
                .filter(a -> a.getTimeOut() != null)
                .mapToDouble(Attendance::getHoursWorked)
                .sum();
    }

    /**
     * Get monthly summary for an employee
     */
    public Map<String, Object> getMonthlySummary(String employeeId, YearMonth month) {
        if (employeeId == null || employeeId.trim().isEmpty() || month == null) {
            Map<String, Object> empty = new HashMap<>();
            empty.put("totalHours", 0.0);
            empty.put("presentDays", 0);
            empty.put("totalOvertime", 0.0);
            empty.put("totalLate", 0.0);
            return empty;
        }
        return attendanceDAO.getStatistics(employeeId, month);
    }

    public boolean timeIn(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }

        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }

        Attendance existing = attendanceDAO.findTodayAttendance(employeeId);
        if (existing != null) {
            if (existing.getTimeOut() == null) {
                throw new IllegalArgumentException("Already timed in today. Please time out first.");
            } else {
                throw new IllegalArgumentException("Already completed shift for today.");
            }
        }

        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setLastName(emp.getLastName());
        attendance.setFirstName(emp.getFirstName());
        attendance.setDate(LocalDate.now());
        attendance.setTimeIn(LocalTime.now());
        attendance.setStatus("CLOCKED_IN");

        boolean success = attendanceDAO.addAttendance(attendance);
        if (success) {
            LOGGER.info("Time in recorded for: " + employeeId + " at " + LocalTime.now());
        }
        return success;
    }

    public boolean timeOut(String employeeId) {
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID cannot be empty");
        }

        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) {
            throw new IllegalArgumentException("Employee not found with ID: " + employeeId);
        }

        Attendance today = attendanceDAO.findTodayAttendance(employeeId);
        if (today == null) {
            throw new IllegalArgumentException("No time in record found for today. Please time in first.");
        }

        if (today.getTimeOut() != null) {
            throw new IllegalArgumentException("Already timed out today.");
        }

        boolean success = attendanceDAO.updateTimeOut(employeeId, LocalTime.now());
        if (success) {
            LOGGER.info("Time out recorded for: " + employeeId + " at " + LocalTime.now());
        }
        return success;
    }

    private String formatTime(LocalTime time) {
        if (time == null) return "—";
        return time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }
}