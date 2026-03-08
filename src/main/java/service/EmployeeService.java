package service;

import dao.*;
import model.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class EmployeeService {
    
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;
    private final LeaveRequestDAO leaveDAO;
    private final ValidationService validator;
    
    public EmployeeService(EmployeeDAO employeeDAO, AttendanceDAO attendanceDAO, 
                          LeaveRequestDAO leaveDAO, ValidationService validator) {
        this.employeeDAO = employeeDAO;
        this.attendanceDAO = attendanceDAO;
        this.leaveDAO = leaveDAO;
        this.validator = validator;
    }
    
    // ========== BASIC EMPLOYEE OPERATIONS ==========
    
    public List<Employee> getAllEmployees() {
        return employeeDAO.getAllEmployees();
    }
    
    public Employee getEmployeeById(String employeeId) {
        return employeeDAO.findByEmployeeId(employeeId);
    }
    
    public boolean addEmployee(Employee employee) {
        ValidationService.ValidationResult result = validator.validateEmployee(employee);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
        return employeeDAO.addEmployee(employee);
    }
    
    public boolean updateEmployee(Employee employee) {
        ValidationService.ValidationResult result = validator.validateEmployee(employee);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
        return employeeDAO.updateEmployee(employee);
    }
    
    public boolean deleteEmployee(String employeeId) {
        return employeeDAO.deleteEmployee(employeeId);
    }
    
    public String getNextEmployeeId() {
        return employeeDAO.getNextEmployeeId();
    }
    
    // ========== EMPLOYEE QUERIES ==========
    
    public List<Employee> searchEmployees(String keyword) {
        return employeeDAO.searchEmployees(keyword);
    }
    
    // ========== TYPE-SPECIFIC EMPLOYEE RETRIEVAL ==========
    
    public List<HREmployee> getHREmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof HREmployee)
                .map(e -> (HREmployee) e)
                .collect(Collectors.toList());
    }
    
    public List<FinanceEmployee> getFinanceEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof FinanceEmployee)
                .map(e -> (FinanceEmployee) e)
                .collect(Collectors.toList());
    }
    
    public List<ITEmployee> getITEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof ITEmployee)
                .map(e -> (ITEmployee) e)
                .collect(Collectors.toList());
    }
    
    public List<AdminEmployee> getAdminEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof AdminEmployee)
                .map(e -> (AdminEmployee) e)
                .collect(Collectors.toList());
    }
    
    public List<RegularEmployee> getRegularEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof RegularEmployee && !(e instanceof ProbationaryEmployee))
                .map(e -> (RegularEmployee) e)
                .collect(Collectors.toList());
    }
    
    public List<ProbationaryEmployee> getProbationaryEmployees() {
        return employeeDAO.getAllEmployees().stream()
                .filter(e -> e instanceof ProbationaryEmployee)
                .map(e -> (ProbationaryEmployee) e)
                .collect(Collectors.toList());
    }
    
    // ========== ATTENDANCE METHODS ==========
    
    public List<Attendance> getEmployeeAttendance(String employeeId) {
        return attendanceDAO.findByEmployeeId(employeeId);
    }
    
    public List<Attendance> getEmployeeAttendanceByPeriod(String employeeId, YearMonth period) {
        LocalDate start = period.atDay(1);
        LocalDate end = period.atEndOfMonth();
        return attendanceDAO.findByEmployeeAndDateRange(employeeId, start, end);
    }
    
    public boolean timeIn(String employeeId) {
        Employee emp = getEmployeeById(employeeId);
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
        attendance.setHoursWorked(0);
        
        return attendanceDAO.add(attendance);
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
        
        // Calculate hours worked
        long minutes = Duration.between(today.getTimeIn(), timeOut).toMinutes();
        today.setHoursWorked(minutes / 60.0);
        
        // Update in DAO (need to implement update method or use cache)
        attendanceDAO.update(today);
        return true;
    }
    
    // ========== LEAVE REQUEST METHODS ==========
    
    public boolean submitLeaveRequest(LeaveRequest request) {
        ValidationService.ValidationResult result = validator.validateLeaveRequest(request, null);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
        
        Employee emp = getEmployeeById(request.getEmployeeId());
        if (emp != null) {
            request.setDepartment(getDepartment(emp.getPosition()));
            request.setPosition(emp.getPosition());
        }
        
        request.setRequestId(leaveDAO.getNextRequestId());
        request.setRequestDate(LocalDate.now());
        request.setStatus(LeaveRequest.LeaveStatus.PENDING);
        
        return leaveDAO.addRequest(request);
    }
    
    public List<LeaveRequest> getEmployeeLeaves(String employeeId) {
        return leaveDAO.findByEmployeeId(employeeId);
    }
    
    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveDAO.findAll();
    }
    
    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveDAO.findPendingRequests();
    }
    
    public List<LeaveRequest> getLeaveRequestsByStatus(LeaveRequest.LeaveStatus status) {
        return leaveDAO.findByStatus(status);
    }
    
    public boolean approveLeaveRequest(String requestId, String approvedBy, String remarks) {
        return leaveDAO.updateStatus(requestId, LeaveRequest.LeaveStatus.APPROVED, approvedBy, remarks);
    }
    
    public boolean rejectLeaveRequest(String requestId, String approvedBy, String remarks) {
        return leaveDAO.updateStatus(requestId, LeaveRequest.LeaveStatus.REJECTED, approvedBy, remarks);
    }
    
    public LeaveRequest getLeaveRequest(String requestId) {
        return leaveDAO.findByRequestId(requestId);
    }
    
    public Map<String, Long> getLeaveRequestStats() {
        List<LeaveRequest> allRequests = getAllLeaveRequests();
        
        long pending = allRequests.stream()
                .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.PENDING)
                .count();
        long approved = allRequests.stream()
                .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
                .count();
        long rejected = allRequests.stream()
                .filter(r -> r.getStatus() == LeaveRequest.LeaveStatus.REJECTED)
                .count();
        
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", pending);
        stats.put("approved", approved);
        stats.put("rejected", rejected);
        stats.put("total", (long) allRequests.size());
        
        return stats;
    }
    
    // ========== HELPER METHODS ==========
    
    private String getDepartment(String position) {
        if (position == null) return "OPERATIONS";
        if (position.contains("HR")) return "HUMAN RESOURCES";
        if (position.contains("IT")) return "INFORMATION TECHNOLOGY";
        if (position.contains("Account") || position.contains("Finance")) return "FINANCE";
        if (position.contains("Sales")) return "SALES & MARKETING";
        if (position.contains("Chief") || position.contains("CEO") || 
            position.contains("CFO") || position.contains("COO")) return "EXECUTIVE";
        return "OPERATIONS";
    }
}