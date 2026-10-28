package com.pesonary.ewalletapi.dto;

import java.time.LocalDateTime;

/**
 * Standard wrapper for every successful response: {@code status}, {@code message}, {@code data} and {@code timestamp}.
 * <p>Controllers return {@code ResponseEntity<ApiResponse<T>>}, so every endpoint has the same JSON shape. Errors use {@link com.pesonary.ewalletapi.exception.ErrorResponseDTO} instead.
 * <p>Getters have no pre-conditions and change nothing.
 *
 * @param <T> type of the payload in {@code data}
 * @author Akaraan
 */
public class ApiResponse <T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    /**
     * Creates a response with a payload.
     * <p><b>Pre-conditions:</b> {@code status} is a valid HTTP status code.
     * <p><b>Post-conditions:</b> {@code timestamp} holds the current time.
     *
     * @param status HTTP status code
     * @param message short description of the result
     * @param data payload; may be null
     */
    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * Creates a response without a payload, e.g. after a delete.
     * <p><b>Pre-conditions:</b> {@code status} is a valid HTTP status code.
     * <p><b>Post-conditions:</b> {@code data} is null; {@code timestamp} holds the current time.
     *
     * @param status HTTP status code
     * @param message short description of the result
     */
    public ApiResponse(int status, String message) {
        this(status, message, null);
    }

    /**
     * Returns the time the response was created.
     *
     * @return time the response was created
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Returns the response payload.
     *
     * @return response payload
     */
    public T getData() {
        return data;
    }

    /**
     * Returns the response message.
     *
     * @return response message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the HTTP status code.
     *
     * @return HTTP status code
     */
    public int getStatus() {
        return status;
    }
}
