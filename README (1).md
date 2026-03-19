# MotorPH OOP Payroll System

A full-stack **Java** payroll management system built with Spring Boot and Thymeleaf, demonstrating Object-Oriented Programming (OOP) principles through a real-world payroll application for the Philippine market.

---

## Overview

This system manages employees, attendance tracking, and payroll processing with Philippine-specific statutory deductions. The entire application — both frontend UI and backend logic — is written in **Java** using Spring Boot as the web framework and Thymeleaf as the server-side HTML rendering engine.

---

## OOP Concepts Demonstrated

| Concept | Implementation |
|---|---|
| **Abstraction** | `Employee` is an abstract class that defines the payroll contract via `calculateGrossPay()` and `calculateNetPay()` |
| **Encapsulation** | All domain models use private fields with controlled access through getters/setters |
| **Inheritance** | `FullTimeEmployee`, `PartTimeEmployee`, and `ContractEmployee` extend `Employee` with type-specific pay logic |
| **Polymorphism** | `PayrollProcessor` handles any `Employee` subtype uniformly through the abstract interface |

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 19 |
| Framework | Spring Boot 3.2.5 |
| View Engine | Thymeleaf (server-side HTML rendering) |
| Security | Spring Security + JWT |
| Database | MongoDB |
| Build Tool | Maven |
| CSS Framework | Bootstrap 5.3 |
| Icons | Bootstrap Icons |

---

## Project Structure

```
backend-java/
├── src/main/java/com/motorph/payroll/
│   ├── MotorPhApplication.java          # Application entry point
│   ├── model/                           # Core OOP domain model
│   │   ├── Employee.java                # Abstract base class (Abstraction)
│   │   ├── FullTimeEmployee.java        # Monthly salary employee (Inheritance)
│   │   ├── PartTimeEmployee.java        # Hourly rate employee (Inheritance)
│   │   ├── ContractEmployee.java        # Daily rate employee (Inheritance)
│   │   ├── EmployeeFactory.java         # Factory pattern for employee creation
│   │   ├── DeductionCalculator.java     # SSS, PhilHealth, Pag-IBIG, Tax
│   │   ├── PayrollProcessor.java        # Processes payroll (Polymorphism)
│   │   ├── AttendanceTracker.java       # Tracks clock-in/out records
│   │   └── Payslip.java                 # Payslip value object
│   ├── controller/                      # HTTP request handlers
│   │   ├── WebController.java           # Serves Thymeleaf page routes
│   │   ├── AuthController.java          # Login and registration API
│   │   ├── EmployeeController.java      # Employee CRUD API
│   │   ├── AttendanceController.java    # Attendance management API
│   │   ├── PayrollController.java       # Payroll processing API
│   │   ├── DashboardController.java     # Dashboard stats API
│   │   ├── DeductionController.java     # Deduction calculation API
│   │   └── OopController.java           # OOP demonstration API
│   ├── service/                         # Business logic layer
│   ├── repository/                      # MongoDB data access layer
│   ├── document/                        # MongoDB document models
│   ├── dto/                             # Data transfer objects
│   ├── security/                        # JWT authentication filter
│   └── config/                          # CORS and Security configuration
└── src/main/resources/
    ├── application.properties           # Server and database configuration
    ├── templates/                       # Thymeleaf HTML templates
    │   ├── login.html                   # Login page
    │   ├── register.html                # Registration page
    │   ├── dashboard.html               # Dashboard with charts and stats
    │   ├── employees.html               # Employee list with filters
    │   ├── employee-form.html           # Create / edit employee form
    │   ├── attendance.html              # Attendance tracking
    │   ├── payroll.html                 # Payroll processing and payslips
    │   ├── oop-concepts.html            # OOP concepts demonstration
    │   └── fragments/
    │       └── sidebar.html             # Shared navigation sidebar
    └── static/
        ├── css/app.css                  # Custom dark-theme styles
        └── js/app.js                    # Shared API client and utilities
```

---

## Features

- **Employee Management** — Add, edit, and delete full-time, part-time, and contract employees
- **Attendance Tracking** — Clock in / clock out with daily hours calculation
- **Payroll Processing** — Generate payslips with automatic statutory deductions
- **Philippine Deductions** — SSS (2024 table), PhilHealth, Pag-IBIG, and TRAIN Law withholding tax
- **Dashboard** — Live stats with employee distribution and department breakdown charts
- **JWT Authentication** — Secure login with token-based session management
- **OOP Concepts Page** — Interactive demonstration of all four OOP pillars with live examples

---

## Philippine Statutory Deductions

| Deduction | Basis |
|---|---|
| SSS | 2024 contribution table (employee share) |
| PhilHealth | 5% of monthly basic salary (employee share: 2.5%), capped at PHP 5,000/month |
| Pag-IBIG | 2% of monthly salary, capped at PHP 100/month |
| Withholding Tax | TRAIN Law annualization formula |

---

## Running the Application

### Prerequisites

- Java 19 or higher
- Maven 3.8+
- MongoDB 6.0+ running on `localhost:27017`

### Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGO_URL` | `mongodb://localhost:27017/motorph_db` | MongoDB connection string |
| `DB_NAME` | `motorph_db` | Database name |
| `JWT_SECRET` | `motorph-secret-key-2024` | JWT signing secret |

### Start the Application

```bash
cd backend-java
mvn spring-boot:run
```

The application will be available at `http://localhost:5000`.

### First-Time Setup

1. Open `http://localhost:5000`
2. Click **Register here** to create an admin account
3. Log in and start adding employees

---

## API Endpoints

All API endpoints are prefixed with `/api` and require a valid JWT token in the `Authorization: Bearer <token>` header (except auth endpoints).

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |
| GET | `/api/employees` | List all employees |
| POST | `/api/employees` | Create a new employee |
| GET | `/api/employees/{id}` | Get employee by ID |
| PUT | `/api/employees/{id}` | Update employee |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/attendance` | List attendance records |
| POST | `/api/attendance/clock-in` | Record clock-in |
| PUT | `/api/attendance/{id}/clock-out` | Record clock-out |
| GET | `/api/payroll/payslips` | List all payslips |
| POST | `/api/payroll/process` | Process payroll for an employee |
| GET | `/api/dashboard/stats` | Get dashboard statistics |
| POST | `/api/deductions/calculate` | Calculate deductions for a salary |

---

## Security

- Passwords are hashed using BCrypt
- All protected routes require a valid JWT token
- Tokens expire after 24 hours
- CORS is configurable via the `CORS_ORIGINS` environment variable
