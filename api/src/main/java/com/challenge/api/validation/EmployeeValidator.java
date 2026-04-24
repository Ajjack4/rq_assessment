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
 * Validates employee request fields beyond what Jakarta annotations can do.
 * Collects all violations before throwing, so the caller sees everything wrong at once.
 * Inject this wherever employee input needs checking.
 */
@Component
public class EmployeeValidator {

    // Letters (Unicode), spaces, hyphens, apostrophes — covers international names.
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L} '\\-]+$");

    // Basic email check: needs a local part, @, domain, and a TLD of 2+ chars.
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    // Catches HTML/script tags — prevents stored XSS if data is ever rendered in a browser.
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 254; // RFC 5321
    private static final int MIN_SALARY = 0;
    private static final int MAX_SALARY = 10_000_000;
    private static final int MIN_AGE = 16;
    private static final int MAX_AGE = 100;

    /**
     * Runs all field validations. Throws 400 if anything fails.
     *
     * @param request the incoming create-employee request
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

    // Only letters, spaces, hyphens, apostrophes. Digits and special chars aren't valid in names.
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

    // @Email is lenient and can pass "user@domain" (no TLD), so we apply our own check too.
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

    // Must match one of the values in JobTitle. Case-insensitive.
    private void validateJobTitle(String value, List<String> violations) {
        if (value == null) return;
        checkInjection("jobTitle", value, violations);
        if (!JobTitle.isValid(value)) {
            violations.add("jobTitle must be one of: " + String.join(", ", JobTitle.validDisplayNames()));
        }
    }

    // @Min(0) on the DTO handles the lower bound too, but we check here as a safety net.
    private void validateSalary(Integer value, List<String> violations) {
        if (value == null) return;
        if (value < MIN_SALARY) {
            violations.add("salary must be " + MIN_SALARY + " or greater");
        }
        if (value > MAX_SALARY) {
            violations.add("salary must not exceed " + MAX_SALARY);
        }
    }

    // @Min(16) on the DTO handles the lower bound too, but we check here as a safety net.
    private void validateAge(Integer value, List<String> violations) {
        if (value == null) return;
        if (value < MIN_AGE) {
            violations.add("age must be at least " + MIN_AGE);
        }
        if (value > MAX_AGE) {
            violations.add("age must not exceed " + MAX_AGE);
        }
    }

    // Null bytes can bypass string filters. HTML tags are a stored XSS risk.
    private void checkInjection(String field, String value, List<String> violations) {
        if (value.contains("\0")) {
            violations.add(field + " contains invalid null byte characters");
        }
        if (HTML_TAG_PATTERN.matcher(value).find()) {
            violations.add(field + " must not contain HTML or script content");
        }
    }
}
