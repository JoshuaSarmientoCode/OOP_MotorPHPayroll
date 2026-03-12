package dao;

import model.*;
import model.Employee.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.util.logging.*;

public class CSVHandler {
    
    private static final Logger LOGGER = Logger.getLogger(CSVHandler.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    
    // Employee CSV column indices
    private static final int EMP_ID = 0;
    private static final int EMP_LAST_NAME = 1;
    private static final int EMP_FIRST_NAME = 2;
    private static final int EMP_BIRTHDAY = 3;
    private static final int EMP_ADDRESS = 4;
    private static final int EMP_PHONE = 5;
    private static final int EMP_SSS = 6;
    private static final int EMP_PHILHEALTH = 7;
    private static final int EMP_TIN = 8;
    private static final int EMP_PAGIBIG = 9;
    private static final int EMP_STATUS = 10;
    private static final int EMP_POSITION = 11;
    private static final int EMP_SUPERVISOR = 12;
    private static final int EMP_BASIC_SALARY = 13;
    private static final int EMP_RICE_SUBSIDY = 14;
    private static final int EMP_PHONE_ALLOWANCE = 15;
    private static final int EMP_CLOTHING_ALLOWANCE = 16;
    
    // Attendance CSV column indices
    private static final int ATT_EMPLOYEE_ID = 0;
    private static final int ATT_LAST_NAME = 1;
    private static final int ATT_FIRST_NAME = 2;
    private static final int ATT_DATE = 3;
    private static final int ATT_TIME_IN = 4;
    private static final int ATT_TIME_OUT = 5;
    
    /**
     * Reads all employees from the CSV file
     */
    public List<Employee> readEmployees(String filePath) {
        List<Employee> employees = new ArrayList<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOGGER.warning("Employee file not found: " + filePath);
            return employees;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Employee emp = parseEmployee(line);
                    if (emp != null) {
                        employees.add(emp);
                    }
                } catch (Exception e) {
                    LOGGER.warning("Error parsing employee at line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            LOGGER.info("Successfully loaded " + employees.size() + " employees from " + filePath);
            
        } catch (IOException e) {
            LOGGER.severe("Error reading employee file: " + e.getMessage());
        }
        
        return employees;
    }
    
    /**
     * Parses a single employee from CSV line
     */
    private Employee parseEmployee(String csvLine) {
        List<String> fields = parseCSVLine(csvLine);
        if (fields.size() <= EMP_CLOTHING_ALLOWANCE) {
            LOGGER.warning("Insufficient fields: expected at least " + (EMP_CLOTHING_ALLOWANCE + 1) + " but got " + fields.size());
            return null;
        }
        
        String position = fields.get(EMP_POSITION).trim();
        String status = fields.get(EMP_STATUS).trim();
        
        // Create appropriate employee type based on position AND status
        Employee emp = createEmployeeByPositionAndStatus(position, status);
        
        GovernmentIds govIds = new GovernmentIds();
        
        try {
            emp.setEmployeeId(fields.get(EMP_ID).trim());
            emp.setLastName(fields.get(EMP_LAST_NAME).trim());
            emp.setFirstName(fields.get(EMP_FIRST_NAME).trim());
            
            String birthdayStr = fields.get(EMP_BIRTHDAY).trim();
            if (!birthdayStr.isEmpty() && !birthdayStr.equals("N/A")) {
                try {
                    emp.setBirthDate(LocalDate.parse(birthdayStr, DATE_FORMATTER));
                } catch (Exception e) {
                    LOGGER.warning("Could not parse birthday: " + birthdayStr);
                }
            }
            
            String address = fields.get(EMP_ADDRESS).trim();
            if (address.startsWith("\"") && address.endsWith("\"")) {
                address = address.substring(1, address.length() - 1);
            }
            emp.setAddress(address);
            
            String phone = fields.get(EMP_PHONE).trim().replace("-", "");
            emp.setPhoneNumber(phone);
            
            govIds.setSssNumber(fields.get(EMP_SSS).trim());
            govIds.setPhilHealthNumber(fields.get(EMP_PHILHEALTH).trim());
            govIds.setTinNumber(fields.get(EMP_TIN).trim());
            govIds.setPagIbigNumber(fields.get(EMP_PAGIBIG).trim());
            emp.setGovernmentIds(govIds);
            
            emp.setStatus(status);
            emp.setPosition(position);
            
            String supervisor = fields.get(EMP_SUPERVISOR).trim();
            if (!supervisor.equals("N/A") && !supervisor.isEmpty()) {
                emp.setImmediateSupervisor(supervisor);
            }
            
            emp.setBasicSalary(parseDouble(fields.get(EMP_BASIC_SALARY)));
            emp.setRiceSubsidy(parseDouble(fields.get(EMP_RICE_SUBSIDY)));
            emp.setPhoneAllowance(parseDouble(fields.get(EMP_PHONE_ALLOWANCE)));
            emp.setClothingAllowance(parseDouble(fields.get(EMP_CLOTHING_ALLOWANCE)));
            
            emp.setEmail(generateEmail(emp.getFirstName(), emp.getLastName()));
            
            // Set hire date
            if (emp.getBirthDate() != null) {
                emp.setHireDate(emp.getBirthDate().plusYears(20));
            } else {
                emp.setHireDate(LocalDate.now().minusYears(1));
            }
            
            // If probationary, set additional fields
            if (emp instanceof ProbationaryEmployee) {
                ProbationaryEmployee probEmp = (ProbationaryEmployee) emp;
                probEmp.setProbationStartDate(emp.getHireDate());
                probEmp.setSupervisor(emp.getImmediateSupervisor());
                probEmp.setProbationarySalary(emp.getBasicSalary() * 0.9);
            }
            
        } catch (Exception e) {
            LOGGER.warning("Error parsing employee: " + e.getMessage());
            return null;
        }
        
        return emp;
    }
    
    /**
     * Creates the appropriate employee type based on position and status
     */
    private Employee createEmployeeByPositionAndStatus(String position, String status) {
        if (position == null || position.isEmpty()) return new RegularEmployee();
        
        String pos = position.toLowerCase();
        String stat = status != null ? status.toLowerCase() : "";
        
        // Check if probationary first
        if (stat.contains("probationary")) {
            return new ProbationaryEmployee();
        }
        
        // Role-based creation
        if (pos.contains("chief") || pos.contains("ceo") || pos.contains("cfo") || 
            pos.contains("coo") || pos.contains("cmo") || pos.contains("admin")) {
            return new AdminEmployee();
        } else if (pos.contains("hr")) {
            return new HREmployee();
        } else if (pos.contains("finance") || pos.contains("account") || pos.contains("payroll")) {
            return new FinanceEmployee();
        } else if (pos.contains("it") || pos.contains("information technology") || 
                   pos.contains("system") || pos.contains("tech")) {
            return new ITEmployee();
        } else {
            return new RegularEmployee();
        }
    }
    
    /**
     * Reads all attendance records from the CSV file
     */
    public List<Attendance> readAttendance(String filePath) {
        List<Attendance> attendanceList = new ArrayList<>();
        Path path = Paths.get(filePath);
        
        if (!Files.exists(path)) {
            LOGGER.warning("Attendance file not found: " + filePath);
            return attendanceList;
        }
        
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            boolean isFirstLine = true;
            int lineNumber = 0;
            int successCount = 0;
            int errorCount = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }
                
                if (line.trim().isEmpty()) {
                    continue;
                }
                
                try {
                    Attendance attendance = parseAttendance(line);
                    if (attendance != null) {
                        attendanceList.add(attendance);
                        successCount++;
                    }
                } catch (Exception e) {
                    errorCount++;
                    LOGGER.fine("Error parsing attendance at line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            LOGGER.info(String.format("Successfully loaded %d attendance records from %s (skipped %d errors)", 
                                     successCount, filePath, errorCount));
            
        } catch (IOException e) {
            LOGGER.severe("Error reading attendance file: " + e.getMessage());
        }
        
        return attendanceList;
    }
    
    /**
     * Parses a single attendance record from CSV line
     */
    private Attendance parseAttendance(String csvLine) {
        String[] fields = csvLine.split(",");
        
        if (fields.length < 5) {
            throw new IllegalArgumentException("Invalid attendance line: insufficient fields");
        }
        
        Attendance attendance = new Attendance();
        
        try {
            attendance.setEmployeeId(fields[ATT_EMPLOYEE_ID].trim());
            attendance.setLastName(fields[ATT_LAST_NAME].trim());
            attendance.setFirstName(fields[ATT_FIRST_NAME].trim());
            
            // Parse date
            String dateStr = fields[ATT_DATE].trim();
            attendance.setDate(LocalDate.parse(dateStr, DATE_FORMATTER));
            
            // Parse time in
            String timeInStr = fields[ATT_TIME_IN].trim();
            if (!timeInStr.isEmpty()) {
                attendance.setTimeIn(LocalTime.parse(timeInStr, TIME_FORMATTER));
            }
            
            // Parse time out (if present)
            if (fields.length > ATT_TIME_OUT && !fields[ATT_TIME_OUT].trim().isEmpty()) {
                String timeOutStr = fields[ATT_TIME_OUT].trim();
                attendance.setTimeOut(LocalTime.parse(timeOutStr, TIME_FORMATTER));
                
                // Calculate hours worked if both times are present
                if (attendance.getTimeIn() != null) {
                    long minutes = Duration.between(attendance.getTimeIn(), attendance.getTimeOut()).toMinutes();
                    attendance.setHoursWorked(minutes / 60.0);
                }
            } else {
                attendance.setHoursWorked(0.0);
            }
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing attendance data: " + e.getMessage());
        }
        
        return attendance;
    }
    
    /**
     * Parses a CSV line handling quoted fields
     */
    private List<String> parseCSVLine(String line) {
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
        
        // Add the last field
        fields.add(currentField.toString());
        
        return fields;
    }
    
    /**
     * Parses a double value from string, removing commas and quotes
     */
    private double parseDouble(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.replace(",", "").replace("\"", "").trim());
        } catch (NumberFormatException e) {
            LOGGER.warning("Could not parse double value: " + value);
            return 0.0;
        }
    }
    
    /**
     * Generates an email address from first and last name
     */
    private String generateEmail(String firstName, String lastName) {
        String cleanFirstName = firstName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        String cleanLastName = lastName.split(" ")[0].toLowerCase().replaceAll("[^a-z]", "");
        return cleanFirstName + "." + cleanLastName + "@motorph.com";
    }
    
    /**
     * Links attendance records to employees
     */
    public Map<String, List<Attendance>> linkAttendanceToEmployees(List<Employee> employees, List<Attendance> attendanceList) {
        Map<String, List<Attendance>> employeeAttendanceMap = new HashMap<>();
        
        // Initialize map for all employees
        for (Employee emp : employees) {
            employeeAttendanceMap.put(emp.getEmployeeId(), new ArrayList<>());
        }
        
        // Add attendance records to corresponding employees
        for (Attendance att : attendanceList) {
            String empId = att.getEmployeeId();
            if (employeeAttendanceMap.containsKey(empId)) {
                employeeAttendanceMap.get(empId).add(att);
            } else {
                // Create a new list for employees not in the map
                employeeAttendanceMap.computeIfAbsent(empId, k -> new ArrayList<>()).add(att);
            }
        }
        
        return employeeAttendanceMap;
    }
}