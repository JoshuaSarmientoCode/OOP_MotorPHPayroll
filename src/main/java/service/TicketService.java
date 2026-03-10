package service;

import dao.TicketDAO;
import dao.SystemLogDAO;
import model.Ticket;
import model.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TicketService {

    private final TicketDAO ticketDAO;
    private final SystemLogDAO logDAO;

    public TicketService(TicketDAO ticketDAO, SystemLogDAO logDAO) {
        this.ticketDAO = ticketDAO;
        this.logDAO = logDAO;
    }

    public List<Ticket> getAllTickets() {
        return ticketDAO.findAll();
    }

    public List<Ticket> getTicketsByEmployee(String employeeId) {
        return ticketDAO.findByEmployeeId(employeeId);
    }

    public List<Ticket> getOpenTickets() {
        return ticketDAO.findOpenTickets();
    }

    public Ticket getTicketById(String ticketId) {
        return ticketDAO.findByTicketId(ticketId);
    }

    public boolean createTicket(Ticket ticket, User user) {
        if (ticket == null) return false;

        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setUpdatedDate(LocalDateTime.now());
        ticket.setStatus(Ticket.TicketStatus.OPEN);

        boolean success = ticketDAO.addTicket(ticket);

        if (success && logDAO != null) {
            logDAO.logInfo(
                    "TicketService",
                    user.getEmployeeId(),
                    user.getFullName(),
                    "CREATE_TICKET",
                    "Created ticket: " + ticket.getTicketId() + " - " + ticket.getSubject()
            );
        }

        return success;
    }

    public boolean updateTicket(Ticket ticket, User user) {
        if (ticket == null) return false;

        ticket.setUpdatedDate(LocalDateTime.now());
        boolean success = ticketDAO.updateTicket(ticket);

        if (success && logDAO != null) {
            logDAO.logAudit(
                    "TicketService",
                    user.getEmployeeId(),
                    user.getFullName(),
                    "UPDATE_TICKET",
                    "Updated ticket: " + ticket.getTicketId()
            );
        }

        return success;
    }

    public boolean updateTicketStatus(String ticketId, Ticket.TicketStatus status,
                                      String resolution, String remarks, User user) {
        Ticket ticket = ticketDAO.findByTicketId(ticketId);
        if (ticket == null) return false;

        Ticket.TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(status);

        if (resolution != null && !resolution.isEmpty()) {
            ticket.setResolution(resolution);
        }

        if (remarks != null && !remarks.isEmpty()) {
            ticket.setRemarks(remarks);
        }

        ticket.setUpdatedDate(LocalDateTime.now());

        if (status == Ticket.TicketStatus.RESOLVED || status == Ticket.TicketStatus.CLOSED) {
            ticket.setResolvedDate(LocalDateTime.now());
        }

        boolean success = ticketDAO.update(ticket);

        if (success && logDAO != null) {
            logDAO.logAudit(
                    "TicketService",
                    user.getEmployeeId(),
                    user.getFullName(),
                    "UPDATE_TICKET_STATUS",
                    "Changed ticket " + ticketId + " status from " + oldStatus + " to " + status
            );
        }

        return success;
    }

    public boolean assignTicket(String ticketId, String assignedTo, User user) {
        boolean success = ticketDAO.assignTicket(ticketId, assignedTo);

        if (success && logDAO != null) {
            logDAO.logAudit(
                    "TicketService",
                    user.getEmployeeId(),
                    user.getFullName(),
                    "ASSIGN_TICKET",
                    "Assigned ticket " + ticketId + " to " + assignedTo
            );
        }

        return success;
    }

    public Map<String, Object> getTicketStatistics() {
        return ticketDAO.getStatistics();
    }

    public boolean changePassword(String employeeId, String oldPassword, String newPassword, User user) {
        // This would integrate with UserService
        // For now, just log the action
        if (logDAO != null) {
            logDAO.logAudit(
                    "TicketService",
                    user.getEmployeeId(),
                    user.getFullName(),
                    "CHANGE_PASSWORD",
                    "Password changed for user: " + employeeId
            );
        }
        return true;
    }
}