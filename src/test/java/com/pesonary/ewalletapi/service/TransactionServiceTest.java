package com.pesonary.ewalletapi.service;

import com.pesonary.ewalletapi.dto.TransactionDTO;
import com.pesonary.ewalletapi.entity.Account;
import com.pesonary.ewalletapi.entity.AccountStatus;
import com.pesonary.ewalletapi.entity.Transaction;
import com.pesonary.ewalletapi.entity.TransactionType;
import com.pesonary.ewalletapi.entity.WalletTier;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.repository.AccountRepository;
import com.pesonary.ewalletapi.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

/**
 * Unit tests with Mockito for {@link TransactionService}.
 * <p>Each test method is one documented case in {@code docs/PESONary Test Cases - Almin, Wesner III C..pdf} ({@code TC-TS-01} to {@code TC-TS-17}); the test method name is the trace key.
 * <p>Repositories are mocked, so there is no database or Spring context; method security does not run here.
 *
 * @author Akaraan
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @InjectMocks TransactionService transactionService;

    /**
     * Builds a BASIC wallet with a fixed ID for test setup.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param id wallet ID, set by reflection because the entity has no {@code setId}
     * @param balance starting balance as a decimal string
     * @param status wallet status
     * @return Account entity
     */
    private Account account(Long id, String balance, AccountStatus status) {
        Account a = new Account();
        ReflectionTestUtils.setField(a, "id", id); // no setId on the entity
        a.setBalance(new BigDecimal(balance));
        a.setStatus(status);
        a.setTier(WalletTier.BASIC);
        return a;
    }

    /**
     * Builds a transaction request from wallet 1.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param type     DEPOSIT, WITHDRAWAL or TRANSFER
     * @param targetId target wallet ID; null unless TRANSFER
     * @param amount   amount as a decimal string
     * @return TransactionDTO object
     */
    private TransactionDTO dto(String type, Long targetId, String amount) {
        TransactionDTO d = new TransactionDTO();
        d.setType(type);
        d.setAccountId(1L);
        d.setCounterpartyAccountId(targetId);
        d.setAmount(new BigDecimal(amount));
        return d;
    }

    /**
     * Builds a ₱100.00 DEPOSIT entry for test setup.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param owner source wallet
     * @return Transaction entity
     */
    private Transaction transaction(Account owner) {
        Transaction t = new Transaction();
        t.setType(TransactionType.DEPOSIT);
        t.setAmount(new BigDecimal("100.00"));
        t.setAccount(owner);
        return t;
    }

    // createTransaction
    /**
     * TC-TS-01 (Success): account 1 ACTIVE, PHP 100.00; DEPOSIT PHP 25.50.
     * <p>Expect: balance becomes PHP 125.50.
     */
    @Test
    void createTransaction_depositAddsToBalance() {
        Account source = account(1L, "100.00", AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        transactionService.createTransaction(dto("DEPOSIT", null, "25.50"));

        assertEquals(new BigDecimal("125.50"), source.getBalance());
    }

    /**
     * TC-TS-02 (Failure): balance PHP 50.00; WITHDRAWAL PHP 75.00.
     * <p>Expect: BusinessRuleException "Insufficient funds"; nothing saved.
     */
    @Test
    void createTransaction_withdrawalOverBalance_throws() {
        Account source = account(1L, "50.00", AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> transactionService.createTransaction(dto("WITHDRAWAL", null, "75.00")));

        assertEquals("Insufficient funds", ex.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    /**
     * TC-TS-03 (Success): source 1 PHP 500.00, target 2 PHP 100.00; TRANSFER PHP 200.00.
     * <p>Expect: source PHP 300.00, target PHP 300.00.
     */
    @Test
    void createTransaction_transferMovesMoney() {
        Account source = account(1L, "500.00", AccountStatus.ACTIVE);
        Account target = account(2L, "100.00", AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());

        transactionService.createTransaction(dto("TRANSFER", 2L, "200.00"));

        assertEquals(new BigDecimal("300.00"), source.getBalance());
        assertEquals(new BigDecimal("300.00"), target.getBalance());
    }

    /**
     * TC-TS-04 (Failure): target 2 is FROZEN; TRANSFER PHP 200.00.
     * <p>Expect: BusinessRuleException "Account is not active"; source stays PHP 500.00.
     */
    @Test
    void createTransaction_transferToFrozenAccount_throws() {
        Account source = account(1L, "500.00", AccountStatus.ACTIVE);
        Account target = account(2L, "100.00", AccountStatus.FROZEN);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(source));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(target));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> transactionService.createTransaction(dto("TRANSFER", 2L, "200.00")));

        assertEquals("Account is not active", ex.getMessage());
        assertEquals(new BigDecimal("500.00"), source.getBalance());
    }

    /**
     * TC-TS-05 (Edge): BASIC account at PHP 9,900.00; DEPOSIT PHP 200.00.
     * <p>Expect: BusinessRuleException "Wallet limit exceeded for the BASIC tier".
     */
    @Test
    void createTransaction_depositPastLimit_throws() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account(1L, "9900.00", AccountStatus.ACTIVE)));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> transactionService.createTransaction(dto("DEPOSIT", null, "200.00")));

        assertEquals("Wallet limit exceeded for the BASIC tier", ex.getMessage());
    }

    // getTransactionsByAccount
    /**
     * TC-TS-06 (Success): account 1 has 2 transactions.
     * <p>Expect: list of 2 transactions.
     */
    @Test
    void getTransactionsByAccount_returnsList() {
        Account owner = account(1L, "100.00", AccountStatus.ACTIVE);
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(transaction(owner), transaction(owner)));

        assertEquals(2, transactionService.getTransactionsByAccount(1L).size());
    }

    /**
     * TC-TS-07 (Edge): account 1 has no transactions.
     * <p>Expect: empty list.
     */
    @Test
    void getTransactionsByAccount_empty() {
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());

        assertTrue(transactionService.getTransactionsByAccount(1L).isEmpty());
    }

    /**
     * TC-TS-08 (Success): 1 DEPOSIT transaction.
     * <p>Expect: type "DEPOSIT".
     */
    @Test
    void getTransactionsByAccount_mapsType() {
        when(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(transaction(account(1L, "100.00", AccountStatus.ACTIVE))));

        assertEquals("DEPOSIT", transactionService.getTransactionsByAccount(1L).get(0).getType());
    }

    // getTransactionById
    /**
     * TC-TS-09 (Success): id 10 exists.
     * <p>Expect: type "DEPOSIT".
     */
    @Test
    void getTransactionById_found() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction(account(1L, "100.00", AccountStatus.ACTIVE))));

        assertEquals("DEPOSIT", transactionService.getTransactionById(10L).getType());
    }

    /**
     * TC-TS-10 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException "Transaction not found".
     */
    @Test
    void getTransactionById_notFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(99L));

        assertEquals("Transaction not found", ex.getMessage());
    }

    /**
     * TC-TS-11 (Edge): id 10 belongs to account 7.
     * <p>Expect: accountId 7.
     */
    @Test
    void getTransactionById_mapsAccountId() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction(account(7L, "100.00", AccountStatus.ACTIVE))));

        assertEquals(7L, transactionService.getTransactionById(10L).getAccountId());
    }

    // updateTransaction
    /**
     * TC-TS-12 (Success): id 10; description "new note".
     * <p>Expect: description "new note".
     */
    @Test
    void updateTransaction_changesDescription() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction(account(1L, "100.00", AccountStatus.ACTIVE))));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());
        TransactionDTO d = new TransactionDTO();
        d.setDescription("new note");

        assertEquals("new note", transactionService.updateTransaction(10L, d).getDescription());
    }

    /**
     * TC-TS-13 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException.
     */
    @Test
    void updateTransaction_notFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(99L, new TransactionDTO()));
    }

    /**
     * TC-TS-14 (Edge): id 10 (PHP 100.00); DTO amount PHP 999.00.
     * <p>Expect: amount stays PHP 100.00.
     */
    @Test
    void updateTransaction_ignoresAmountChange() {
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(transaction(account(1L, "100.00", AccountStatus.ACTIVE))));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(returnsFirstArg());
        TransactionDTO d = new TransactionDTO();
        d.setAmount(new BigDecimal("999.00"));

        assertEquals(new BigDecimal("100.00"), transactionService.updateTransaction(10L, d).getAmount());
    }

    // deleteTransaction
    /**
     * TC-TS-15 (Success): id 10 exists.
     * <p>Expect: repository delete called with that transaction.
     */
    @Test
    void deleteTransaction_deletes() {
        Transaction existing = transaction(account(1L, "100.00", AccountStatus.ACTIVE));
        when(transactionRepository.findById(10L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(10L);

        verify(transactionRepository).delete(existing);
    }

    /**
     * TC-TS-16 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException "Transaction not found".
     */
    @Test
    void deleteTransaction_notFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(99L));

        assertEquals("Transaction not found", ex.getMessage());
    }

    /**
     * TC-TS-17 (Edge): id 99 does not exist.
     * <p>Expect: delete is never called.
     */
    @Test
    void deleteTransaction_notFound_doesNotDelete() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L));

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }
}