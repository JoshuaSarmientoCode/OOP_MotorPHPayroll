package model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import service.ValidationService;
import service.Validatable;

public abstract class Employee implements EmployeeInterface, Validatable {
    // ========== PRIVATE FIELDS ==========
    protected String employeeId;
    protected String firstName;
    protected String lastName;
    protected LocalDate birthDate;
    protected String email;
    protected String phoneNumber;
    protected String address;
    protected double basicSalary;
    protected double riceSubsidy;
    protected double phoneAllowance;
    protected double clothingAllowance;
    protected String position;
    protected LocalDate hireDate;
    protected EmploymentStatus status;
    protected String immediateSupervisor;
    protected GovernmentIds governmentIds;
    protected List<Attendance> attendanceRecords;
    protected List<LeaveRequest> leaveRequests;
    protected ProbationDetails probationDetails;

    // ========== CONSTRUCTORS ==========

    public Employee() {
        this.attendanceRecords = new ArrayList<>();
        this.leaveRequests = new ArrayList<>();
        this.governmentIds = new GovernmentIds();
        this.status = EmploymentStatus.REGULAR;
        this.riceSubsidy = 1500.0;
        this.phoneAllowance = 500.0;
        this.clothingAllowance = 500.0;
    }

    public Employee(String employeeId, String firstName, String lastName, String position) {
        this();
        setEmployeeId(employeeId);
        setFirstName(firstName);
        setLastName(lastName);
        setPosition(position);
    }

    // ========== GETTERS ==========

    public String getEmployeeId() { return employeeId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
    public double getBasicSalary() { return basicSalary; }
    public double getRiceSubsidy() { return riceSubsidy; }
    public double getPhoneAllowance() { return phoneAllowance; }
    public double getClothingAllowance() { return clothingAllowance; }
    public String getPosition() { return position; }
    public LocalDate getHireDate() { return hireDate; }
    public EmploymentStatus getStatus() { return status; }
    public String getImmediateSupervisor() { return immediateSupervisor; }
    public GovernmentIds getGovernmentIds() { return governmentIds; }
    public List<Attendance> getAttendanceRecords() { return new ArrayList<>(attendanceRecords); }
    public List<LeaveRequest> getLeaveRequests() { return new ArrayList<>(leaveRequests); }
    public ProbationDetails getProbationDetails() { return probationDetails; }

    // ========== SETTERS ==========

    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public void setEmail(String email) { this.email = email; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setAddress(String address) { this.address = address; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    public void setPosition(String position) { this.position = position; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
    public void setStatus(EmploymentStatus status) { this.status = status; }
    public void setStatus(String statusString) {
        if (statusString == null || statusString.trim().isEmpty()) {
            this.status = EmploymentStatus.REGULAR;
        } else {
            try {
                this.status = EmploymentStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.status = EmploymentStatus.REGULAR;
            }
        }
    }
    public void setImmediateSupervisor(String immediateSupervisor) { this.immediateSupervisor = immediateSupervisor; }
    public void setGovernmentIds(GovernmentIds governmentIds) { this.governmentIds = governmentIds; }
    public void setAttendanceRecords(List<Attendance> attendanceRecords) { this.attendanceRecords = attendanceRecords; }
    public void setLeaveRequests(List<LeaveRequest> leaveRequests) { this.leaveRequests = leaveRequests; }
    public void setProbationDetails(ProbationDetails probationDetails) { this.probationDetails = probationDetails; }

    // ========== BUSINESS METHODS ==========

    @Override
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getFormattedName() {
        return lastName + ", " + firstName;
    }

    @Override
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    @Override
    public double getTotalAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }

    @Override
    public double getGrossSalary() {
        return basicSalary + getTotalAllowances();
    }

    @Override
    public double getMonthlySalary() {
        return getGrossSalary();
    }

    @Override
    public double getHourlyRate() {
        return basicSalary / 168; // 168 working hours per month (21 days * 8 hours)
    }

    @Override
    public double getDailyRate() {
        return basicSalary / 22; // 22 working days per month
    }

    @Override
    public boolean isProbationary() {
        return status == EmploymentStatus.PROBATIONARY;
    }

    @Override
    public boolean isRegular() {
        return status == EmploymentStatus.REGULAR;
    }

    @Override
    public String getDepartment() {
        if (position == null) return "OPERATIONS";
        if (position.contains("HR")) return "HUMAN RESOURCES";
        if (position.contains("IT")) return "INFORMATION TECHNOLOGY";
        if (position.contains("Account") || position.contains("Finance")) return "FINANCE";
        if (position.contains("Sales")) return "SALES & MARKETING";
        if (position.contains("Chief") || position.contains("CEO") ||
                position.contains("CFO") || position.contains("COO")) return "EXECUTIVE";
        return "OPERATIONS";
    }

    @Override
    public void addAttendance(Attendance attendance) {
        if (attendance != null) {
            this.attendanceRecords.add(attendance);
        }
    }

    @Override
    public void addLeaveRequest(LeaveRequest leaveRequest) {
        if (leaveRequest != null) {
            this.leaveRequests.add(leaveRequest);
        }
    }

    @Override
    public List<Attendance> getAttendanceForPeriod(LocalDate start, LocalDate end) {
        return attendanceRecords.stream()
                .filter(a -> !a.getDate().isBefore(start) && !a.getDate().isAfter(end))
                .toList();
    }

    @Override
    public double getTotalHoursWorked(LocalDate start, LocalDate end) {
        return getAttendanceForPeriod(start, end).stream()
                .filter(a -> a.getTimeOut() != null)
                .mapToDouble(Attendance::getHoursWorked)
                .sum();
    }

    @Override
    public List<LeaveRequest> getLeaveRequestsForPeriod(LocalDate start, LocalDate end) {
        return leaveRequests.stream()
                .filter(l -> !l.getEndDate().isBefore(start) && !l.getStartDate().isAfter(end))
                .filter(l -> l.getStatus() == LeaveRequest.LeaveStatus.APPROVED)
                .toList();
    }

    @Override
    public long getUnpaidLeaveDays(LocalDate start, LocalDate end) {
        return getLeaveRequestsForPeriod(start, end).stream()
                .filter(l -> l.getLeaveType().toUpperCase().contains("UNPAID"))
                .mapToLong(LeaveRequest::getNumberOfDays)
                .sum();
    }

    // ========== Validatable IMPLEMENTATION ==========

    @Override
    public ValidationService.ValidationResult validate() {
        ValidationService.ValidationResult result = new ValidationService.ValidationResult();

        // Employee ID
        if (employeeId == null || employeeId.trim().isEmpty()) {
            result.addFieldError("employeeId", "Employee ID is required");
        } else if (!employeeId.matches("^\\d{5}$")) {
            result.addFieldError("employeeId", "Employee ID must be 5 digits");
        }

        // Name
        if (firstName == null || firstName.trim().isEmpty()) {
            result.addFieldError("firstName", "First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            result.addFieldError("lastName", "Last name is required");
        }

        // Birth date
        if (birthDate == null) {
            result.addFieldError("birthDate", "Birth date is required");
        } else {
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) result.addFieldError("birthDate", "Employee must be at least 18 years old");
            if (age > 100) result.addFieldError("birthDate", "Invalid birth date");
        }

        // Salary
        if (basicSalary < 10000) {
            result.addFieldError("basicSalary", "Basic salary must be at least ₱10,000");
        }

        // Position
        if (position == null || position.trim().isEmpty()) {
            result.addFieldError("position", "Position is required");
        }

        return result;
    }

    @Override
    public boolean isValid() {
        return validate().isValid();
    }

    // ========== ABSTRACT METHODS FOR SUBCLASSES ==========

    @Override
    public abstract String getRoleName();
    @Override
    public abstract boolean canAccess(String feature);
    @Override
    public abstract Employee.DashboardType getDashboardType();

    // ========== ENUMS ==========

    public enum DashboardType {
        ADMIN, HR, FINANCE, IT, EMPLOYEE
    }

    // ========== OVERRIDDEN OBJECT METHODS ==========

    @Override
    public String toString() {
        return String.format("Employee[id=%s, name=%s, position=%s, status=%s]",
                employeeId, getFullName(), position, status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return employeeId.equals(employee.employeeId);
    }

    @Override
    public int hashCode() {
        return employeeId.hashCode();
    }
}