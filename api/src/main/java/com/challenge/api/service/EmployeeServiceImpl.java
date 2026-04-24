package com.challenge.api.service;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.Employee;
import com.challenge.api.model.EmployeeModel;
import com.challenge.api.validation.EmployeeValidator;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * In-memory implementation of {@link EmployeeService} backed by a {@link ConcurrentHashMap}.
 *
 * Thread-safe without explicit synchronization. Seeded with mock employees at startup so
 * {@code GET /api/v1/employee} returns data without requiring a prior {@code POST}.
 */
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final Map<UUID, Employee> store = new ConcurrentHashMap<>();
    private final EmployeeValidator employeeValidator;

    public EmployeeServiceImpl(EmployeeValidator employeeValidator) {
        this.employeeValidator = employeeValidator;
    }

    /** {@inheritDoc} */
    @Override
    public List<Employee> getAllEmployees() {
        return new ArrayList<>(store.values());
    }

    /**
     * {@inheritDoc}
     *
     * @throws ResponseStatusException 404 if no employee exists for the given UUID
     */
    @Override
    public Employee getEmployeeByUuid(UUID uuid) {
        Employee employee = store.get(uuid);
        if (employee == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found: " + uuid);
        }
        return employee;
    }

    /**
     * {@inheritDoc}
     *
     * Runs {@link EmployeeValidator#validate} before persisting to enforce security
     * constraints and business rules beyond what Jakarta annotations can express.
     */
    @Override
    public Employee createEmployee(CreateEmployeeRequest request) {
        employeeValidator.validate(request);

        EmployeeModel employee = new EmployeeModel();
        employee.setUuid(UUID.randomUUID());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setFullName(request.getFirstName() + " " + request.getLastName());
        employee.setSalary(request.getSalary());
        employee.setAge(request.getAge());
        employee.setJobTitle(request.getJobTitle());
        employee.setEmail(request.getEmail());
        employee.setContractHireDate(Instant.now());

        store.put(employee.getUuid(), employee);
        return employee;
    }

    /**
     * Populates the store with mock employees at startup.
     *
     * Uses {@code @PostConstruct} rather than the constructor to avoid calling an overridable
     * method before the bean is fully initialized. Routes through {@link #createEmployee} so
     * UUID generation and field derivation stay in one place.
     */
    @PostConstruct
    void seedMockData() {
        createEmployee(buildRequest("Alice", "Johnson", 120000, 34, "Staff Engineer", "alice.johnson@company.com"));
        createEmployee(buildRequest("Marcus", "Lee", 95000, 28, "Backend Engineer", "marcus.lee@company.com"));
        createEmployee(buildRequest("Priya", "Nair", 105000, 31, "Product Manager", "priya.nair@company.com"));
    }

    /**
     * Builds a {@link CreateEmployeeRequest} from the given attributes.
     *
     * @param firstName first name
     * @param lastName last name
     * @param salary annual salary in USD
     * @param age age in years
     * @param jobTitle job title
     * @param email company email address
     * @return a fully populated request
     */
    private CreateEmployeeRequest buildRequest(
            String firstName, String lastName, Integer salary, Integer age, String jobTitle, String email) {
        CreateEmployeeRequest request = new CreateEmployeeRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setSalary(salary);
        request.setAge(age);
        request.setJobTitle(jobTitle);
        request.setEmail(email);
        return request;
    }
}
