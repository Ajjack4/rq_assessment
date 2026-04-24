package com.challenge.api.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for the create-employee endpoint.
 *
 * Only contains caller-supplied attributes. Server-side fields — {@code uuid},
 * {@code fullName}, and {@code contractHireDate} — are assigned by the service
 * and must not be included in the request. Any extra fields are ignored.
 *
 * Validation is two-layered:
 * 1. Jakarta annotations here enforce structural constraints (presence, format, min bounds).
 * 2. {@code EmployeeValidator} enforces security and business constraints (email regex,
 *    job title whitelist, upper bounds, injection protection).
 *
 * Accepted job titles are defined in {@link JobTitle}. The value must match a
 * {@code JobTitle} display name (case-insensitive), e.g. "Product Manager".
 */
@Data
public class CreateEmployeeRequest {

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    /** Annual salary in USD; must be zero or greater. */
    @NotNull
    @Min(0)
    private Integer salary;

    /** Must be at least 16 to satisfy minimum working-age requirements. */
    @NotNull
    @Min(16)
    private Integer age;

    /** Must match an accepted value from {@link JobTitle} (case-insensitive). */
    @NotBlank
    private String jobTitle;

    @Email
    @NotBlank
    @Size(max = 254)
    private String email;
}
