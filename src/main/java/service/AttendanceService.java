package service;

import dao.AttendanceDAO;
import model.Attendance;
import model.Employee;
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
        return attendanceDAO.findByEmployeeId(employeeId);
    }
    
    public List<Attendance> getAttendanceForPeriod(String employeeId, LocalDate start, LocalDate end) {
        return attendanceDAO.findByEmployeeAndDateRange(employeeId, start, end);
    }
    
    public List<Attendance> getAttendanceForMonth(String employeeId, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return getAttendanceForPeriod(employeeId, start, end);
    }
    
    public Attendance getTodayAttendance(String employeeId) {
        return attendanceDAO.findTodayAttendance(employeeId);
    }
    
    public String getAttendanceStatus(String employeeId) {
        Attendance today = getTodayAttendance(employeeId);
        if (today == null) return "NOT CLOCKED IN";
        if (today.getTimeOut() == null) {
            return "CLOCKED IN at " + formatTime(today.getTimeIn());
        }
        return String.format("COMPLETED - %.1f hours", today.getHoursWorked());
    }
    
    public Map<String, String> getCurrentShiftInfo(String employeeId) {
        Attendance today = getTodayAttendance(employeeId);
        Map<String, String> info = new HashMap<>();
        
        if (today == null) {
            info.put("status", "NOT CLOCKED IN");
            info.put("timeIn", "—");
            info.put("elapsed", "—");
            return info;
        }
        
        info.put("status", today.getStatus());
        info.put("timeIn", formatTime(today.getTimeIn()));
        
        if (today.getTimeOut() == null) {
            long minutes = Duration.between(today.getTimeIn(), LocalTime.now()).toMinutes();
            double hours = minutes / 60.0;
            info.put("elapsed", String.format("%.1f hours", hours));
        } else {
            info.put("timeOut", formatTime(today.getTimeOut()));
            info.put("elapsed", String.format("%.1f hours", today.getHoursWorked()));
        }
        
        return info;
    }
    
    public double getTotalHoursWorked(String employeeId, LocalDate start, LocalDate end) {
        return getAttendanceForPeriod(employeeId, start, end).stream()
            .filter(a -> a.getTimeOut() != null)
            .mapToDouble(Attendance::getHoursWorked)
            .sum();
    }
    
    public Map<String, Object> getMonthlySummary(String employeeId, YearMonth month) {
        return attendanceDAO.getStatistics(employeeId, month);
    }
    
    public boolean timeIn(String employeeId) {
        Employee emp = employeeService.getEmployeeById(employeeId);
        if (emp == null) {
            throw new IllegalArgumentException("Employee not found");
        }
        
        Attendance existing = attendanceDAO.findTodayAttendance(employeeId);
        if (existing != null) {
            throw new IllegalArgumentException("Already timed in today");
        }
        
        Attendance attendance = new Attendance();
        attendance.setEmployeeId(employeeId);
        attendance.setLastName(emp.getLastName());
        attendance.setFirstName(emp.getFirstName());
        attendance.setDate(LocalDate.now());
        attendance.setTimeIn(LocalTime.now());
        attendance.setStatus("CLOCKED_IN");
        
        attendanceDAO.add(attendance);
        LOGGER.info("Time in recorded for: " + employeeId);
        return true;
    }
    
    public boolean timeOut(String employeeId) {
        Attendance today = attendanceDAO.findTodayAttendance(employeeId);
        if (today == null) {
            throw new IllegalArgumentException("No time in record for today");
        }
        if (today.getTimeOut() != null) {
            throw new IllegalArgumentException("Already timed out today");
        }
        
        LocalTime timeOut = LocalTime.now();
        today.setTimeOut(timeOut);
        
        long minutes = Duration.between(today.getTimeIn(), timeOut).toMinutes();
        double hours = minutes / 60.0;
        today.setHoursWorked(hours);
        
        if (hours > 8.0) {
            today.setOvertimeHours(hours - 8.0);
        }
        
        LocalTime standardStart = LocalTime.of(8, 0);
        if (today.getTimeIn().isAfter(standardStart)) {
            long lateMinutes = Duration.between(standardStart, today.getTimeIn()).toMinutes();
            today.setLateHours(lateMinutes / 60.0);
        }
        
        if (hours >= 8.0) {
            today.setStatus("COMPLETED");
        } else if (hours >= 4.0) {
            today.setStatus("HALF_DAY");
        } else {
            today.setStatus("UNDERTIME");
        }
        
        attendanceDAO.update(today);
        LOGGER.info("Time out recorded for: " + employeeId);
        return true;
    }
    
    private String formatTime(LocalTime time) {
        if (time == null) return "—";
        return time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"));
    }
}