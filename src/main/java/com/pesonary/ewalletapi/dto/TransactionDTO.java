package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {
    private Long id;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "(?i)DEPOSIT|WITHDRAWAL|TRANSFER", message = "Type must be DEPOSIT, WITHDRAWAL, or TRANSFER")
    private String type;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Account ID is required")
    private Long accountId;

    private Long counterpartyAccountId;

    @Size(max = 255, message = "Description is too long")
    private String description;
    private String referenceNo;
    private LocalDateTime createdAt;
}
