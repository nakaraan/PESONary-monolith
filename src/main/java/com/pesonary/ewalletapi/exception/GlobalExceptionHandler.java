package com.pesonary.ewalletapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import java.util.stream.Collectors;

/**
 * Turns every exception thrown by a controller or service into one JSON error shape ({@link ErrorResponseDTO}).
 * <p>Spring picks the most specific handler, so {@code handleAll} only receives what no other handler matches. Controllers never catch exceptions themselves.
 * <p>Status map: 400 invalid input, 401 authentication, 403 method-security refusal, 404 not found, 409 database conflict, 422 business rule, 500 anything else.
 *
 * @author Akaraan
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles failed {@code @Valid} checks on request bodies.
     * <p><b>Pre-conditions:</b> a controller parameter annotated {@code @Valid} failed validation.
     * <p><b>Post-conditions:</b> the service is never called; no data changes.
     *
     * @param ex exception holding every field error
     * @return 400 response with an {@link ErrorResponseDTO} body whose {@code message} lists every field error
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        // join every field error so the client sees all problems at once
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Validation Failed", message));
    }

    /**
     * Handles request bodies that are not valid JSON.
     * <p><b>Pre-conditions:</b> the body could not be read.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 400 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleMalformedJson(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Bad Request", "Malformed JSON request"));
    }

    /**
     * Handles path or query values of the wrong type, e.g. {@code /accounts/abc}.
     * <p><b>Pre-conditions:</b> a parameter could not be converted.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 400 response with an {@link ErrorResponseDTO} body naming the bad parameter
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Bad Request", "Invalid value for " + ex.getName()));
    }

    /**
     * Handles bad input that got past validation, e.g. an unknown status value.
     * <p><b>Pre-conditions:</b> a service or Spring threw {@code IllegalArgumentException}.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 400 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponseDTO(400, "Bad Request", ex.getMessage()));
    }

    /**
     * Handles requests for a record that does not exist.
     * <p><b>Pre-conditions:</b> a service threw {@link ResourceNotFoundException}.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 404 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDTO(404, "Not Found", ex.getMessage()));
    }

    /**
     * Handles database conflicts, e.g. deleting a customer who still has wallets.
     * <p><b>Pre-conditions:</b> the database rejected a write.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 409 response with an {@link ErrorResponseDTO} body; the database message is never sent
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConflict(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDTO(409, "Conflict", "Request conflicts with existing data"));
    }

    /**
     * Handles requests refused by a business rule.
     * <p><b>Pre-conditions:</b> a service threw {@link BusinessRuleException}.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 422 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponseDTO(422, "Business Rule Violation", ex.getMessage()));
    }

    /**
     * Handles authentication failures other than a wrong password.
     * <p><b>Pre-conditions:</b> Spring Security threw {@code AuthenticationException}.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 401 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(401, "Unauthorized", "Authentication failed"));
    }

    /**
     * Handles a wrong username or password at login.
     * <p><b>Pre-conditions:</b> {@code AuthenticationManager} rejected the credentials.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param ex exception that was thrown
     * @return 401 response with an {@link ErrorResponseDTO} body saying {@code "Invalid username or password"}
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponseDTO(401, "Unauthorized", "Invalid username or password"));
    }

    /**
     * Handles {@code @PreAuthorize} and {@code @PostAuthorize} refusals.
     * <p><b>Pre-conditions:</b> a method-security rule refused the logged-in user.
     * <p><b>Post-conditions:</b> for {@code @PostAuthorize}, the method already ran but its result is not returned; no data changes.
     *
     * @param ex exception that was thrown
     * @return 403 response with an {@link ErrorResponseDTO} body
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDTO(403, "Forbidden", "You do not have permission to access this resource"));
    }

    /**
     * Handles every exception no other handler matches.
     * <p><b>Pre-conditions:</b> an unexpected exception reached the controller layer.
     * <p><b>Post-conditions:</b> unexpected exceptions are logged with their stack trace.
     *
     * @param ex exception that was thrown
     * @return 500 (or Spring's own status for web errors) response with an {@link ErrorResponseDTO} body; the stack trace is never sent
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAll(Exception ex) {
        if (ex instanceof ErrorResponse springError) { // Spring's own web errors (unknown route, wrong method); keep their status
            int code = springError.getStatusCode().value();
            return ResponseEntity.status(code)
                    .body(new ErrorResponseDTO(code, "Request Error", ex.getMessage()));
        }
        log.error("Unhandled exception", ex); // log the stack trace here, never send it to the client
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDTO(500, "Internal Server Error", "An unexpected error occurred"));
    }
}