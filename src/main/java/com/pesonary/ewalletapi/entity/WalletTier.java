package com.pesonary.ewalletapi.entity;

import java.math.BigDecimal;

/**
 * KYC tier of a wallet. Each tier caps how much the wallet can hold.
 * <p>{@code BASIC} is the default for new wallets; an admin upgrades a wallet to {@code VERIFIED} through {@code PUT /api/v1/accounts/{id}}. The cap lives on the enum, so adding a tier is one constant.
 *
 * @author Akaraan
 */
public enum WalletTier {
    BASIC(new BigDecimal("10000.00")),     // default for new wallets
    VERIFIED(new BigDecimal("500000.00")); // set by an admin after verification

    private final BigDecimal maxBalance;

    /**
     * Creates a tier with its balance cap.
     * <p><b>Pre-conditions:</b> {@code maxBalance} is positive.
     * <p><b>Post-conditions:</b> the cap is fixed for the life of the program.
     *
     * @param maxBalance highest balance a wallet of this tier can hold
     */
    WalletTier(BigDecimal maxBalance) {
        this.maxBalance = maxBalance;
    }

    /**
     * Returns the balance cap of this tier.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return BigDecimal containing the maximum balance
     */
    public BigDecimal getMaxBalance() {
        return maxBalance;
    }
}
