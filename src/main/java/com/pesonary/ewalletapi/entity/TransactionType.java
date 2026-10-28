package com.pesonary.ewalletapi.entity;

/**
 * Kind of money movement recorded in the ledger.
 * <p>{@code DEPOSIT} adds to the source wallet, {@code WITHDRAWAL} takes from it, and {@code TRANSFER} moves money from the source wallet to {@code counterpartyAccountId}.
 *
 * @author Akaraan
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER
}
