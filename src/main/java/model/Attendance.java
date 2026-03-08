package model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class Attendance {
    private String employeeId;
    private String lastName;
    private String firstName;
    private LocalDate date;
    private LocalTime timeIn;
    private LocalTime timeOut;
    private double hoursWorked;
    private double overtimeHours;
    private double lateHours;
    private String status;
    private String remarks;

    // ========== GETTERS ==========

    public String getEmployeeId() { return employeeId; }
    public String getLastName() { return lastName; }
    public String getFirstName() { return firstName; }
    public LocalDate getDate() { return date; }
    public LocalTime getTimeIn() { return timeIn; }
    public LocalTime getTimeOut() { return timeOut; }
    public double getHoursWorked() { return hoursWorked; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getLateHours() { return lateHours; }

    public String getStatus() {
        System.out.println("Getting status: " + status);
        return status;
    }

    public String getRemarks() { return remarks; }

    // ========== SETTERS ==========

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setTimeIn(LocalTime timeIn) { this.timeIn = timeIn; }
    public void setTimeOut(LocalTime timeOut) { this.timeOut = timeOut; }
    public void setHoursWorked(double hoursWorked) { this.hoursWorked = hoursWorked; }
    public void setOvertimeHours(double overtimeHours) { this.overtimeHours = overtimeHours; }
    public void setLateHours(double lateHours) { this.lateHours = lateHours; }
    public void setStatus(String status) {
        System.out.println("Setting status to: " + status);
        this.status = status;
    }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ========== BUSINESS METHODS ==========

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFormattedDate() {
        return date != null ? date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")) : "";
    }

    public String getFormattedTimeIn() {
        return timeIn != null ? timeIn.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getFormattedTimeOut() {
        return timeOut != null ? timeOut.format(DateTimeFormatter.ofPattern("hh:mm a")) : "";
    }

    public String getFormattedHoursWorked() {
        return String.format("%.1f hrs", hoursWorked);
    }

    public String getFormattedOvertime() {
        return overtimeHours > 0 ? String.format("%.1f hrs", overtimeHours) : "";
    }

    public String getFormattedLate() {
        return lateHours > 0 ? String.format("%.1f hrs", lateHours) : "";
    }

    public String getStatusBadge() {
        return status;
    }
}