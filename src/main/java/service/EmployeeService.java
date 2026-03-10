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
        // Feed current employee list into validator for duplicate checking
        validator.setExistingEmployees(employeeDAO.getAllEmployees());

        ValidationService.ValidationResult result = validator.validateEmployee(employee);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }
        return employeeDAO.addEmployee(employee);
    }

    public boolean updateEmployee(Employee employee) {
        // Feed current employee list into validator for duplicate checking (excludes self)
        validator.setExistingEmployees(employeeDAO.getAllEmployees());

        ValidationService.ValidationResult result = validator.validateEmployeeForUpdate(employee);
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

    // ========== LEAVE REQUEST METHODS ==========

    public List<LeaveRequest> getEmployeeLeaves(String employeeId) {
        return leaveDAO.findByEmployeeId(employeeId);
    }

    public boolean submitLeaveRequest(LeaveRequest request) {
        ValidationService.ValidationResult result = validator.validateLeaveRequest(request, null);
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getErrorMessage());
        }

        Employee emp = getEmployeeById(request.getEmployeeId());
        if (emp != null) {
            request.setDepartment(emp.getDepartment());
            request.setPosition(emp.getPosition());
        }

        request.setRequestId(leaveDAO.getNextRequestId());
        request.setRequestDate(LocalDate.now());
        request.setStatus(LeaveRequest.LeaveStatus.PENDING);

        return leaveDAO.addRequest(request);
    }

    public List<LeaveRequest> getAllLeaveRequests() {
        return leaveDAO.findAll();
    }

    public List<LeaveRequest> getPendingLeaveRequests() {
        return leaveDAO.findPendingRequests();
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
}