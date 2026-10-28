package com.pesonary.ewalletapi.exception;

/**
 * Thrown when a customer, wallet, transaction or user with the requested ID or username does not exist.
 * <p>{@code GlobalExceptionHandler} maps it to HTTP 404.
 *
 * @author Akaraan
 */
public class ResourceNotFoundException extends RuntimeException{
    /**
     * Creates the exception with a message for the client.
     * <p><b>Pre-conditions:</b> {@code message} is short and safe to show the client.
     * <p><b>Post-conditions:</b> {@code getMessage()} returns {@code message}.
     *
     * @param message reason shown in the error response
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
