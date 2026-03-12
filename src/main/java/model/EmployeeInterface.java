package model;

import model.Employee.Employee;

import java.time.LocalDate;
import java.util.List;


public interface EmployeeInterface {

    // ========== ROLE & ACCESS ==========

    String getRoleName();

    boolean canAccess(String feature);

    Employee.DashboardType getDashboardType();

    String getDepartment();

    // ========== IDENTITY & COMPUTED FIELDS ==========

    String getFullName();

    String getFormattedName();

    int getAge();

    // ========== SALARY & ALLOWANCES ==========

    double getTotalAllowances();

    double getGrossSalary();

    double getMonthlySalary();

    double getHourlyRate();

    double getDailyRate();

    // ========== STATUS CHECKS ==========

    boolean isProbationary();

    boolean isRegular();

    // ========== ATTENDANCE ==========

    void addAttendance(Attendance attendance);

    List<Attendance> getAttendanceForPeriod(LocalDate start, LocalDate end);

    double getTotalHoursWorked(LocalDate start, LocalDate end);

    // ========== LEAVE ==========

    void addLeaveRequest(LeaveRequest leaveRequest);

    List<LeaveRequest> getLeaveRequestsForPeriod(LocalDate start, LocalDate end);

    long getUnpaidLeaveDays(LocalDate start, LocalDate end);
}