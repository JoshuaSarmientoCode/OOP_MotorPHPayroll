package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import service.ValidationService;

public class LeaveRequest implements Approvable, Validatable {
    private String requestId;
    private String employeeId;
    private String employeeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String leaveType;
    private String reason;
    private LeaveStatus status;
    private String approvedBy;
    private LocalDate requestDate;
    private LocalDate approvalDate;
    private String remarks;
    private String department;
    private String position;

    public enum LeaveStatus {
        PENDING, APPROVED, REJECTED, CANCELLED
    }

    public LeaveRequest() {
        this.requestDate = LocalDate.now();
        this.status = LeaveStatus.PENDING;
    }

    // ========== GETTERS ==========

    @Override
    public String getRequestId() { return requestId; }
    @Override
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getLeaveType() { return leaveType; }
    public String getReason() { return reason; }
    public LeaveStatus getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; }
    @Override
    public LocalDate getRequestDate() { return requestDate; }
    public LocalDate getApprovalDate() { return approvalDate; }
    public String getRemarks() { return remarks; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }

    // ========== SETTERS ==========

    public void setRequestId(String requestId) { this.requestId = requestId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public void setLeaveType(String leaveType) { this.leaveType = leaveType; }
    public void setReason(String reason) { this.reason = reason; }
    public void setStatus(LeaveStatus status) { this.status = status; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public void setRequestDate(LocalDate requestDate) { this.requestDate = requestDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public void setDepartment(String department) { this.department = department; }
    public void setPosition(String position) { this.position = position; }

    // ========== BUSINESS METHODS ==========

    public int getNumberOfDays() {
        if (startDate != null && endDate != null) {
            return (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;
        }
        return 0;
    }

    @Override
    public boolean isApproved() {
        return status == LeaveStatus.APPROVED;
    }

    @Override
    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }

    @Override
    public boolean isRejected() {
        return status == LeaveStatus.REJECTED;
    }

    public boolean isPaidLeave() {
        return leaveType != null && !leaveType.toUpperCase().contains("UNPAID");
    }

    // ========== Validatable IMPLEMENTATION ==========

    @Override
    public ValidationService.ValidationResult validate() {
        ValidationService.ValidationResult result = new ValidationService.ValidationResult();

        // Employee
        if (employeeId == null || employeeId.trim().isEmpty()) {
            result.addFieldError("employeeId", "Employee ID is required");
        }

        // Dates
        if (startDate == null) {
            result.addFieldError("startDate", "Start date is required");
        } else if (startDate.isBefore(LocalDate.now())) {
            result.addFieldError("startDate", "Start date cannot be in the past");
        }

        if (endDate == null) {
            result.addFieldError("endDate", "End date is required");
        } else if (startDate != null && endDate.isBefore(startDate)) {
            result.addFieldError("endDate", "End date must be after start date");
        }

        if (startDate != null && endDate != null && getNumberOfDays() > 30) {
            result.addFieldError("dates", "Leave request cannot exceed 30 days");
        }

        // Leave type
        if (leaveType == null || leaveType.trim().isEmpty()) {
            result.addFieldError("leaveType", "Leave type is required");
        }

        // Reason
        if (reason == null || reason.trim().isEmpty()) {
            result.addFieldError("reason", "Reason is required");
        } else if (reason.trim().length() < 10) {
            result.addFieldError("reason", "Please provide a more detailed reason");
        }

        return result;
    }

    @Override
    public boolean isValid() {
        return validate().isValid();
    }

    // ========== Approvable IMPLEMENTATION ==========

    @Override
    public void approve(String approvedBy) {
        this.status = LeaveStatus.APPROVED;
        this.approvedBy = approvedBy;
        this.approvalDate = LocalDate.now();
    }

    @Override
    public void reject(String rejectedBy, String reason) {
        this.status = LeaveStatus.REJECTED;
        this.approvedBy = rejectedBy;
        this.remarks = reason;
        this.approvalDate = LocalDate.now();
    }

    @Override
    public String getStatusDisplay() { return status != null ? status.toString() : "PENDING"; }

    public String getFormattedPeriodLong() {
        if (startDate == null || endDate == null) return "";
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
        return startDate.format(fmt) + " to " + endDate.format(fmt);
    }

    public String getFormattedRequestDate() {
        return requestDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    public String getFormattedApprovalDate() {
        return approvalDate != null ?
                approvalDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) : "";
    }
}