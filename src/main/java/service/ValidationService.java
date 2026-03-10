package service;

import model.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ValidationService {

    private static final Logger LOGGER = Logger.getLogger(ValidationService.class.getName());

    // Employee list for duplicate checks
    private List<Employee> existingEmployees = new ArrayList<>();

    public void setExistingEmployees(List<Employee> employees) {
        this.existingEmployees = employees != null ? employees : new ArrayList<>();
    }

    // ========== PATTERNS ==========

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,6}$");

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[0-9\\-\\+\\(\\)\\s]{7,20}$");

    private static final Pattern SSS_PATTERN =
            Pattern.compile("^(\\d{2}-\\d{7}-\\d{1}|\\d{10})$");

    private static final Pattern TIN_PATTERN =
            Pattern.compile("^(\\d{3}-\\d{3}-\\d{3}-\\d{3}|\\d{12})$");

    private static final Pattern PHILHEALTH_PATTERN =
            Pattern.compile("^(\\d{2}-\\d{9}-\\d{1}|\\d{12})$");

    private static final Pattern PAGIBIG_PATTERN =
            Pattern.compile("^(\\d{4}-\\d{4}-\\d{4}|\\d{12})$");

    private static final Pattern EMPLOYEE_ID_PATTERN =
            Pattern.compile("^\\d{5}$");

    // ========== VALIDATION RESULT ==========

    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;
        private Map<String, String> fieldErrors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
            this.fieldErrors = new HashMap<>();
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public void addFieldError(String field, String error) {
            this.valid = false;
            this.fieldErrors.put(field, error);
            this.errors.add(field + ": " + error);
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
        public Map<String, String> getFieldErrors() { return fieldErrors; }

        public String getErrorMessage() {
            return String.join("\n", errors);
        }

        // Combines another result into this one
        public void merge(ValidationResult other) {
            if (!other.isValid()) {
                this.valid = false;
                this.errors.addAll(other.getErrors());
                this.fieldErrors.putAll(other.getFieldErrors());
            }
        }
    }

    // ========== EMPLOYEE VALIDATION ==========

    // Add — includes duplicate check
    public ValidationResult validateEmployee(Employee emp) {
        ValidationResult result = validateEmployeeFields(emp);
        result.merge(validateNoDuplicates(emp));
        return result;
    }

    // Update — duplicate check excludes own record
    public ValidationResult validateEmployeeForUpdate(Employee emp) {
        ValidationResult result = validateEmployeeFields(emp);
        result.merge(validateNoDuplicatesOnUpdate(emp, emp.getEmployeeId()));
        return result;
    }

    // Shared field checks for add and update
    private ValidationResult validateEmployeeFields(Employee emp) {
        ValidationResult result = new ValidationResult();

        if (emp == null) {
            result.addError("Employee cannot be null");
            return result;
        }

        result.merge(validateEmployeeId(emp.getEmployeeId()));
        result.merge(validateName(emp.getFirstName(), "First name"));
        result.merge(validateName(emp.getLastName(), "Last name"));
        result.merge(validateBirthDate(emp.getBirthDate()));
        result.merge(validateAddress(emp.getAddress()));
        result.merge(validatePhone(emp.getPhoneNumber()));
        result.merge(validatePosition(emp.getPosition()));
        result.merge(validateSalary(emp.getBasicSalary()));
        result.merge(validateAllowances(emp));

        if (emp.getGovernmentIds() != null) {
            result.merge(validateGovernmentIds(emp.getGovernmentIds()));
        }

        if (emp.isProbationary() && emp.getProbationDetails() == null) {
            result.addFieldError("probationDetails",
                    "Probationary employees must have probation details");
        }

        return result;
    }

    // 5-digit ID
    public ValidationResult validateEmployeeId(String employeeId) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(employeeId)) {
            result.addFieldError("employeeId", "Employee ID is required");
        } else if (!EMPLOYEE_ID_PATTERN.matcher(employeeId).matches()) {
            result.addFieldError("employeeId", "Employee ID must be 5 digits");
        }

        return result;
    }

    // Accepts Filipino names: Jr., Sr., III, hyphens, apostrophes
    public ValidationResult validateName(String name, String fieldName) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(name)) {
            result.addFieldError(fieldName.toLowerCase(), fieldName + " is required");
        } else if (name.trim().length() < 2) {
            result.addFieldError(fieldName.toLowerCase(),
                    fieldName + " must be at least 2 characters");
        } else if (name.length() > 50) {
            result.addFieldError(fieldName.toLowerCase(),
                    fieldName + " cannot exceed 50 characters");
        } else if (!name.matches("^[A-Za-z0-9\\s\\-'.,]+$")) {
            result.addFieldError(fieldName.toLowerCase(),
                    fieldName + " contains invalid characters");
        }

        return result;
    }

    // Min 18, max 100 years old
    public ValidationResult validateBirthDate(LocalDate birthDate) {
        ValidationResult result = new ValidationResult();

        if (birthDate == null) {
            result.addFieldError("birthDate", "Birth date is required");
            return result;
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();

        if (birthDate.isAfter(LocalDate.now())) {
            result.addFieldError("birthDate", "Birth date cannot be in the future");
        } else if (age < 18) {
            result.addFieldError("birthDate", "Employee must be at least 18 years old");
        } else if (age > 100) {
            result.addFieldError("birthDate", "Invalid birth date");
        }

        return result;
    }

    // 5-200 characters
    public ValidationResult validateAddress(String address) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(address)) {
            result.addFieldError("address", "Address is required");
        } else if (address.length() < 5) {
            result.addFieldError("address", "Address is too short");
        } else if (address.length() > 200) {
            result.addFieldError("address", "Address is too long (max 200 characters)");
        }

        return result;
    }

    // 7-20 digits, allows dashes and spaces
    public ValidationResult validatePhone(String phone) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(phone)) {
            result.addFieldError("phoneNumber", "Phone number is required");
        } else if (!PHONE_PATTERN.matcher(phone).matches()) {
            result.addFieldError("phoneNumber", "Invalid phone number format");
        }

        return result;
    }

    public ValidationResult validateEmail(String email) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(email)) {
            result.addFieldError("email", "Email is required");
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            result.addFieldError("email", "Invalid email format");
        }

        return result;
    }

    public ValidationResult validatePosition(String position) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(position)) {
            result.addFieldError("position", "Position is required");
        }

        return result;
    }

    // P10,000 - P1,000,000
    public ValidationResult validateSalary(double salary) {
        ValidationResult result = new ValidationResult();

        if (salary < 10000) {
            result.addFieldError("basicSalary", "Basic salary must be at least P10,000");
        } else if (salary > 1000000) {
            result.addFieldError("basicSalary", "Basic salary cannot exceed P1,000,000");
        }

        return result;
    }

    // No negatives; total must not exceed basic salary
    public ValidationResult validateAllowances(Employee emp) {
        ValidationResult result = new ValidationResult();

        if (emp.getRiceSubsidy() < 0)
            result.addFieldError("riceSubsidy", "Rice subsidy cannot be negative");
        if (emp.getPhoneAllowance() < 0)
            result.addFieldError("phoneAllowance", "Phone allowance cannot be negative");
        if (emp.getClothingAllowance() < 0)
            result.addFieldError("clothingAllowance", "Clothing allowance cannot be negative");
        if (emp.getTotalAllowances() > emp.getBasicSalary())
            result.addFieldError("allowances", "Total allowances cannot exceed basic salary");

        return result;
    }

    // Accepts with or without dashes
    public ValidationResult validateGovernmentIds(GovernmentIds gov) {
        ValidationResult result = new ValidationResult();

        if (!isNullOrEmpty(gov.getSssNumber())) {
            String clean = gov.getSssNumber().replace("-", "");
            if (!SSS_PATTERN.matcher(gov.getSssNumber()).matches() && !clean.matches("\\d{10}"))
                result.addFieldError("sss", "Invalid SSS format (XX-XXXXXXX-X or 10 digits)");
        }

        if (!isNullOrEmpty(gov.getTinNumber())) {
            String clean = gov.getTinNumber().replace("-", "");
            if (!TIN_PATTERN.matcher(gov.getTinNumber()).matches() && !clean.matches("\\d{12}"))
                result.addFieldError("tin", "Invalid TIN format (XXX-XXX-XXX-XXX or 12 digits)");
        }

        if (!isNullOrEmpty(gov.getPhilHealthNumber())) {
            String clean = gov.getPhilHealthNumber().replace("-", "");
            if (!PHILHEALTH_PATTERN.matcher(gov.getPhilHealthNumber()).matches() && !clean.matches("\\d{12}"))
                result.addFieldError("philHealth", "Invalid PhilHealth format (XX-XXXXXXXXX-X or 12 digits)");
        }

        if (!isNullOrEmpty(gov.getPagIbigNumber())) {
            String clean = gov.getPagIbigNumber().replace("-", "");
            if (!PAGIBIG_PATTERN.matcher(gov.getPagIbigNumber()).matches() && !clean.matches("\\d{12}"))
                result.addFieldError("pagIbig", "Invalid Pag-IBIG format (XXXX-XXXX-XXXX or 12 digits)");
        }

        return result;
    }

    // ========== DUPLICATE VALIDATION ==========

    // New employee — checks all unique fields
    public ValidationResult validateNoDuplicates(Employee emp) {
        return checkDuplicates(emp, null);
    }

    // Existing employee — skips own record
    public ValidationResult validateNoDuplicatesOnUpdate(Employee emp, String currentId) {
        return checkDuplicates(emp, currentId);
    }

    // excludeId = null on add, own ID on update
    private ValidationResult checkDuplicates(Employee emp, String excludeId) {
        ValidationResult result = new ValidationResult();
        if (emp == null || existingEmployees.isEmpty()) return result;

        for (Employee existing : existingEmployees) {
            if (excludeId != null && excludeId.equals(existing.getEmployeeId())) continue;

            if (!isNullOrEmpty(emp.getEmployeeId()) &&
                    emp.getEmployeeId().equals(existing.getEmployeeId())) {
                result.addFieldError("employeeId",
                        "Employee ID " + emp.getEmployeeId() + " is already taken");
            }

            if (!isNullOrEmpty(emp.getFirstName()) && !isNullOrEmpty(emp.getLastName()) &&
                    emp.getLastName().equalsIgnoreCase(existing.getLastName()) &&
                    emp.getFirstName().equalsIgnoreCase(existing.getFirstName())) {
                result.addFieldError("name",
                        emp.getFirstName() + " " + emp.getLastName()
                                + " already exists (ID: " + existing.getEmployeeId() + ")");
            }

            if (!isNullOrEmpty(emp.getPhoneNumber()) &&
                    emp.getPhoneNumber().replaceAll("\\s", "").equals(
                            existing.getPhoneNumber() != null
                                    ? existing.getPhoneNumber().replaceAll("\\s", "") : "")) {
                result.addFieldError("phoneNumber",
                        "Phone number already registered to another employee");
            }

            if (emp.getGovernmentIds() != null && existing.getGovernmentIds() != null) {
                GovernmentIds n = emp.getGovernmentIds();
                GovernmentIds o = existing.getGovernmentIds();

                if (!isNullOrEmpty(n.getSssNumber()) && n.getSssNumber().equals(o.getSssNumber()))
                    result.addFieldError("sss", "SSS number already registered to another employee");

                if (!isNullOrEmpty(n.getTinNumber()) && n.getTinNumber().equals(o.getTinNumber()))
                    result.addFieldError("tin", "TIN number already registered to another employee");

                if (!isNullOrEmpty(n.getPhilHealthNumber()) && n.getPhilHealthNumber().equals(o.getPhilHealthNumber()))
                    result.addFieldError("philHealth", "PhilHealth number already registered to another employee");

                if (!isNullOrEmpty(n.getPagIbigNumber()) && n.getPagIbigNumber().equals(o.getPagIbigNumber()))
                    result.addFieldError("pagIbig", "Pag-IBIG number already registered to another employee");
            }
        }

        return result;
    }

    // ========== LEAVE VALIDATION ==========

    // Checks dates, duration, type, reason, and leave credits
    public ValidationResult validateLeaveRequest(LeaveRequest request, Employee employee) {
        ValidationResult result = new ValidationResult();

        if (request == null) {
            result.addError("Leave request cannot be null");
            return result;
        }

        if (request.getStartDate() == null) {
            result.addFieldError("startDate", "Start date is required");
        } else if (request.getStartDate().isBefore(LocalDate.now())) {
            result.addFieldError("startDate", "Start date cannot be in the past");
        }

        if (request.getEndDate() == null) {
            result.addFieldError("endDate", "End date is required");
        } else if (request.getStartDate() != null &&
                request.getEndDate().isBefore(request.getStartDate())) {
            result.addFieldError("endDate", "End date must be after start date");
        }

        if (request.getStartDate() != null && request.getEndDate() != null &&
                request.getNumberOfDays() > 30) {
            result.addFieldError("dates", "Leave request cannot exceed 30 days");
        }

        if (isNullOrEmpty(request.getLeaveType()))
            result.addFieldError("leaveType", "Leave type is required");

        if (isNullOrEmpty(request.getReason())) {
            result.addFieldError("reason", "Reason is required");
        } else if (request.getReason().length() < 10) {
            result.addFieldError("reason", "Please provide a more detailed reason");
        }

        // Leave credits check for regular employees
        if (employee instanceof RegularEmployee && request.isPaidLeave()) {
            RegularEmployee reg = (RegularEmployee) employee;
            int days = request.getNumberOfDays();
            if (!reg.hasSufficientLeave(days)) {
                result.addFieldError("leaveCredits",
                        String.format("Insufficient leave credits. Available: %d, Requested: %d",
                                reg.getLeaveCredits(), days));
            }
        }

        return result;
    }

    // ========== ATTENDANCE VALIDATION ==========

    public ValidationResult validateTimeIn(Employee employee, LocalDate date) {
        ValidationResult result = new ValidationResult();

        if (employee == null) {
            result.addError("Employee not found");
            return result;
        }

        boolean hasTimedIn = employee.getAttendanceRecords().stream()
                .anyMatch(a -> a.getDate().equals(date) && a.getTimeIn() != null);

        if (hasTimedIn) result.addError("Already timed in for today");

        return result;
    }

    public ValidationResult validateTimeOut(Employee employee, LocalDate date) {
        ValidationResult result = new ValidationResult();

        if (employee == null) {
            result.addError("Employee not found");
            return result;
        }

        Attendance today = employee.getAttendanceRecords().stream()
                .filter(a -> a.getDate().equals(date))
                .findFirst().orElse(null);

        if (today == null) {
            result.addError("No time in record for today");
        } else if (today.getTimeOut() != null) {
            result.addError("Already timed out for today");
        }

        return result;
    }

    // ========== LOGIN VALIDATION ==========

    public ValidationResult validateLogin(String username, String password) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(username))
            result.addFieldError("username", "Username is required");

        if (isNullOrEmpty(password)) {
            result.addFieldError("password", "Password is required");
        } else if (password.length() < 4) {
            result.addFieldError("password", "Password must be at least 4 characters");
        }

        return result;
    }

    // ========== PAYROLL VALIDATION ==========

    // Cannot generate for future periods
    public ValidationResult validatePayrollGeneration(Employee employee, YearMonth period) {
        ValidationResult result = new ValidationResult();

        if (employee == null) result.addError("Employee not found");

        if (period == null) {
            result.addError("Payroll period is required");
        } else if (period.isAfter(YearMonth.now())) {
            result.addError("Cannot generate payroll for future periods");
        }

        return result;
    }

    // ========== HELPERS ==========

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Start before end; max 1 year range
    public ValidationResult validateDateRange(LocalDate start, LocalDate end) {
        ValidationResult result = new ValidationResult();

        if (start == null) result.addFieldError("startDate", "Start date is required");
        if (end == null)   result.addFieldError("endDate", "End date is required");

        if (start != null && end != null) {
            if (end.isBefore(start))
                result.addFieldError("dateRange", "End date must be after start date");
            if (ChronoUnit.DAYS.between(start, end) > 365)
                result.addFieldError("dateRange", "Date range cannot exceed 1 year");
        }

        return result;
    }

    public ValidationResult validateNumeric(String value, String fieldName, double min, double max) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(value)) {
            result.addFieldError(fieldName.toLowerCase(), fieldName + " is required");
            return result;
        }

        try {
            double num = Double.parseDouble(value.replace(",", "").trim());
            if (num < min)
                result.addFieldError(fieldName.toLowerCase(), fieldName + " must be at least " + min);
            else if (num > max)
                result.addFieldError(fieldName.toLowerCase(), fieldName + " cannot exceed " + max);
        } catch (NumberFormatException e) {
            result.addFieldError(fieldName.toLowerCase(), fieldName + " must be a valid number");
        }

        return result;
    }

    // Min 5 chars, must contain a letter and a digit
    public ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();

        if (isNullOrEmpty(password)) {
            result.addFieldError("password", "Password is required");
        } else if (password.length() < 5) {
            result.addFieldError("password", "Password must be at least 5 characters");
        } else if (password.length() > 50) {
            result.addFieldError("password", "Password cannot exceed 50 characters");
        } else if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            result.addFieldError("password", "Password must contain at least one letter and one number");
        }

        return result;
    }
}