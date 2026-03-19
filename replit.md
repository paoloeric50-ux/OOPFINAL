# MotorPH OOP Payroll System

## Overview
A full-stack Java payroll management system for MotorPH, demonstrating OOP principles with Philippine-specific payroll processing.

## Tech Stack
- **Language:** Java 19
- **Framework:** Spring Boot 3.2.5
- **View Engine:** Thymeleaf (server-side HTML)
- **Database:** H2 (embedded SQL database, file-based persistence)
- **ORM:** Spring Data JPA / Hibernate
- **Security:** Spring Security with JWT authentication
- **Build:** Apache Maven

## Project Structure
```
backend-java/         - Spring Boot application
  src/main/java/com/motorph/payroll/
    config/           - Security, CORS, and MapJsonConverter
    controller/       - Web and API controllers
    document/         - JPA entity classes (EmployeeDocument, UserDocument, etc.)
    dto/              - Data transfer objects
    model/            - Core OOP domain models
    repository/       - Spring Data JPA repositories
    security/         - JWT filter and utilities
    service/          - Business logic (no MongoTemplate)
  src/main/resources/
    application.properties  - App configuration
    static/           - CSS, JS assets
    templates/        - Thymeleaf HTML templates
  data/               - H2 database files (motorph_db.mv.db)
start.sh              - Startup script (build + run JAR)
```

## Running the App
The app starts via `start.sh` which:
1. Creates the data directory
2. Builds the Maven project (skips tests)
3. Runs the JAR on port 5000

## Configuration
- Server port: 5000 (bound to 0.0.0.0 for Replit proxy)
- Database: H2 file-based at `./data/motorph_db` (auto-created)
- JWT secret: configurable via JWT_SECRET env var

## Database
H2 embedded SQL database — no separate process required. Data persists in `backend-java/data/motorph_db.mv.db`. Tables are auto-created by Hibernate on first startup.

### Tables
- `employees` - Employee records
- `users` - User accounts
- `attendance` - Clock-in/out records
- `payslips` - Generated payslips (earnings/deductions stored as JSON)

## Key Features
- Employee management (Full-Time, Part-Time, Contract types)
- Attendance tracking (clock-in/out)
- Payroll processing with Philippine deductions (SSS, PhilHealth, Pag-IBIG, TRAIN Law)
- Dashboard with real-time statistics
- OOP concepts demonstration page
