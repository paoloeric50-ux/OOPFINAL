## Intoduction
MotorPH OOP Payroll System
A payroll management system for MotorPH built as an OOP course project. It handles employee records, daily attendance, and payroll processing with automatic Philippine statutory deductions (SSS, PhilHealth, Pag-IBIG, Withholding Tax).

Frontend: React 19, Tailwind CSS, Radix UI — runs on http://localhost:5000

Backend: Python FastAPI, MongoDB — runs on http://localhost:8000

```
MONGO_URL=mongodb+srv://<Eric_Paolo>:<PaoloEric>@cluster0.xxxxx.mongodb.net/
DB_NAME=motorph_db
JWT_SECRET=motorph-secret-key-2024
CORS_ORIGINS=*
```
## Running the Project (MacOS)

Open two terminals:

Terminal 1 - Backend

cd backend
uvicorn server --host 127.0.0.1 --port 8000 --reload

Terminal 2 - Frontend

cd frontend
PORT=5000 npx craco start
