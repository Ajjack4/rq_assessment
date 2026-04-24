package com.challenge.api.controller;

import com.challenge.api.model.ErrorResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Handles exceptions for all controllers in one place.
 * Keeps controllers clean — no try/catch needed there.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Fired when @Valid fails on a request body.
     * Collects all field errors into one response so the caller sees everything at once.
     *
     * @param ex the validation exception
     * @return 400 with all field violations
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
     * Fired when a path variable can't be converted — e.g. a malformed UUID.
     * Returns 400 instead of letting Spring throw a 500.
     *
     * @param ex the type mismatch exception
     * @return 400 with a message describing the bad value
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
     * Handles errors thrown by the service layer, like 404 when an employee isn't found.
     *
     * @param ex the exception with the status and reason
     * @return error response with the embedded status code
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException ex) {
        String reason =
                ex.getReason() != null ? ex.getReason() : ex.getStatusCode().toString();
        return ResponseEntity.status(ex.getStatusCode())
                .body(new ErrorResponse(ex.getStatusCode().value(), List.of(reason)));
    }
}
