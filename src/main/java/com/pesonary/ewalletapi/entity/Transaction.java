package com.pesonary.ewalletapi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One ledger entry: a deposit, withdrawal or transfer. Maps to the {@code transactions} table.
 * <p>{@code account} is the source wallet ({@code account_id}); for a transfer, {@code counterpartyAccountId} holds the target wallet's ID. {@code referenceNo} is generated on insert and cannot be updated.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    private Long counterpartyAccountId; // --> other account in the transaction

    @Column(nullable = false, unique = true, updatable = false)
    private String referenceNo;

    private LocalDateTime createdAt, updatedAt;

    /**
     * Sets the timestamps before the first insert. Also generates the reference number.
     * <p><b>Pre-conditions:</b> called by JPA before {@code persist}.
     * <p><b>Post-conditions:</b> {@code createdAt} and {@code updatedAt} hold the current time; {@code referenceNo} holds 8 random upper-case hexadecimal characters.
     */
    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
        referenceNo = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Refreshes {@code updatedAt} before every update.
     * <p><b>Pre-conditions:</b> called by JPA before an update is flushed.
     * <p><b>Post-conditions:</b> {@code updatedAt} holds the current time.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Returns the database ID.
     *
     * @return database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the transaction type.
     *
     * @return transaction type
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * Sets the transaction type.
     *
     * @param type new transaction type
     */
    public void setType(TransactionType type) {
        this.type = type;
    }

    /**
     * Returns the transaction amount.
     *
     * @return transaction amount
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Sets the transaction amount.
     *
     * @param amount new transaction amount
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
     * Returns the source wallet.
     *
     * @return source wallet
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Sets the source wallet.
     *
     * @param account new source wallet
     */
    public void setAccount(Account account) {
        this.account = account;
    }

    /**
     * Returns the ID of the other wallet in a transfer.
     *
     * @return ID of the other wallet in a transfer
     */
    public Long getCounterpartyAccountId() {
        return counterpartyAccountId;
    }

    /**
     * Sets the ID of the other wallet in a transfer.
     *
     * @param counterpartyAccountId new ID of the other wallet in a transfer
     */
    public void setCounterpartyAccountId(Long counterpartyAccountId) {
        this.counterpartyAccountId = counterpartyAccountId;
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
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last-update timestamp.
     *
     * @return last-update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
