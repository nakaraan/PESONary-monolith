package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class AccountDTO {
    private Long id;

    @NotBlank (message = "Account type is required")
    private String accountType;

    @NotNull (message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance cannot be negative")
    private BigDecimal balance;

    @Pattern(regexp = "(?i)ACTIVE|FROZEN|CLOSED", message = "Status must be ACTIVE, FROZEN or CLOSED")
    private String status;

    @Pattern(regexp = "(?i)BASIC|VERIFIED", message = "Tier must be BASIC or VERIFIED")
    private String tier;

    private Long customerId;
    private String customerName;
    private String username;
}
