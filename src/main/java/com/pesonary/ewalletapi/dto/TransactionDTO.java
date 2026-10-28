package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction data sent to and from the API.
 * <p>{@code type} must be {@code DEPOSIT}, {@code WITHDRAWAL} or {@code TRANSFER} (any case). {@code counterpartyAccountId} is required only for {@code TRANSFER}; the service checks it. {@code referenceNo} and {@code createdAt} are output only.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
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

    /**
     * Returns the transaction's database ID.
     *
     * @return transaction's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the transaction's database ID.
     *
     * @param id new transaction's database ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the type (DEPOSIT, WITHDRAWAL or TRANSFER).
     *
     * @return type (DEPOSIT, WITHDRAWAL or TRANSFER)
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type (DEPOSIT, WITHDRAWAL or TRANSFER).
     *
     * @param type new type (DEPOSIT, WITHDRAWAL or TRANSFER)
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the amount.
     *
     * @return amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the amount.
     *
     * @param amount new amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Returns the source wallet ID.
     *
     * @return source wallet ID
     */
    public Long getAccountId() {
        return accountId;
    }

    /**
     * Sets the source wallet ID.
     *
     * @param accountId new source wallet ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    /**
     * Returns the target wallet ID for a transfer.
     *
     * @return target wallet ID for a transfer
     */
    public Long getCounterpartyAccountId() {
        return counterpartyAccountId;
    }

    /**
     * Sets the target wallet ID for a transfer.
     *
     * @param counterpartyAccountId new target wallet ID for a transfer
     */
    public void setCounterpartyAccountId(Long counterpartyAccountId) {
        this.counterpartyAccountId = counterpartyAccountId;
    }

    /**
     * Returns the description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     *
     * @param description new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the receipt reference number.
     *
     * @return receipt reference number
     */
    public String getReferenceNo() {
        return referenceNo;
    }

    /**
     * Sets the receipt reference number.
     *
     * @param referenceNo new receipt reference number
     */
    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt new creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
