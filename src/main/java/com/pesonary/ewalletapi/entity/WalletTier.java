package com.pesonary.ewalletapi.entity;

import java.math.BigDecimal;

public enum WalletTier {
    BASIC(new BigDecimal("10000.000")),
    VERIFIED(new BigDecimal("500000.00"));

    private final BigDecimal maxBalance;

    WalletTier(BigDecimal maxBalance) {
        this.maxBalance = maxBalance;
    }

    public BigDecimal getMaxBalance() {
        return maxBalance;
    }
}
