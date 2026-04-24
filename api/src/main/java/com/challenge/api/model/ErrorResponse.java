package com.challenge.api.model;

import java.util.List;

/**
 * Uniform error payload returned for all API error responses.
 *
 * Declared as a record so fields are immutable and constructor/accessor
 * boilerplate is generated automatically.
 *
 * Example response body:
 * <pre>{@code
 * {
 *   "status": 400,
 *   "errors": ["firstName must not be blank", "salary must be greater than or equal to 0"]
 * }
 * }</pre>
 *
 * @param status the HTTP status code
 * @param errors one or more descriptions of what went wrong
 */
public record ErrorResponse(int status, List<String> errors) {}
