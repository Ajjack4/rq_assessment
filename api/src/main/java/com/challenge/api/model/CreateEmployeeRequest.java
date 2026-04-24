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
 * Only contains caller-supplied attributes. Server-side fields ({@code uuid},
 * {@code fullName}, {@code contractHireDate}) are assigned by the service and
 * are excluded from this contract.
 *
 * Validation is two-layered: Jakarta Bean Validation annotations here enforce
 * structural constraints (presence, format, bounds) before the request reaches
 * the service. {@code EmployeeValidator} then enforces security constraints
 * (injection protection, character allowlists, upper-bound business rules).
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
    @NotNull @Min(0)
    private Integer salary;

    /** Must be at least 16 to satisfy minimum working-age requirements. */
    @NotNull @Min(16)
    private Integer age;

    @NotBlank
    @Size(max = 150)
    private String jobTitle;

    @Email
    @NotBlank
    @Size(max = 254)
    private String email;
}
