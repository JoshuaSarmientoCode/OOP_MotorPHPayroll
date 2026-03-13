GROUP 23 OOP-S2101

# MotorPH Payroll System
### MO-IT103 Object-Oriented Programming | Milestone 2

---

## Overview

The **MotorPH Payroll System** is a desktop payroll management application built in Java using Object-Oriented Programming principles. It handles employee management, attendance tracking, payroll processing, leave requests, and IT support ticketing for MotorPH.

---

## Features

### Authentication & Access Control
- Secure login with employee ID and password
- Role-based access control (RBAC) — 5 distinct roles
- Default password format: `emp` + last 2 digits of employee ID (e.g., `emp09`)
- Password change with validation (min 5 characters, must contain letter + digit)

### Employee Management *(Admin/HR only)*
- Add, edit, delete, and view employee records
- Real-time duplicate validation (ID, name, phone, government IDs)
- Supports Regular, Probationary, Admin, HR, Finance, and IT employee types
- Government ID tracking (SSS, PhilHealth, TIN, Pag-IBIG)

### Attendance Tracking
- Time In / Time Out recording
- Automatic calculation of hours worked, overtime, and tardiness
- Monthly attendance summary per employee
- Attendance history with date filtering

### Payroll Processing *(Finance only)*
- Semi-monthly payslip generation
- Automated Philippine statutory deductions:
  - SSS (Social Security System)
  - PhilHealth
  - Pag-IBIG
  - Withholding Tax (BIR)
- Prorated salary based on attendance
- Payslip history per employee

### Leave Request Management
- Employee leave request submission
- HR approval / rejection workflow
- Leave credit tracking for regular employees
- Leave history per employee

### IT Support Ticketing
- Employees submit support tickets
- IT staff manage, assign, and resolve tickets
- Ticket status tracking (Open → In Progress → Resolved)

### System Logs *(Admin only)*
- Full audit trail of all system actions
- Filterable by user, date, and log level (INFO, WARNING, ERROR, AUDIT)

---

## Role Access Matrix

| Feature | Admin | HR | Finance | IT | Employee |
|---|:---:|:---:|:---:|:---:|:---:|
| Employee Management | ✅ | ✅ | ❌ | ❌ | ❌ |
| Payroll Processing | ❌ | ❌ | ✅ | ❌ | ❌ |
| View Own Payslip | ✅ | ✅ | ✅ | ✅ | ✅ |
| Leave Approvals | ❌ | ✅ | ❌ | ❌ | ❌ |
| Submit Leave | ✅ | ✅ | ✅ | ✅ | ✅ |
| Ticket Management | ❌ | ❌ | ❌ | ✅ | ❌ |
| Submit Ticket | ✅ | ✅ | ✅ | ✅ | ✅ |
| System Logs | ✅ | ❌ | ❌ | ❌ | ❌ |
| Attendance | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## OOP Design

### Four Pillars Implementation

**Encapsulation**
All model fields are `private` with validated getters and setters. Business rules are enforced at the setter level — salary bounds, leave credit limits, performance rating ranges. Utility methods in `BaseDAO` are `protected`, accessible to subclasses only.

**Inheritance**
```
Person (abstract)
└── Employee (abstract)
    ├── RegularEmployee
    │   └── ProbationaryEmployee
    ├── AdminEmployee
    ├── HREmployee
    ├── FinanceEmployee
    └── ITEmployee

BaseDAO<T> (abstract, generic)
├── EmployeeDAO
├── AttendanceDAO
├── PayrollDAO
├── LeaveRequestDAO
├── TicketDAO
├── UserDAO
└── SystemLogDAO

DeductionService (abstract)
├── SSSDeduction
├── PhilHealthDeduction
├── PagIbigDeduction
└── TaxDeduction
```

**Polymorphism**
- `getRoleName()`, `canAccess()`, `getDashboardType()` — overridden per employee type
- `calculate()` — each deduction class computes differently
- `fromCSV()` / `toCSV()` — overridden per DAO for type-safe parsing
- 119+ `@Override` annotations across the codebase

**Abstraction**
- `Employee` enforces 3 abstract methods on all subclasses
- `BaseDAO<T>` implements the Template Method pattern — full CRUD defined once, subclasses only implement `fromCSV()`, `toCSV()`, `getHeaders()`, `getId()`
- `DeductionService` abstracts the deduction calculation contract

### Interface Architecture

| Interface | Package | Implemented By | Purpose |
|---|---|---|---|
| `EmployeeInterface` | `model` | `Employee` | Polymorphic employee contract |
| `DAOInterface<T>` | `dao` | `BaseDAO<T>` | Generic repository contract |
| `Deductible` | `service.deductions` | `DeductionService` | Strategy pattern for deductions |
| `Approvable` | `model` | `LeaveRequest`, `Ticket` | Shared approval workflow |
| `Validatable` | `model` | `Employee`, `LeaveRequest`, `Ticket` | Self-validation contract |
| `DataService` | `dao` | `BaseDAO<T>` | Raw CSV I/O contract |

### Design Patterns Used

| Pattern | Where |
|---|---|
| Template Method | `BaseDAO<T>` — CRUD defined once, subclasses fill in the blanks |
| Strategy | `DeductionService` — deduction algorithm swappable at runtime |
| Facade | `CSVHandler` — simplified interface over DAO layer (MS1 compatibility) |
| MVC | `MainController` (Controller), model classes (Model), Swing panels (View) |
| Repository | All DAO classes — data access abstracted behind interface |

---

## Package Structure

```
src/main/java/
├── main/
│   ├── MotorPHApplication.java       Entry point
│   └── MainController.java           Application controller (MVC)
│
├── model/
│   ├── employee/                     Employee hierarchy
│   │   ├── Person.java
│   │   ├── Employee.java             Abstract base
│   │   ├── RegularEmployee.java
│   │   ├── ProbationaryEmployee.java
│   │   ├── AdminEmployee.java
│   │   ├── HREmployee.java
│   │   ├── FinanceEmployee.java
│   │   └── ITEmployee.java
│   ├── Attendance.java
│   ├── LeaveRequest.java
│   ├── Payroll.java
│   ├── Payslip.java
│   ├── Ticket.java
│   ├── SystemLog.java
│   ├── User.java
│   ├── GovernmentIds.java
│   ├── ProbationDetails.java
│   ├── EmploymentStatus.java         Enum
│   ├── EmployeeInterface.java        Interface
│   ├── Approvable.java               Interface
│   └── Validatable.java              Interface
│
├── dao/
│   ├── BaseDAO.java                  Abstract generic DAO
│   ├── DAOInterface.java             Interface
│   ├── DataService.java              Interface
│   ├── CSVHandler.java               Facade (MS1 compatibility)
│   ├── EmployeeDAO.java
│   ├── AttendanceDAO.java
│   ├── PayrollDAO.java
│   ├── LeaveRequestDAO.java
│   ├── TicketDAO.java
│   ├── UserDAO.java
│   └── SystemLogDAO.java
│
├── service/
│   ├── EmployeeService.java
│   ├── AttendanceService.java
│   ├── PayrollService.java
│   ├── UserService.java
│   ├── TicketService.java
│   ├── SystemLogService.java
│   ├── ValidationService.java
│   └── deductions/
│       ├── DeductionService.java     Abstract
│       ├── Deductible.java           Interface
│       ├── SSSDeduction.java
│       ├── PhilHealthDeduction.java
│       ├── PagIbigDeduction.java
│       └── TaxDeduction.java
│
└── ui/
    ├── components/
    │   └── UITheme.java              Centralized styling
    ├── LoginPanel.java
    ├── MainFrame.java
    ├── MainDashboardPanel.java
    ├── EmployeeManagementPanel.java
    ├── EmployeeDialog.java
    ├── EmployeeDetailsDialog.java
    ├── AttendancePanel.java
    ├── PayrollProcessingPanel.java
    ├── PayrollPanel.java
    ├── PayslipPanel.java
    ├── LeaveRequestPanel.java
    ├── LeaveApprovalsPanel.java
    ├── SubmitTicketPanel.java
    ├── TicketManagementPanel.java
    └── SystemLogsPanel.java
```

---

## Data Storage

All data is persisted as CSV files in the `data/` directory, created automatically on first run.

| File | Contents |
|---|---|
| `MotorPH_Employee_Details.csv` | Employee master data (provided) |
| `MotorPH_Attendance_Record.csv` | Attendance records (provided) |
| `users.csv` | Login credentials and roles |
| `leave_requests.csv` | Leave request records |
| `payroll.csv` | Processed payroll history |
| `tickets.csv` | IT support tickets |
| `system_logs.csv` | Audit trail |

---

## Setup & Running

### Requirements
- Java 11 or higher
- NetBeans IDE (recommended) or any Java IDE
- No external dependencies — pure Java SE

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/[your-username]/OOP_MotorPHPayroll.git
cd OOP_MotorPHPayroll
```

**2. Add the CSV data files**

Place the provided CSV files in the `data/` folder at the project root:
```
OOP_MotorPHPayroll/
└── data/
    ├── MotorPH_Employee_Details.csv
    └── MotorPH_Attendance_Record.csv
```

**3. Build and run**

In NetBeans: Open project → Clean and Build → Run

Or via terminal:
```bash
javac -d out -sourcepath src/main/java src/main/java/main/MotorPHApplication.java
java -cp out main.MotorPHApplication
```

### First Login

On first launch, user accounts are auto-created from the employee CSV. Use:

```
Employee ID:  [your 5-digit employee ID]
Password:     emp + last 2 digits of ID
Example:      Employee 10009 → password: emp09
```

---

## Team

| Name | Role |
|---|---|
| Joshua Al Hosani Sarmiento 
| Franzie Frielle Mangalindan 
| Vinson Sia 
| Jordan Isaac Resma 
| Bernice Mariano Cunanan 
---

## Course Information

- **Course:** MO-IT103 Object-Oriented Programming
- **Milestone:** 2 — Final Terminal Assessment
- **Institution:** Mapúa Malayan Digital College
