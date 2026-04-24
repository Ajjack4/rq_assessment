package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import java.util.List;
import java.util.UUID;

/**
 * Core employee operations. Interface-backed so the implementation
 * can be swapped out later when a real DB is introduced.
 */
public interface EmployeeService {

    /**
     * Returns all employees. Never null, may be empty.
     *
     * @return list of all employees
     */
    List<Employee> getAllEmployees();

    /**
     * Returns the employee with the given UUID.
     *
     * @param uuid the employee's unique identifier
     * @return the matching employee
     * @throws org.springframework.web.server.ResponseStatusException 404 if not found
     */
    Employee getEmployeeByUuid(UUID uuid);

    /**
     * Creates a new employee. UUID, fullName, and hire date are assigned here —
     * don't include them in the request.
     *
     * @param request fields for the new employee
     * @return the created employee including all server-assigned fields
     */
    Employee createEmployee(CreateEmployeeRequest request);
}
