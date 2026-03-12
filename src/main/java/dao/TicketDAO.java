package dao;

import model.Ticket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TicketDAO extends BaseDAO<Ticket> {
    private static final Logger LOGGER = Logger.getLogger(TicketDAO.class.getName());


    private static final String[] HEADERS = {
            "Ticket ID", "Employee ID", "Employee Name", "Subject", "Description",
            "Category", "Priority", "Status", "Created Date", "Updated Date",
            "Resolved Date", "Assigned To", "Resolution", "Remarks"
    };

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TicketDAO(String filePath) {
        super(filePath);
    }

    @Override
    public Ticket fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 8) return null;

        Ticket ticket = new Ticket();

        try {
            ticket.setTicketId(safeGet(data, 0));
            ticket.setEmployeeId(safeGet(data, 1));
            ticket.setEmployeeName(safeGet(data, 2));
            ticket.setSubject(safeGet(data, 3));
            ticket.setDescription(safeGet(data, 4).replace("\"", ""));

            if (data.length > 5 && !safeGet(data, 5).isEmpty()) {
                ticket.setCategory(Ticket.TicketCategory.valueOf(safeGet(data, 5)));
            }

            if (data.length > 6 && !safeGet(data, 6).isEmpty()) {
                ticket.setPriority(Ticket.TicketPriority.valueOf(safeGet(data, 6)));
            }

            if (data.length > 7 && !safeGet(data, 7).isEmpty()) {
                ticket.setStatus(Ticket.TicketStatus.valueOf(safeGet(data, 7)));
            }

            if (data.length > 8 && !safeGet(data, 8).isEmpty()) {
                ticket.setCreatedDate(LocalDateTime.parse(safeGet(data, 8), DATETIME_FORMATTER));
            }

            if (data.length > 9 && !safeGet(data, 9).isEmpty()) {
                ticket.setUpdatedDate(LocalDateTime.parse(safeGet(data, 9), DATETIME_FORMATTER));
            }

            if (data.length > 10 && !safeGet(data, 10).isEmpty()) {
                ticket.setResolvedDate(LocalDateTime.parse(safeGet(data, 10), DATETIME_FORMATTER));
            }

            if (data.length > 11) ticket.setAssignedTo(safeGet(data, 11));
            if (data.length > 12) ticket.setResolution(safeGet(data, 12).replace("\"", ""));
            if (data.length > 13) ticket.setRemarks(safeGet(data, 13).replace("\"", ""));

        } catch (Exception e) {
            LOGGER.warning("Error parsing ticket: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return ticket;
    }

    @Override
    public String toCSV(Ticket ticket) {
        List<String> fields = new ArrayList<>();

        fields.add(ticket.getTicketId());
        fields.add(ticket.getEmployeeId());
        fields.add(ticket.getEmployeeName());
        fields.add(ticket.getSubject());
        fields.add("\"" + ticket.getDescription() + "\"");
        fields.add(ticket.getCategory() != null ? ticket.getCategory().toString() : "");
        fields.add(ticket.getPriority() != null ? ticket.getPriority().toString() : "MEDIUM");
        fields.add(ticket.getStatus() != null ? ticket.getStatus().toString() : "OPEN");
        fields.add(ticket.getCreatedDate() != null ? ticket.getCreatedDate().format(DATETIME_FORMATTER) : "");
        fields.add(ticket.getUpdatedDate() != null ? ticket.getUpdatedDate().format(DATETIME_FORMATTER) : "");
        fields.add(ticket.getResolvedDate() != null ? ticket.getResolvedDate().format(DATETIME_FORMATTER) : "");
        fields.add(ticket.getAssignedTo() != null ? ticket.getAssignedTo() : "");
        fields.add(ticket.getResolution() != null ? "\"" + ticket.getResolution() + "\"" : "");
        fields.add(ticket.getRemarks() != null ? "\"" + ticket.getRemarks() + "\"" : "");

        return String.join(",", fields);
    }

    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }

    @Override
    protected String getId(Ticket item) {
        return item.getTicketId();
    }

    // ========== BUSINESS METHODS ==========

    public List<Ticket> findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(t -> t.getEmployeeId().equals(employeeId))
                .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
                .collect(Collectors.toList());
    }

    public List<Ticket> findByStatus(Ticket.TicketStatus status) {
        return cache.stream()
                .filter(t -> t.getStatus() == status)
                .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
                .collect(Collectors.toList());
    }

    public List<Ticket> findByPriority(Ticket.TicketPriority priority) {
        return cache.stream()
                .filter(t -> t.getPriority() == priority)
                .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
                .collect(Collectors.toList());
    }

    public List<Ticket> findOpenTickets() {
        return cache.stream()
                .filter(t -> t.getStatus() == Ticket.TicketStatus.OPEN ||
                        t.getStatus() == Ticket.TicketStatus.REOPENED)
                .sorted((t1, t2) -> {
                    // Sort by priority first, then by date
                    if (t1.getPriority() != t2.getPriority()) {
                        return t2.getPriority().compareTo(t1.getPriority());
                    }
                    return t2.getCreatedDate().compareTo(t1.getCreatedDate());
                })
                .collect(Collectors.toList());
    }

    public List<Ticket> findAll() {
        return cache.stream()
                .sorted((t1, t2) -> t2.getCreatedDate().compareTo(t1.getCreatedDate()))
                .collect(Collectors.toList());
    }

    public Ticket findByTicketId(String ticketId) {
        return findById(ticketId);
    }

    public boolean addTicket(Ticket ticket) {
        return add(ticket);
    }

    public boolean updateTicket(Ticket ticket) {
        ticket.setUpdatedDate(LocalDateTime.now());
        return update(ticket);
    }

    public boolean updateStatus(String ticketId, Ticket.TicketStatus status, String resolution, String remarks) {
        Ticket ticket = findByTicketId(ticketId);
        if (ticket == null) return false;

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

        return update(ticket);
    }

    public boolean assignTicket(String ticketId, String assignedTo) {
        Ticket ticket = findByTicketId(ticketId);
        if (ticket == null) return false;

        ticket.setAssignedTo(assignedTo);
        if (ticket.getStatus() == Ticket.TicketStatus.OPEN) {
            ticket.setStatus(Ticket.TicketStatus.IN_PROGRESS);
        }
        ticket.setUpdatedDate(LocalDateTime.now());

        return update(ticket);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long openCount = cache.stream()
                .filter(t -> t.getStatus() == Ticket.TicketStatus.OPEN)
                .count();

        long inProgressCount = cache.stream()
                .filter(t -> t.getStatus() == Ticket.TicketStatus.IN_PROGRESS)
                .count();

        long resolvedCount = cache.stream()
                .filter(t -> t.getStatus() == Ticket.TicketStatus.RESOLVED)
                .count();

        long closedCount = cache.stream()
                .filter(t -> t.getStatus() == Ticket.TicketStatus.CLOSED)
                .count();

        long criticalCount = cache.stream()
                .filter(t -> t.getPriority() == Ticket.TicketPriority.CRITICAL &&
                        t.getStatus() != Ticket.TicketStatus.RESOLVED &&
                        t.getStatus() != Ticket.TicketStatus.CLOSED)
                .count();

        stats.put("total", cache.size());
        stats.put("open", openCount);
        stats.put("inProgress", inProgressCount);
        stats.put("resolved", resolvedCount);
        stats.put("closed", closedCount);
        stats.put("critical", criticalCount);

        return stats;
    }
}