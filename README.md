# MotorPH OOP Payroll System

**Course:** MO-IT101 - Object-Oriented Programming  
**School:** Mapua Malayan Colleges Mindanao  
**Term:** AY 2024–2025  
**Group:** Group 5

---

## About This Project

This is our final project for MO-IT101. We built a payroll management system for the fictional company **MotorPH** using Java. The system demonstrates all four core OOP concepts that we learned in class.

We built the backend using **Spring Boot** (Java web framework), the UI with **Thymeleaf** (HTML templates), and the data is saved using an **H2 embedded database** (no separate database server needed to run this).

---

## What the System Can Do

- Register and log in as an HR user (JWT token-based authentication)
- Add, view, update, and delete employees (Full-Time, Part-Time, and Contract types)
- Clock in and clock out employee attendance
- Process payroll for one or all employees and generate payslips
- Automatically compute SSS, PhilHealth, Pag-IBIG, and TRAIN Law withholding tax
- View a dashboard with summary statistics
- See how the OOP concepts were applied through the OOP Concepts page

---

## OOP Concepts Applied

### 1. Abstraction
`Employee.java` is an **abstract class** — you can't create a plain `Employee` object directly. You have to use one of the subclasses. The abstract methods `computeSalary()` and `getSalaryBreakdown()` force subclasses to provide their own implementation.

### 2. Encapsulation
In `Employee.java`, the `basicSalary` field is **private**. It can only be read or changed through the `getBasicSalary()` and `setBasicSalary()` methods. The setter also validates the value (no negative salaries allowed). This protects the data from being accidentally changed.

### 3. Inheritance
Three employee types inherit from `Employee`:
- `FullTimeEmployee` — paid by monthly salary + overtime
- `PartTimeEmployee` — paid by hourly rate
- `ContractEmployee` — paid by daily rate

Each one calls `super(data)` to use the parent's constructor, then adds its own specific fields.

### 4. Polymorphism
The `PayrollProcessor` class calls `employee.computeSalary()` without needing to know what type of employee it is. The JVM figures out which version to call at runtime. This is method overriding / runtime polymorphism.

We also use **method overloading** — `computeSalary()` has a no-argument version that defaults to zeros.

---

## Design Patterns Used

**Factory Pattern** — `EmployeeFactory.java` creates the right employee subclass based on the `employee_type` value. This keeps the object creation logic in one place.

**Composition** — `PayrollProcessor` uses composition to combine `DeductionCalculator`, `AttendanceTracker`, and `EmployeeFactory` rather than inheriting from them.

---

## How to Run

### Requirements
- Java 19 (or newer)
- Maven 3.8+

### Steps

1. Clone or download the project
2. Open a terminal in the project folder
3. Run the startup script:

```bash
bash start.sh
```

4. Open your browser and go to: `http://localhost:5000`
5. Click **Register** to create an account, then log in

> The database is created automatically on first startup. No setup needed.

---

## Project Structure

```
backend-java/
├── src/main/java/com/motorph/payroll/
│   ├── model/                     ← OOP model classes (core of the project)
│   │   ├── Employee.java          ← Abstract base class (Abstraction + Encapsulation)
│   │   ├── FullTimeEmployee.java  ← Subclass (Inheritance + Polymorphism)
│   │   ├── PartTimeEmployee.java  ← Subclass (Inheritance + Polymorphism)
│   │   ├── ContractEmployee.java  ← Subclass (Inheritance + Polymorphism)
│   │   ├── EmployeeFactory.java   ← Factory Pattern
│   │   ├── DeductionCalculator.java ← Philippine deductions math
│   │   ├── AttendanceTracker.java ← Clock-in/out logic
│   │   ├── PayrollProcessor.java  ← Orchestrates the payroll run
│   │   └── Payslip.java          ← Result of payroll processing
│   │
│   ├── document/                  ← JPA entities (database tables)
│   │   ├── EmployeeDocument.java
│   │   ├── UserDocument.java
│   │   ├── AttendanceDocument.java
│   │   └── PayslipDocument.java
│   │
│   ├── repository/                ← Database access (Spring Data JPA)
│   ├── service/                   ← Business logic layer
│   ├── controller/                ← Web routes (REST API + page routes)
│   ├── dto/                       ← Request/response data shapes
│   ├── security/                  ← JWT login filter and utilities
│   └── config/                    ← Spring Security, CORS, converter config
│
├── src/main/resources/
│   ├── templates/                 ← Thymeleaf HTML pages
│   ├── static/                    ← CSS and JavaScript files
│   └── application.properties    ← App configuration
│
└── data/                          ← H2 database file (auto-created)

start.sh                           ← Startup script
```

---

## Salary Computation

### Full-Time Employees
```
daily_rate   = monthly_salary / 22 working days
base_pay     = daily_rate × days_worked
overtime_pay = overtime_hours × hourly_rate × 1.25   (PH labor law OT rate)
gross_pay    = base_pay + overtime_pay
```

### Part-Time Employees
```
gross_pay = hourly_rate × hours_worked
```

### Contract Employees
```
gross_pay = daily_rate × days_worked
```

---

## Deductions (Philippine Mandatories)

| Deduction       | Basis                                               |
|----------------|------------------------------------------------------|
| SSS            | Table lookup based on gross salary (2024 rates)      |
| PhilHealth     | 5% of gross salary, employee pays 2.5% (min 250)    |
| Pag-IBIG       | 1–2% of gross, capped at PHP 100/month              |
| Withholding Tax| TRAIN Law (RA 10963) — annualized, then ÷ 12        |

```
net_pay = gross_pay - (SSS + PhilHealth + Pag-IBIG + Withholding Tax)
```

---

## Technologies Used

| Technology       | Purpose                                              |
|-----------------|------------------------------------------------------|
| Java 19          | Programming language                                |
| Spring Boot 3.2  | Web framework (handles HTTP, routing, security)     |
| Spring Data JPA  | Database access layer                               |
| H2 Database      | Embedded SQL database (no install needed)           |
| Hibernate ORM    | Maps Java objects to database tables                |
| Thymeleaf        | HTML template engine for server-side rendering      |
| Spring Security  | Login/logout, route protection                      |
| JWT              | Token-based authentication (stays logged in)        |
| Maven            | Build and dependency management                     |

---

## Notes

- Default admin account: register one through the `/register` page
- The H2 database file is stored at `backend-java/data/motorph_db.mv.db`
- To reset the database, delete that file and restart the app
- Payroll is based on a standard 22-working-day month

---

*This project was created for academic purposes as part of our OOP course requirements.*
