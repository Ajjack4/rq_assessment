package com.challenge.api.model;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Accepted job titles for employee records.
 * The create-employee endpoint rejects anything not on this list.
 */
public enum JobTitle {
    SOFTWARE_ENGINEER("Software Engineer"),
    SENIOR_ENGINEER("Senior Engineer"),
    STAFF_ENGINEER("Staff Engineer"),
    BACKEND_ENGINEER("Backend Engineer"),
    FRONTEND_ENGINEER("Frontend Engineer"),
    FULLSTACK_ENGINEER("Full Stack Engineer"),
    DEVOPS_ENGINEER("DevOps Engineer"),
    SECURITY_ENGINEER("Security Engineer"),
    DATA_ENGINEER("Data Engineer"),
    DATA_SCIENTIST("Data Scientist"),
    QA_ENGINEER("QA Engineer"),
    SOLUTIONS_ARCHITECT("Solutions Architect"),
    TECHNICAL_LEAD("Technical Lead"),
    ENGINEERING_MANAGER("Engineering Manager"),
    PRODUCT_MANAGER("Product Manager"),
    PROJECT_MANAGER("Project Manager");

    private final String displayName;

    JobTitle(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns true if the given string matches any job title (case-insensitive).
     *
     * @param value the job title to check
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String value) {
        if (value == null) return false;
        for (JobTitle title : values()) {
            if (title.displayName.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * All accepted display names: used in validation error messages.
     *
     * @return set of all valid job title strings
     */
    public static Set<String> validDisplayNames() {
        return Arrays.stream(values()).map(JobTitle::getDisplayName).collect(Collectors.toSet());
    }
}
