# ReliaQuest's Entry-Level Java Challenge

> **Note:** This README has been edited by the candidate to document the implementation approach, authentication usage, and password configuration. The original problem statement and formatting guidelines are preserved below unchanged.

---

## My Approach

Three-layer REST API over an in-memory `ConcurrentHashMap` store:

- **Controller** — routing only; delegates all logic to the service layer.
- **Service** — assigns UUID, `fullName`, and hire date; pre-seeded with mock data via `@PostConstruct`.
- **Validation** — Jakarta annotations for structural constraints, `EmployeeValidator` for business rules (job title enum, name format, XSS/null-byte checks); all violations collected before throwing.

**Security:** HTTP Basic Auth via Spring Security, CSRF disabled, stateless sessions.

**Error handling:** `GlobalExceptionHandler` maps all exceptions to a consistent `ErrorResponse`. `ResponseStatusException` is handled via `ResponseEntity` to correctly propagate status codes (e.g. 404) to the HTTP response.

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

## Original Assignment

Please keep the following in mind while working on this challenge:
* Code implementations will not be graded for **correctness** but rather on practicality
* Articulate clear and concise design methodologies, if necessary
* Use clean coding etiquette
  * E.g. avoid liberal use of new-lines, odd variable and method names, random indentation, etc...
* Test cases are not required

### Problem Statement

Your employer has recently purchased a license to top-tier SaaS platform, Employees-R-US, to off-load all employee management responsibilities.
Unfortunately, your company's product has an existing employee management solution that is tightly coupled to other services and therefore 
cannot be replaced whole-cloth. Product and Development leads in your department have decided it would be best to interface
the existing employee management solution with the commercial offering from Employees-R-US for the time being until all employees can be
migrated to the new SaaS platform.

Your ask is to expose employee information as a protected, secure REST API for consumption by Employees-R-US web hooks.
The initial REST API will consist of 3 endpoints, listed in the following section. If for any reason the implementation 
of an endpoint is problematic, the team lead will accept **pseudo-code** and a pertinent description (e.g. java-doc) of intent.

Good luck!

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
