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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getCounterpartyAccountId() {
        return counterpartyAccountId;
    }

    public void setCounterpartyAccountId(Long counterpartyAccountId) {
        this.counterpartyAccountId = counterpartyAccountId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
