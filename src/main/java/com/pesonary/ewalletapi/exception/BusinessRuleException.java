package com.pesonary.ewalletapi.exception;

/**
 * Thrown when a request is valid but a business rule refuses it: insufficient funds, an inactive wallet, a tier limit or a duplicate email.
 * <p>{@code GlobalExceptionHandler} maps it to HTTP 422.
 *
 * @author Akaraan
 */
public class BusinessRuleException extends RuntimeException{
    /**
     * Creates the exception with a message for the client.
     * <p><b>Pre-conditions:</b> {@code message} is short and safe to show the client.
     * <p><b>Post-conditions:</b> {@code getMessage()} returns {@code message}.
     *
     * @param message reason shown in the error response
     */
    public BusinessRuleException(String message) {
        super(message);
    }
}