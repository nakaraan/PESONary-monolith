package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/**
 * Wallet data sent to and from the API.
 * <p>{@code status} and {@code tier} are Strings here and enums in {@code Account}; the service converts them. {@code username} is output only: {@code @PostAuthorize} on {@code AccountService.getAccountById} compares it with the logged-in user to decide ownership.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
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

    /**
     * Returns the wallet's database ID.
     *
     * @return wallet's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the wallet's database ID.
     *
     * @param id new wallet's database ID
     */
    public void setId(Long id) {
        this.id = id;
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
     * Returns the status (ACTIVE, FROZEN or CLOSED).
     *
     * @return status (ACTIVE, FROZEN or CLOSED)
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status (ACTIVE, FROZEN or CLOSED).
     *
     * @param status new status (ACTIVE, FROZEN or CLOSED)
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns the tier (BASIC or VERIFIED).
     *
     * @return tier (BASIC or VERIFIED)
     */
    public String getTier() {
        return tier;
    }

    /**
     * Sets the tier (BASIC or VERIFIED).
     *
     * @param tier new tier (BASIC or VERIFIED)
     */
    public void setTier(String tier) {
        this.tier = tier;
    }

    /**
     * Returns the owner's customer ID.
     *
     * @return owner's customer ID
     */
    public Long getCustomerId() {
        return customerId;
    }

    /**
     * Sets the owner's customer ID.
     *
     * @param customerId new owner's customer ID
     */
    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    /**
     * Returns the owner's full name.
     *
     * @return owner's full name
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * Sets the owner's full name.
     *
     * @param customerName new owner's full name
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * Returns the owner's username.
     *
     * @return owner's username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the owner's username.
     *
     * @param username new owner's username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
