package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for {@link Transaction}. CRUD methods come from {@code JpaRepository}.
 * <p>{@code findByAccountIdOrderByCreatedAtDesc} walks {@code Transaction.account.id} and sorts by {@code createdAt}.
 *
 * @author Akaraan
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Lists a wallet's transactions, newest first.
     * <p><b>Pre-conditions:</b> {@code accountId} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param accountId ID of the source wallet
     * @return {@code List} of transactions sorted by {@code createdAt}, descending; empty if none
     */
    List<Transaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
