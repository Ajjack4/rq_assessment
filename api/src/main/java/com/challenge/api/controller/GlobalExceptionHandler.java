package com.challenge.api.controller;

import com.challenge.api.model.ErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralized exception handler for all REST controllers.
 *
 * Converts exceptions into a consistent {@link ErrorResponse} payload so individual
 * controllers never need their own error-handling logic. Any controller that uses
 * {@code @Valid} automatically gets a structured 400 response through this handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation failures from {@code @Valid @RequestBody} parameters.
     *
     * Collects all field violations into a single response so the caller sees every
     * problem at once rather than one error per request.
     *
     * @param ex the validation exception containing one entry per violated constraint
     * @return 400 with a list of human-readable violation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " " + fieldError.getDefaultMessage())
                .toList();
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), errors);
    }

    /**
     * Handles type conversion failures on path variables and request parameters.
     *
     * Catches malformed UUIDs in {@code GET /api/v1/employee/{uuid}} before the
     * request reaches the service, returning a descriptive 400 instead of a 500.
     *
     * @param ex the exception carrying the rejected value and expected type
     * @return 400 with a message identifying the invalid parameter and its value
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message =
                String.format("'%s' is not a valid %s for parameter '%s'", ex.getValue(), expected, ex.getName());
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), List.of(message));
    }

    /**
     * Handles {@link ResponseStatusException} thrown by the service layer.
     *
     * Preserves the HTTP status code embedded in the exception so service code can
     * signal domain errors (e.g. 404) without depending on HTTP types directly.
     *
     * @param ex the exception carrying the intended status and reason
     * @return the embedded status with the reason as the error message
     */
    @ExceptionHandler(ResponseStatusException.class)
    @ResponseBody
    public ErrorResponse handleResponseStatusException(ResponseStatusException ex) {
        String reason =
                ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return new ErrorResponse(ex.getStatusCode().value(), List.of(reason));
    }
}
