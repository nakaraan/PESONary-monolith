package com.pesonary.ewalletapi.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A wallet. Maps to the {@code accounts} table and is the owning side of the customer relationship ({@code customer_id}).
 * <p>{@code balance} is a {@code BigDecimal} (precision 19, scale 2), so money is never rounded like a {@code double}. {@code version} turns on optimistic locking: two concurrent updates to the same wallet cannot both win. {@code tier} caps the balance (see {@link WalletTier}).
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String accountType;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletTier tier;

    private LocalDateTime createdAt, updatedAt;

    /**
     * Sets the timestamps before the first insert.
     * <p><b>Pre-conditions:</b> called by JPA before {@code persist}.
     * <p><b>Post-conditions:</b> {@code createdAt} and {@code updatedAt} hold the current time.
     */
    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
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
     * Returns the wallet balance.
     *
     * @return wallet balance
     */
    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * Sets the wallet balance.
     *
     * @param balance new wallet balance
     */
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /**
     * Returns the account type (e.g. SAVINGS).
     *
     * @return account type (e.g. SAVINGS)
     */
    public String getAccountType() {
        return accountType;
    }

    /**
     * Sets the account type (e.g. SAVINGS).
     *
     * @param accountType new account type (e.g. SAVINGS)
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    /**
     * Returns the wallet status.
     *
     * @return wallet status
     */
    public AccountStatus getStatus() {
        return status;
    }

    /**
     * Sets the wallet status.
     *
     * @param status new wallet status
     */
    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    /**
     * Returns the owning customer.
     *
     * @return owning customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Sets the owning customer.
     *
     * @param customer new owning customer
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Returns the optimistic-lock version.
     *
     * @return optimistic-lock version
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Returns the wallet tier.
     *
     * @return wallet tier
     */
    public WalletTier getTier() {
        return tier;
    }

    /**
     * Sets the wallet tier.
     *
     * @param tier new wallet tier
     */
    public void setTier(WalletTier tier) {
        this.tier = tier;
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
