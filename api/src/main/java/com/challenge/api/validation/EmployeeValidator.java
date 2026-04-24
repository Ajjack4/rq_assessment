package com.challenge.api.validation;

import com.challenge.api.model.CreateEmployeeRequest;
import com.challenge.api.model.JobTitle;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralized validator for employee request data.
 *
 * Works alongside Jakarta Bean Validation annotations on the request DTO.
 * Jakarta handles structural constraints (not-null, not-blank, min values).
 * This validator enforces everything annotations cannot express: email format,
 * job title whitelisting, explicit numeric bounds, injection protection, and
 * character allowlists. All violations are collected before throwing so the
 * caller sees every problem at once.
 *
 * Inject this component wherever employee input needs to be validated.
 */
@Component
public class EmployeeValidator {

    // Unicode letters, spaces, hyphens, apostrophes — covers international names.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '\\-]+$");

    // RFC 5322-aligned email pattern: requires localpart @ domain . TLD (2+ chars).
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    // Detects HTML/XML tags — blocks stored XSS in any string field.
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 maximum
    private static final int MIN_SALARY = 0;
    private static final int MAX_SALARY = 10_000_000;
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 100;

    /**
     * Validates all fields in the given request, collecting every violation before throwing.
     *
     * @param request the request to validate; assumed non-null (enforced upstream by @Valid)
     * @throws ResponseStatusException 400 if any field fails validation
     */
    public void validate(CreateEmployeeRequest request) {
        List<String> violations = new ArrayList<>();

        validateName("firstName", request.getFirstName(), violations);
        validateName("lastName", request.getLastName(), violations);
        validateEmail(request.getEmail(), violations);
        validateJobTitle(request.getJobTitle(), violations);
        validateSalary(request.getSalary(), violations);
        validateAge(request.getAge(), violations);

        if (!violations.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Validation failed: " + String.join("; ", violations));
        }
    }

    /**
     * Validates a name field (firstName or lastName).
     *
     * Names must contain only Unicode letters, spaces, hyphens, or apostrophes.
     * This covers international names while blocking digits, scripts, and injection payloads.
     */
    private void validateName(String field, String value, List<String> violations) {
        if (value == null) return;

        if (value.length() > MAX_NAME_LENGTH) {
            violations.add(field + " must not exceed " + MAX_NAME_LENGTH + " characters");
        }
        if (!NAME_PATTERN.matcher(value).matches()) {
            violations.add(field + " must contain only letters, spaces, hyphens, or apostrophes");
        }
        checkInjection(field, value, violations);
    }

    /**
     * Validates the email field format and checks for injection.
     *
     * Applies its own regex rather than relying solely on {@code @Email}, which uses
     * a lenient pattern that can pass malformed addresses like "user@domain" (no TLD).
     */
    private void validateEmail(String value, List<String> violations) {
        if (value == null) return;

        if (value.length() > MAX_EMAIL_LENGTH) {
            violations.add("email must not exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            violations.add("email must be a valid address (e.g. user@example.com)");
        }
        checkInjection("email", value, violations);
    }

    /**
     * Validates that the job title is one of the accepted values defined in {@link JobTitle}.
     *
     * Matching is case-insensitive so "product manager" and "Product Manager" are both accepted.
     */
    private void validateJobTitle(String value, List<String> violations) {
        if (value == null) return;

        checkInjection("jobTitle", value, violations);
        if (!JobTitle.isValid(value)) {
            violations.add("jobTitle must be one of: " + String.join(", ", JobTitle.validDisplayNames()));
        }
    }

    /**
     * Validates that salary is within the accepted range [{@code MIN_SALARY}, {@code MAX_SALARY}].
     *
     * Both bounds are checked here as a definitive safety net, even though {@code @Min(0)}
     * on the DTO enforces the lower bound at the Jakarta layer.
     */
    private void validateSalary(Integer value, List<String> violations) {
        if (value == null) return;

        if (value < MIN_SALARY) {
            violations.add("salary must be " + MIN_SALARY + " or greater");
        }
        if (value > MAX_SALARY) {
            violations.add("salary must not exceed " + MAX_SALARY);
        }
    }

    /**
     * Validates that age is within the accepted range [{@code MIN_AGE}, {@code MAX_AGE}].
     *
     * Both bounds are checked here as a definitive safety net, even though {@code @Min(16)}
     * on the DTO enforces the lower bound at the Jakarta layer.
     */
    private void validateAge(Integer value, List<String> violations) {
        if (value == null) return;

        if (value < MIN_AGE) {
            violations.add("age must be at least " + MIN_AGE);
        }
        if (value > MAX_AGE) {
            violations.add("age must not exceed " + MAX_AGE);
        }
    }

    /**
     * Checks a string field for injection attack patterns.
     *
     * Null bytes can truncate strings in downstream systems and bypass filters.
     * HTML tags in stored data can execute as scripts if ever rendered in a browser.
     */
    private void checkInjection(String field, String value, List<String> violations) {
        if (value.contains("\0")) {
            violations.add(field + " contains invalid null byte characters");
        }
        if (HTML_TAG_PATTERN.matcher(value).find()) {
            violations.add(field + " must not contain HTML or script content");
        }
    }
}
