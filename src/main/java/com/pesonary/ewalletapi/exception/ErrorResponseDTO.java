package com.pesonary.ewalletapi.exception;

import java.time.LocalDateTime;

public class ErrorResponseDTO {
    LocalDateTime timestamp;
    int status;
    String errorType;
    String message;

    public ErrorResponseDTO(int status, String errorType, String message) {
        this.status = status;
        this.errorType = errorType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }
}
