package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private String ticketId;
    private String employeeId;
    private String employeeName;
    private String subject;
    private String description;
    private TicketStatus status;
    private TicketPriority priority;
    private TicketCategory category;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private LocalDateTime resolvedDate;
    private String assignedTo;
    private String resolution;
    private String remarks;

    public enum TicketStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED, REOPENED
    }

    public enum TicketPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum TicketCategory {
        ACCOUNT_ACCESS("Account Access Issues"),
        PASSWORD_RESET("Password Reset"),
        SYSTEM_ERROR("System Error"),
        HARDWARE_ISSUE("Hardware Issue"),
        SOFTWARE_ISSUE("Software Issue"),
        NETWORK_ISSUE("Network Issue"),
        PERMISSION_ISSUE("Permission Issue"),
        DATA_REQUEST("Data Request"),
        REPORT_ISSUE("Report Issue"),
        OTHER("Other");

        private final String displayName;

        TicketCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public Ticket() {
        this.ticketId = generateTicketId();
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
        this.status = TicketStatus.OPEN;
        this.priority = TicketPriority.MEDIUM;
    }

    private String generateTicketId() {
        return "TKT" + System.currentTimeMillis();
    }

    // ========== GETTERS ==========

    public String getTicketId() { return ticketId; }
    public String getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public TicketStatus getStatus() { return status; }
    public TicketPriority getPriority() { return priority; }
    public TicketCategory getCategory() { return category; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    public LocalDateTime getUpdatedDate() { return updatedDate; }
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public String getAssignedTo() { return assignedTo; }
    public String getResolution() { return resolution; }
    public String getRemarks() { return remarks; }

    // ========== SETTERS ==========

    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public void setSubject(String subject) { this.subject = subject; }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(TicketStatus status) {
        this.status = status;
        this.updatedDate = LocalDateTime.now();
        if (status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED) {
            this.resolvedDate = LocalDateTime.now();
        }
    }
    public void setPriority(TicketPriority priority) {
        this.priority = priority;
        this.updatedDate = LocalDateTime.now();
    }
    public void setCategory(TicketCategory category) { this.category = category; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }
    public void setUpdatedDate(LocalDateTime updatedDate) { this.updatedDate = updatedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public void setResolution(String resolution) {
        this.resolution = resolution;
        this.updatedDate = LocalDateTime.now();
    }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    // ========== BUSINESS METHODS ==========

    public String getFormattedCreatedDate() {
        return createdDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
    }

    public String getFormattedUpdatedDate() {
        return updatedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
    }

    public String getFormattedResolvedDate() {
        return resolvedDate != null ? resolvedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a")) : "—";
    }

    public String getStatusBadge() {
        switch (status) {
            case OPEN: return "OPEN";
            case IN_PROGRESS: return "IN PROGRESS";
            case RESOLVED: return "RESOLVED";
            case CLOSED: return "CLOSED";
            case REOPENED: return "REOPENED";
            default: return status.toString();
        }
    }

    public String getPriorityBadge() {
        switch (priority) {
            case LOW: return "LOW";
            case MEDIUM: return "MEDIUM";
            case HIGH: return "HIGH";
            case CRITICAL: return "CRITICAL";
            default: return priority.toString();
        }
    }

    public String getCategoryDisplay() {
        return category != null ? category.getDisplayName() : "Other";
    }

    public boolean isOpen() {
        return status == TicketStatus.OPEN || status == TicketStatus.REOPENED;
    }

    public boolean isInProgress() {
        return status == TicketStatus.IN_PROGRESS;
    }

    public boolean isResolved() {
        return status == TicketStatus.RESOLVED || status == TicketStatus.CLOSED;
    }

    @Override
    public String toString() {
        return String.format("Ticket[id=%s, employee=%s, subject=%s, status=%s, priority=%s]",
                ticketId, employeeId, subject, status, priority);
    }
}