package dao;

import model.LeaveRequest;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.stream.Collectors;

public class LeaveRequestDAO extends BaseDAO<LeaveRequest> {

    private static final String[] HEADERS = {
            "Request ID", "Employee ID", "Employee Name", "Department", "Position",
            "Start Date", "End Date", "Leave Type", "Reason", "Status",
            "Approved By", "Request Date", "Approval Date", "Remarks"
    };

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public LeaveRequestDAO(String filePath) {
        super(filePath);
    }

    @Override
    public LeaveRequest fromCSV(String csvLine) {
        String[] data = parseCSVLine(csvLine);
        if (data.length < 9) return null;

        LeaveRequest request = new LeaveRequest();

        try {
            request.setRequestId(safeGet(data, 0));
            request.setEmployeeId(safeGet(data, 1));
            request.setEmployeeName(safeGet(data, 2));

            if (data.length > 3) request.setDepartment(safeGet(data, 3));
            if (data.length > 4) request.setPosition(safeGet(data, 4));

            request.setStartDate(LocalDate.parse(safeGet(data, 5), DATE_FORMATTER));
            request.setEndDate(LocalDate.parse(safeGet(data, 6), DATE_FORMATTER));

            request.setLeaveType(safeGet(data, 7));
            request.setReason(safeGet(data, 8).replace("\"", ""));

            if (data.length > 9 && !safeGet(data, 9).isEmpty()) {
                request.setStatus(LeaveRequest.LeaveStatus.valueOf(safeGet(data, 9).toUpperCase()));
            }

            if (data.length > 10 && !safeGet(data, 10).isEmpty()) {
                request.setApprovedBy(safeGet(data, 10));
            }

            request.setRequestDate(LocalDate.parse(safeGet(data, 11), DATE_FORMATTER));

            if (data.length > 12 && !safeGet(data, 12).isEmpty()) {
                request.setApprovalDate(LocalDate.parse(safeGet(data, 12), DATE_FORMATTER));
            }

            if (data.length > 13 && !safeGet(data, 13).isEmpty()) {
                request.setRemarks(safeGet(data, 13).replace("\"", ""));
            }

        } catch (Exception e) {
            LOGGER.warning("Error parsing leave request: " + e.getMessage());
            return null;
        }

        return request;
    }

    @Override
    public String toCSV(LeaveRequest request) {
        // Return a single line without any newline characters
        return String.join(",",
                request.getRequestId(),
                request.getEmployeeId(),
                request.getEmployeeName(),
                request.getDepartment() != null ? request.getDepartment() : "",
                request.getPosition() != null ? request.getPosition() : "",
                request.getStartDate().format(DATE_FORMATTER),
                request.getEndDate().format(DATE_FORMATTER),
                request.getLeaveType(),
                "\"" + request.getReason() + "\"",
                request.getStatus().toString(),
                request.getApprovedBy() != null ? request.getApprovedBy() : "",
                request.getRequestDate().format(DATE_FORMATTER),
                request.getApprovalDate() != null ? request.getApprovalDate().format(DATE_FORMATTER) : "",
                request.getRemarks() != null ? "\"" + request.getRemarks() + "\"" : ""
        );
    }

    @Override
    protected String[] getHeaders() {
        return HEADERS;
    }

    @Override
    protected String getId(LeaveRequest item) {
        return item.getRequestId();
    }

    private String safeGet(String[] data, int index) {
        if (index < 0 || index >= data.length) return "";
        return data[index] != null ? data[index].trim() : "";
    }

    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    // ========== BUSINESS METHODS ==========

    public List<LeaveRequest> findByEmployeeId(String employeeId) {
        return cache.stream()
                .filter(l -> l.getEmployeeId().equals(employeeId))
                .sorted((l1, l2) -> l2.getRequestDate().compareTo(l1.getRequestDate()))
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status) {
        return cache.stream()
                .filter(l -> l.getStatus() == status)
                .sorted((l1, l2) -> l2.getRequestDate().compareTo(l1.getRequestDate()))
                .collect(Collectors.toList());
    }

    public List<LeaveRequest> findPendingRequests() {
        return findByStatus(LeaveRequest.LeaveStatus.PENDING);
    }

    public List<LeaveRequest> findAll() {
        return cache.stream()
                .sorted((l1, l2) -> l2.getRequestDate().compareTo(l1.getRequestDate()))
                .collect(Collectors.toList());
    }

    public LeaveRequest findByRequestId(String requestId) {
        return findById(requestId);
    }

    public boolean addRequest(LeaveRequest request) {
        return add(request);
    }

    public boolean updateStatus(String requestId, LeaveRequest.LeaveStatus status,
                                String approvedBy, String remarks) {
        LeaveRequest request = findByRequestId(requestId);
        if (request == null) return false;

        request.setStatus(status);
        request.setApprovedBy(approvedBy);
        request.setApprovalDate(LocalDate.now());
        if (remarks != null && !remarks.isEmpty()) {
            request.setRemarks(remarks);
        }

        return update(request);
    }

    public String getNextRequestId() {
        int maxId = cache.stream()
                .map(l -> {
                    try {
                        String id = l.getRequestId();
                        if (id.startsWith("LV")) {
                            return Integer.parseInt(id.substring(2));
                        }
                        return Integer.parseInt(id);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(1000);
        return String.format("LV%04d", maxId + 1);
    }
}