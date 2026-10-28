package com.pesonary.ewalletapi.service;

import com.pesonary.ewalletapi.dto.TransactionDTO;
import com.pesonary.ewalletapi.entity.Account;
import com.pesonary.ewalletapi.entity.AccountStatus;
import com.pesonary.ewalletapi.entity.Transaction;
import com.pesonary.ewalletapi.entity.TransactionType;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.repository.AccountRepository;
import com.pesonary.ewalletapi.repository.TransactionRepository;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Business logic for the ledger: deposits, withdrawals and transfers, plus reading and correcting records.
 * <p>{@code createTransaction} is one {@code @Transactional} unit: the debit, the credit and the insert all happen or none do. Balances are written by dirty checking at commit, so the method never saves an account directly.
 * <p>Rules: a wallet must be {@code ACTIVE}, cannot go below zero, cannot exceed its tier's cap, and cannot transfer to itself.
 * <p>Every public method takes and returns DTOs; entities never leave this class. Access rules sit on each method ({@code @PreAuthorize}, {@code @PostAuthorize}). Missing records throw {@link ResourceNotFoundException}; refused rules throw {@link BusinessRuleException}.
 *
 * @author Akaraan
 */
@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    /**
     * Creates the service.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param transactionRepository data access for transactions
     * @param accountRepository data access for wallets, used to load and update balances
     */
    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Records a deposit, withdrawal or transfer and moves the money.
     * <p><b>Pre-conditions:</b> {@code dto} passed validation; the caller is an admin or owns the source wallet.
     * <p><b>Post-conditions:</b> on success, balances are updated and one transaction is saved with a reference number. On any exception nothing is saved and both balances stay as they were (rollback).
     *
     * @param dto type, amount, source {@code accountId}, and {@code counterpartyAccountId} for transfers
     * @return TransactionDTO object containing the saved transaction and its {@code referenceNo}
     * @throws ResourceNotFoundException if the source or target wallet does not exist
     * @throws BusinessRuleException if funds are insufficient, a wallet is not active, a tier cap would be exceeded, or the transfer target is missing or is the source
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(#dto.accountId, authentication.name)")
    public TransactionDTO createTransaction(TransactionDTO dto) {
        Account source = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        requireActive(source); // frozen or closed wallets cannot move money
        TransactionType type = TransactionType.valueOf(dto.getType().toUpperCase());

        // apply the money movement; any exception rolls back the whole method
        switch (type) {
            case DEPOSIT -> credit(source, dto.getAmount());
            case WITHDRAWAL -> debit(source, dto.getAmount());
            case TRANSFER -> {
                if (dto.getCounterpartyAccountId() == null) {
                    throw new BusinessRuleException("Target account is required");
                }
                if (dto.getCounterpartyAccountId().equals(source.getId())) {
                    throw new BusinessRuleException("Cannot transfer to the same account");
                }
                Account target = accountRepository.findById(dto.getCounterpartyAccountId())
                        .orElseThrow(() -> new ResourceNotFoundException("Target account not found"));
                requireActive(target);
                debit(source, dto.getAmount());  // debit first; a failed credit still rolls this back
                credit(target, dto.getAmount());
            }
        }

        // record the ledger entry; referenceNo is generated on insert
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(dto.getAmount());
        tx.setDescription(dto.getDescription());
        tx.setAccount(source);
        tx.setCounterpartyAccountId(dto.getCounterpartyAccountId());
        return convertToDTO(transactionRepository.save(tx));
    }

    /**
     * Takes money from a wallet.
     * <p><b>Pre-conditions:</b> {@code amount} is positive.
     * <p><b>Post-conditions:</b> the balance is reduced by {@code amount}.
     *
     * @param account wallet to debit
     * @param amount amount to take
     * @throws BusinessRuleException if the balance is less than {@code amount}
     */
    private void debit(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
    }

    /**
     * Adds money to a wallet.
     * <p><b>Pre-conditions:</b> {@code amount} is positive.
     * <p><b>Post-conditions:</b> the balance is increased by {@code amount}.
     *
     * @param account wallet to credit
     * @param amount amount to add
     * @throws BusinessRuleException if the new balance would exceed the wallet's tier cap
     */
    private void credit(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(account.getTier().getMaxBalance()) > 0) {
            throw new BusinessRuleException("Wallet limit exceeded for the " + account.getTier() + " tier");
        }
        account.setBalance(newBalance);
    }

    /**
     * Refuses wallets that are frozen or closed.
     * <p><b>Pre-conditions:</b> {@code account} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param account wallet to check
     * @throws BusinessRuleException if the wallet is not {@code ACTIVE}
     */
    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account is not active");
        }
    }

    /**
     * Lists a wallet's transactions, newest first.
     * <p><b>Pre-conditions:</b> {@code accountId} is not null; the caller is an admin or owns the wallet.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param accountId ID of the source wallet
     * @return {@code List} of transactions; empty if none
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(#accountId, authentication.name)")
    public List<TransactionDTO> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Returns one transaction by ID.
     * <p><b>Pre-conditions:</b> {@code id} is not null; the caller is an admin or owns the source wallet (checked on the result by {@code @PostAuthorize}).
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id transaction's database ID
     * @return TransactionDTO object containing the transaction
     * @throws ResourceNotFoundException if no transaction has this ID
     */
    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(returnObject.accountId, authentication.name)")
    public TransactionDTO getTransactionById(Long id) {
        return convertToDTO(findOrThrow(id));
    }

    /**
     * Corrects a transaction's description.
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> only the description changes; amount, type and wallets are never edited.
     *
     * @param id transaction's database ID
     * @param dto holds the new description
     * @return TransactionDTO object containing the updated transaction
     * @throws ResourceNotFoundException if no transaction has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        Transaction tx = findOrThrow(id);
        tx.setDescription(dto.getDescription());
        return convertToDTO(transactionRepository.save(tx));
    }

    /**
     * Deletes a transaction record (admin correction).
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the record is deleted; balances are not reversed.
     *
     * @param id transaction's database ID
     * @throws ResourceNotFoundException if no transaction has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTransaction(Long id) {
        // shortcut: does not reverse balances, add a reversing entry before real money moves
        transactionRepository.delete(findOrThrow(id));
    }

    /**
     * Loads a transaction or throws.
     * <p><b>Pre-conditions:</b> {@code id} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id transaction's database ID
     * @return Transaction entity
     * @throws ResourceNotFoundException if no transaction has this ID
     */
    private Transaction findOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }


    /**
     * Maps a transaction entity to its DTO.
     * <p><b>Pre-conditions:</b> {@code transaction} is not null; called inside a transaction, because {@code account} is loaded lazily.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param transaction transaction entity
     * @return TransactionDTO object containing the transaction and its source wallet ID
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setType(transaction.getType().name());
        dto.setAmount(transaction.getAmount());
        dto.setAccountId(transaction.getAccount().getId());
        dto.setCounterpartyAccountId(transaction.getCounterpartyAccountId());
        dto.setDescription(transaction.getDescription());
        dto.setReferenceNo(transaction.getReferenceNo());
        dto.setCreatedAt(transaction.getCreatedAt());
        return dto;
    }
}