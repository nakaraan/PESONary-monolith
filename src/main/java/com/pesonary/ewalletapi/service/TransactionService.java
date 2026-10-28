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

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public TransactionService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(#dto.accountId, authentication.name)")
    public TransactionDTO createTransaction(TransactionDTO dto) {
        Account source = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        requireActive(source);
        TransactionType type = TransactionType.valueOf(dto.getType().toUpperCase());

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
                debit(source, dto.getAmount());
                credit(target, dto.getAmount());
            }
        }

        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setAmount(dto.getAmount());
        tx.setDescription(dto.getDescription());
        tx.setAccount(source);
        tx.setCounterpartyAccountId(dto.getCounterpartyAccountId());
        return convertToDTO(transactionRepository.save(tx));
    }

    private void debit(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessRuleException("Insufficient funds");
        }
        account.setBalance(account.getBalance().subtract(amount));
    }

    private void credit(Account account, BigDecimal amount) {
        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(account.getTier().getMaxBalance()) > 0) {
            throw new BusinessRuleException("Wallet limit exceeded for the " + account.getTier() + " tier");
        }
        account.setBalance(newBalance);
    }

    private void requireActive(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessRuleException("Account is not active");
        }
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(#accountId, authentication.name)")
    public List<TransactionDTO> getTransactionsByAccount(Long accountId) {
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnAccount(returnObject.accountId, authentication.name)")
    public TransactionDTO getTransactionById(Long id) {
        return convertToDTO(findOrThrow(id));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public TransactionDTO updateTransaction(Long id, TransactionDTO dto) {
        Transaction tx = findOrThrow(id);
        tx.setDescription(dto.getDescription());
        return convertToDTO(transactionRepository.save(tx));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTransaction(Long id) {
        // shortcut: does not reverse balances, add a reversing entry before real money moves
        transactionRepository.delete(findOrThrow(id));
    }

    private Transaction findOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
    }


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