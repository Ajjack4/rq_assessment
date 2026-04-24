# ReliaQuest's Entry-Level Java Challenge

> **Note:** This README has been edited by the candidate to document the implementation approach, authentication usage, and password configuration. The original problem statement and formatting guidelines are preserved below unchanged.

---

## My Approach

The goal was to expose a protected REST API that wraps an in-memory employee store. I approached this in three layers:

**1. Controller (`EmployeeController`)**
Thin layer responsible only for routing. No business logic — delegates everything to the service. HTTP semantics are handled here: `GET` for reads, `POST` for creation, `201 Created` on success.

**2. Service (`EmployeeService` / `EmployeeServiceImpl`)**
Backed by a `ConcurrentHashMap` for thread-safe in-memory storage. UUID, `fullName`, and `contractHireDate` are assigned here — not by the caller. Pre-seeded with three employees on startup via `@PostConstruct` so the `GET` endpoints return data immediately.

**3. Validation (`EmployeeValidator` + Jakarta annotations)**
Two-layer validation strategy:
- Jakarta `@Valid` annotations on the DTO handle structural constraints (not blank, min/max values, email format).
- `EmployeeValidator` handles business-rule constraints: valid job titles from the `JobTitle` enum, name character rules, XSS/null-byte injection prevention, and salary/age upper bounds.
- All violations are collected before throwing so callers see every error in one response.

**Security**
HTTP Basic Auth is enforced on all endpoints via Spring Security. CSRF is disabled (correct for stateless REST APIs) and sessions are stateless — every request must carry credentials.

**Error handling**
`GlobalExceptionHandler` centralises all exception mapping. A `ResponseEntity<ErrorResponse>` return type is used for `ResponseStatusException` to ensure the exception's status code (e.g. 404) is forwarded correctly to the HTTP response — returning a plain type would default to 200.

---

## Making Requests with Basic Auth

The API requires HTTP Basic Auth on every request. Use the credentials configured in `application.yml` (see [Changing the Password](#changing-the-password) below).

**Default credentials**
- Username: `reliaquest`
- Password: `changeme`

**curl examples**

Get all employees:
```bash
curl -u reliaquest:changeme http://localhost:8080/api/v1/employee
```

Get employee by UUID:
```bash
curl -u reliaquest:changeme http://localhost:8080/api/v1/employee/{uuid}
```

Create an employee:
```bash
curl -u reliaquest:changeme \
     -X POST http://localhost:8080/api/v1/employee \
     -H "Content-Type: application/json" \
     -d '{
           "firstName": "Jane",
           "lastName": "Doe",
           "salary": 95000,
           "age": 30,
           "jobTitle": "Software Engineer",
           "email": "jane.doe@company.com"
         }'
```

**Using Postman or an HTTP client**
Set the Authorization type to `Basic Auth` and supply the same username and password.

---

## Changing the Password

The password is read from the `API_PASSWORD` environment variable at startup. If the variable is not set, it falls back to the default `changeme`.

**Set the variable before running the app:**
```bash
# Linux / macOS
export API_PASSWORD=your-secure-password
./gradlew bootRun

# Windows (Command Prompt)
set API_PASSWORD=your-secure-password
gradlew.bat bootRun

# Windows (PowerShell)
$env:API_PASSWORD = "your-secure-password"
./gradlew.bat bootRun
```

**Or pass it inline:**
```bash
API_PASSWORD=your-secure-password ./gradlew bootRun
```

The binding in `application.yml` is:
```yaml
spring:
  security:
    user:
      name: reliaquest
      password: ${API_PASSWORD:changeme}
```

Replace `changeme` in the default only if you are running in an environment where setting environment variables is not possible. Never commit a real password to source control.

---

## Problem Statement

Your employer has recently purchased a license to top-tier SaaS platform, Employees-R-US, to off-load all employee management responsibilities.
Unfortunately, your company's product has an existing employee management solution that is tightly coupled to other services and therefore 
cannot be replaced whole-cloth. Product and Development leads in your department have decided it would be best to interface
the existing employee management solution with the commercial offering from Employees-R-US for the time being until all employees can be
migrated to the new SaaS platform.

Your ask is to expose employee information as a protected, secure REST API for consumption by Employees-R-US web hooks.
The initial REST API will consist of 3 endpoints, listed in the following section. If for any reason the implementation 
of an endpoint is problematic, the team lead will accept **pseudo-code** and a pertinent description (e.g. java-doc) of intent.

Good luck!

---

Please keep the following in mind while working on this challenge:
* Code implementations will not be graded for **correctness** but rather on practicality
* Articulate clear and concise design methodologies, if necessary
* Use clean coding etiquette
  * E.g. avoid liberal use of new-lines, odd variable and method names, random indentation, etc...
* Test cases are not required

## Endpoints to implement (API module)

_See `com.challenge.api.controller.EmployeeController` for details._

getAllEmployees()

    output - list of employees
    description - this should return all employees, unfiltered

getEmployeeByUuid(...)

    path variable - employee UUID
    output - employee
    description - this should return a single employee based on the provided employee UUID

createEmployee(...)

    request body - attributes necessary to create an employee
    output - employee
    description - this should return a single employee, if created, otherwise error

## Code Formatting

This project utilizes Gradle plugin [Diffplug Spotless](https://github.com/diffplug/spotless/tree/main/plugin-gradle) to enforce format
and style guidelines with every build.

To format code according to style guidelines, you can run **spotlessApply** task.
`./gradlew spotlessApply`

The spotless plugin will also execute check-and-validation tasks as part of the gradle **build** task.
`./gradlew build`
