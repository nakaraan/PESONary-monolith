package com.pesonary.ewalletapi.entity;

/**
 * Lifecycle state of a wallet. Stored as text ({@code EnumType.STRING}) in {@code accounts.status}.
 * <p>Only {@code ACTIVE} wallets can send or receive money; {@code TransactionService} refuses {@code FROZEN} and {@code CLOSED} wallets.
 *
 * @author Akaraan
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
