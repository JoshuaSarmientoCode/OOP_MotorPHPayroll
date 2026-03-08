package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LeaveRequest {
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
    
    public String getRequestId() { return requestId; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public String getLeaveType() { return leaveType; }
    public String getReason() { return reason; }
    public LeaveStatus getStatus() { return status; }
    public String getApprovedBy() { return approvedBy; }
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
    
    public boolean isApproved() {
        return status == LeaveStatus.APPROVED;
    }
    
    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }
    
    public boolean isRejected() {
        return status == LeaveStatus.REJECTED;
    }
    
    public boolean isPaidLeave() {
        return leaveType != null && !leaveType.toUpperCase().contains("UNPAID");
    }
    
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