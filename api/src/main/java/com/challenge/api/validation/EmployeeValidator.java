package com.challenge.api.validation;

import com.challenge.api.model.CreateEmployeeRequest;
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
 * This validator handles security concerns that annotations cannot express:
 * injection attacks, character allowlists, upper-bound business rules, and
 * input length caps to prevent memory abuse.
 *
 * Inject this component wherever employee input needs to be validated.
 */
@Component
public class EmployeeValidator {

    // Allows letters (including Unicode for international names), spaces, hyphens, apostrophes.
    // Rejects digits and special characters that have no place in a person's name.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '\\-]+$");

    // Detects HTML/XML tags — catches stored XSS attempts in any string field.
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_JOB_TITLE_LENGTH = 150;
    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321 maximum
    private static final int MAX_SALARY = 10_000_000;
    private static final int MAX_AGE = 100;

    /**
     * Validates all fields in the given request, collecting every violation before throwing.
     *
     * The caller sees all problems at once rather than fixing them one at a time.
     *
     * @param request the request to validate; assumed non-null (enforced upstream by @Valid)
     * @throws ResponseStatusException 400 if any field fails validation
     */
    public void validate(CreateEmployeeRequest request) {
        List<String> violations = new ArrayList<>();

        validateName("firstName", request.getFirstName(), violations);
        validateName("lastName", request.getLastName(), violations);
        validateJobTitle(request.getJobTitle(), violations);
        validateEmail(request.getEmail(), violations);
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
     * Validates the jobTitle field.
     *
     * Job titles allow a broader character set than names but must not contain HTML or null bytes.
     */
    private void validateJobTitle(String value, List<String> violations) {
        if (value == null) return;

        if (value.length() > MAX_JOB_TITLE_LENGTH) {
            violations.add("jobTitle must not exceed " + MAX_JOB_TITLE_LENGTH + " characters");
        }
        checkInjection("jobTitle", value, violations);
    }

    /**
     * Validates the email field.
     *
     * Format is enforced upstream by {@code @Email}. This method caps length per RFC 5321
     * and checks for injection payloads.
     */
    private void validateEmail(String value, List<String> violations) {
        if (value == null) return;

        if (value.length() > MAX_EMAIL_LENGTH) {
            violations.add("email must not exceed " + MAX_EMAIL_LENGTH + " characters");
        }
        checkInjection("email", value, violations);
    }

    /**
     * Validates that salary does not exceed the upper business limit.
     *
     * The lower bound (>= 0) is enforced upstream by {@code @Min(0)}.
     */
    private void validateSalary(Integer value, List<String> violations) {
        if (value == null) return;

        if (value > MAX_SALARY) {
            violations.add("salary must not exceed " + MAX_SALARY);
        }
    }

    /**
     * Validates that age falls within a realistic working-age range.
     *
     * The lower bound (>= 16) is enforced upstream by {@code @Min(16)}.
     */
    private void validateAge(Integer value, List<String> violations) {
        if (value == null) return;

        if (value > MAX_AGE) {
            violations.add("age must not exceed " + MAX_AGE);
        }

        if (value <= 0) {
            violations.add("age must be a positive value");
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
