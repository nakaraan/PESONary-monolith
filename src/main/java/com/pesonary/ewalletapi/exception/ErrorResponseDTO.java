package com.pesonary.ewalletapi.exception;

import java.time.LocalDateTime;

/**
 * JSON body of every error response: {@code timestamp}, {@code status}, {@code errorType} and {@code message}.
 * <p>Built by {@code GlobalExceptionHandler}, {@code CustomAuthenticationEntryPoint} and {@code CustomAccessDeniedHandler}. Read-only after construction; getters have no pre-conditions and change nothing.
 *
 * @author Akaraan
 */
public class ErrorResponseDTO {
    private LocalDateTime timestamp;
    private int status;
    private String errorType;
    private String message;

    /**
     * Creates an error body.
     * <p><b>Pre-conditions:</b> {@code status} is a valid HTTP error code.
     * <p><b>Post-conditions:</b> {@code timestamp} holds the current time.
     *
     * @param status HTTP status code
     * @param errorType error category, e.g. {@code "Not Found"}
     * @param message details for the client
     */
    public ErrorResponseDTO(int status, String errorType, String message) {
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Returns the time the error occurred.
     *
     * @return time the error occurred
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Returns the error category.
     *
     * @return error category
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Returns the error message.
     *
     * @return error message
     */
    public String getMessage() {
        return message;
    }
}
