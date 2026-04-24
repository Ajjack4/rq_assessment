package com.challenge.api.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

/**
 * Concrete Employee implementation. @Data handles all the getters/setters
 * the Employee interface requires.
 */
@Data
public class EmployeeModel implements Employee {

    private UUID uuid;
    private String firstName;
    private String lastName;

    /** firstName + lastName, set at creation time. */
    private String fullName;

    private Integer salary;
    private Integer age;
    private String jobTitle;
    private String email;
    private Instant contractHireDate;

    /** Null means the employee is still active. */
    private Instant contractTerminationDate;
}
