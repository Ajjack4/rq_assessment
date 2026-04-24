package com.challenge.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * What the caller sends when creating an employee.
 * Don't include uuid, fullName, or contractHireDate — the backend sets those.
 * jobTitle must match one of the values in {@link JobTitle} (case-insensitive).
 */
@Data
public class CreateEmployeeRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    /** In USD. Must be 0 or more. */
    @NotNull @Min(0)
    private Integer salary;

    /** Must be between 16 and 100. */
    @NotNull @Min(16)
    private Integer age;

    /** Must match a value from {@link JobTitle}, e.g. "Product Manager". */
    @NotBlank
    private String jobTitle;

    @Email
    @NotBlank
    @Size(max = 254)
    private String email;
}
