package com.challenge.api.controller;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.service.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes employee management endpoints at {@code /api/v1/employee}.
 *
 * Handles HTTP concerns only — routing, status codes, and request binding.
 * All business logic is delegated to {@link EmployeeService}.
 */
@RestController
@RequestMapping("/api/v1/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * @param employeeService the service handling employee business logic
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Returns all employees, unfiltered.
     *
     * @return 200 with all employees; may be an empty list
     */
    @GetMapping
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    /**
     * Returns a single employee by UUID.
     *
     * @param uuid the employee's unique identifier
     * @return 200 with the matching employee, or 404 if not found
     */
    @GetMapping("/{uuid}")
    public Employee getEmployeeByUuid(@PathVariable UUID uuid) {
        return employeeService.getEmployeeByUuid(uuid);
    }

    /**
     * Creates a new employee from the request body.
     *
     * Returns 201 Created to signal that a new resource was created, per REST semantics.
     *
     * @param requestBody the attributes required to create the employee
     * @return 201 with the created employee including all server-assigned fields
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee createEmployee(@Valid @RequestBody CreateEmployeeRequest requestBody) {
        return employeeService.createEmployee(requestBody);
    }
}
