package com.challenge.api.model;

import java.util.List;

/**
 * Shape of every error response returned by this API.
 *
 * <pre>{@code
 * { "status": 400, "errors": ["firstName must not be blank"] }
 * }</pre>
 *
 * @param status HTTP status code
 * @param errors what went wrong
 */
public record ErrorResponse(int status, List<String> errors) {}
