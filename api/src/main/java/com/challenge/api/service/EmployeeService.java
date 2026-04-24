package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import java.util.List;
import java.util.UUID;

/**
 * Business operations for employee management.
 *
 * Backed by an interface so the HTTP layer stays decoupled from the implementation,
 * making it easy to swap the in-memory store for a real persistence layer later.
 */
public interface EmployeeService {

    /**
     * Returns all employees in the system.
     *
     * @return all employees; never null, may be empty
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
     * Creates a new employee from the given request.
     *
     * The service assigns the UUID, derives {@code fullName}, and sets {@code contractHireDate}.
     *
     * @param request the attributes for the new employee
     * @return the created employee including all server-assigned fields
     */
    Employee createEmployee(CreateEmployeeRequest request);
}
