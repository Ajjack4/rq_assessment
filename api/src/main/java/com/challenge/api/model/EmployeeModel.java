package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

/**
 * Concrete implementation of the {@link Employee} domain contract.
 *
 * {@code @Data} generates all getters and setters required by the interface,
 * along with {@code equals}, {@code hashCode}, and {@code toString}.
 */
@Data
public class EmployeeModel implements Employee {

    private UUID uuid;
    private String firstName;
    private String lastName;

    /** Derived from {@code firstName} and {@code lastName} at creation time. */
    private String fullName;

    private Integer salary;
    private Integer age;
    private String jobTitle;
    private String email;
    private Instant contractHireDate;

    /** Null when the employee has not been terminated. */
    private Instant contractTerminationDate;
}
