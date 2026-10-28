package com.pesonary.ewalletapi.service;

import com.pesonary.ewalletapi.dto.AccountDTO;
import com.pesonary.ewalletapi.entity.Account;
import com.pesonary.ewalletapi.entity.AccountStatus;
import com.pesonary.ewalletapi.entity.Customer;
import com.pesonary.ewalletapi.entity.WalletTier;
import com.pesonary.ewalletapi.exception.BusinessRuleException;
import com.pesonary.ewalletapi.exception.ResourceNotFoundException;
import com.pesonary.ewalletapi.repository.AccountRepository;
import com.pesonary.ewalletapi.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

/**
 * Unit tests with Mockito for {@link AccountService}.
 * <p>Each test method is one documented case in {@code docs/TEST_CASES.docx} ({@code TC-AS-01} to {@code TC-AS-21}); the test method name is the trace key.
 * <p>Repositories are mocked, so there is no database or Spring context; method security does not run here.
 *
 * @author Akaraan
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock CustomerRepository customerRepository;
    @InjectMocks AccountService accountService;

    /**
     * Builds a BASIC SAVINGS wallet for test setup.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param balance starting balance as a decimal string
     * @param status wallet status
     * @return Account entity
     */
    private Account account(String balance, AccountStatus status) {
        Account a = new Account();
        a.setAccountType("SAVINGS");
        a.setBalance(new BigDecimal(balance));
        a.setStatus(status);
        a.setTier(WalletTier.BASIC);
        return a;
    }

    /**
     * Builds a SAVINGS wallet request owned by customer 1.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> returns a new object; nothing else changes.
     *
     * @param balance balance as a decimal string
     * @return AccountDTO object
     */
    private AccountDTO dto(String balance) {
        AccountDTO d = new AccountDTO();
        d.setAccountType("SAVINGS");
        d.setBalance(new BigDecimal(balance));
        d.setCustomerId(1L);
        return d;
    }

    // createAccount
    /**
     * TC-AS-01 (Success): customer 1 exists; DTO: SAVINGS, PHP 1,000.00, no status or tier.
     * <p>Expect: returns status ACTIVE and tier BASIC.
     */
    @Test
    void createAccount_defaultsToActiveBasic() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(accountRepository.save(any(Account.class))).thenAnswer(returnsFirstArg());

        AccountDTO result = accountService.createAccount(dto("1000.00"));

        assertEquals("ACTIVE", result.getStatus());
        assertEquals("BASIC", result.getTier());
    }

    /**
     * TC-AS-02 (Failure): customer 1 does not exist.
     * <p>Expect: ResourceNotFoundException "Customer not found".
     */
    @Test
    void createAccount_unknownCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.createAccount(dto("1000.00")));

        assertEquals("Customer not found", ex.getMessage());
    }

    /**
     * TC-AS-03 (Edge): opening balance PHP 10,000.01 (BASIC cap PHP 10,000.00).
     * <p>Expect: BusinessRuleException "Opening balance exceeds BASIC limit"; nothing saved.
     */
    @Test
    void createAccount_overBasicLimit() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> accountService.createAccount(dto("10000.01")));

        assertEquals("Opening balance exceeds BASIC limit", ex.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // getAllAccounts
    /**
     * TC-AS-04 (Success): repository holds 2 accounts.
     * <p>Expect: list of 2 accounts.
     */
    @Test
    void getAllAccounts_returnsAll() {
        when(accountRepository.findAll()).thenReturn(List.of(account("1.00", AccountStatus.ACTIVE), account("2.00", AccountStatus.ACTIVE)));

        assertEquals(2, accountService.getAllAccounts().size());
    }

    /**
     * TC-AS-05 (Edge): repository is empty.
     * <p>Expect: empty list.
     */
    @Test
    void getAllAccounts_empty() {
        when(accountRepository.findAll()).thenReturn(List.of());

        assertTrue(accountService.getAllAccounts().isEmpty());
    }

    /**
     * TC-AS-06 (Success): 1 account owned by customer "Alice".
     * <p>Expect: customerName is "Alice".
     */
    @Test
    void getAllAccounts_mapsCustomerName() {
        Customer owner = new Customer();
        owner.setFullName("Alice");
        Account a = account("1.00", AccountStatus.ACTIVE);
        a.setCustomer(owner);
        when(accountRepository.findAll()).thenReturn(List.of(a));

        assertEquals("Alice", accountService.getAllAccounts().get(0).getCustomerName());
    }

    // getPaginatedAccounts
    /**
     * TC-AS-07 (Success): page 0, size 5; page holds 1 account.
     * <p>Expect: page content has 1 account.
     */
    @Test
    void getPaginatedAccounts_returnsContent() {
        when(accountRepository.findAll(PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(account("1.00", AccountStatus.ACTIVE))));

        assertEquals(1, accountService.getPaginatedAccounts(0, 5).getContent().size());
    }

    /**
     * TC-AS-08 (Edge): page 0, size 5; empty page.
     * <p>Expect: empty page.
     */
    @Test
    void getPaginatedAccounts_empty() {
        when(accountRepository.findAll(PageRequest.of(0, 5))).thenReturn(Page.empty());

        assertTrue(accountService.getPaginatedAccounts(0, 5).isEmpty());
    }

    /**
     * TC-AS-09 (Success): page 2, size 10.
     * <p>Expect: repository queried with PageRequest.of(2, 10).
     */
    @Test
    void getPaginatedAccounts_usesGivenPageAndSize() {
        when(accountRepository.findAll(PageRequest.of(2, 10))).thenReturn(Page.empty());

        accountService.getPaginatedAccounts(2, 10);

        verify(accountRepository).findAll(PageRequest.of(2, 10));
    }

    // getAccountsByStatus
    /**
     * TC-AS-10 (Success): "ACTIVE"; 1 active account.
     * <p>Expect: list of 1 account.
     */
    @Test
    void getAccountsByStatus_active() {
        when(accountRepository.findByStatus(AccountStatus.ACTIVE)).thenReturn(List.of(account("1.00", AccountStatus.ACTIVE)));

        assertEquals(1, accountService.getAccountsByStatus("ACTIVE").size());
    }

    /**
     * TC-AS-11 (Failure): "BOGUS".
     * <p>Expect: IllegalArgumentException.
     */
    @Test
    void getAccountsByStatus_invalidStatus() {
        assertThrows(IllegalArgumentException.class, () -> accountService.getAccountsByStatus("BOGUS"));
    }

    /**
     * TC-AS-12 (Edge): "frozen" (lowercase).
     * <p>Expect: finds FROZEN accounts; status "FROZEN".
     */
    @Test
    void getAccountsByStatus_lowercaseWorks() {
        when(accountRepository.findByStatus(AccountStatus.FROZEN)).thenReturn(List.of(account("1.00", AccountStatus.FROZEN)));

        assertEquals("FROZEN", accountService.getAccountsByStatus("frozen").get(0).getStatus());
    }

    // getAccountById
    /**
     * TC-AS-13 (Success): id 1 exists.
     * <p>Expect: accountType "SAVINGS".
     */
    @Test
    void getAccountById_found() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account("1.00", AccountStatus.ACTIVE)));

        assertEquals("SAVINGS", accountService.getAccountById(1L).getAccountType());
    }

    /**
     * TC-AS-14 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException "Account not found".
     */
    @Test
    void getAccountById_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.getAccountById(99L));

        assertEquals("Account not found", ex.getMessage());
    }

    /**
     * TC-AS-15 (Edge): id 1 has no owning customer.
     * <p>Expect: customerId is null.
     */
    @Test
    void getAccountById_noCustomer_customerIdIsNull() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account("1.00", AccountStatus.ACTIVE)));

        assertNull(accountService.getAccountById(1L).getCustomerId());
    }

    // updateAccount
    /**
     * TC-AS-16 (Success): id 1; DTO: CHECKING, PHP 250.00.
     * <p>Expect: accountType CHECKING, balance 250.00.
     */
    @Test
    void updateAccount_changesTypeAndBalance() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account("1.00", AccountStatus.ACTIVE)));
        when(accountRepository.save(any(Account.class))).thenAnswer(returnsFirstArg());
        AccountDTO d = dto("250.00");
        d.setAccountType("CHECKING");

        AccountDTO result = accountService.updateAccount(1L, d);

        assertEquals("CHECKING", result.getAccountType());
        assertEquals(new BigDecimal("250.00"), result.getBalance());
    }

    /**
     * TC-AS-17 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException.
     */
    @Test
    void updateAccount_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(99L, dto("1.00")));
    }

    /**
     * TC-AS-18 (Edge): id 1 is FROZEN; DTO has no status.
     * <p>Expect: status stays FROZEN.
     */
    @Test
    void updateAccount_nullStatusKeepsOldStatus() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account("1.00", AccountStatus.FROZEN)));
        when(accountRepository.save(any(Account.class))).thenAnswer(returnsFirstArg());

        assertEquals("FROZEN", accountService.updateAccount(1L, dto("1.00")).getStatus());
    }

    // closeAccount
    /**
     * TC-AS-19 (Success): id 1 exists.
     * <p>Expect: repository delete called with that account.
     */
    @Test
    void closeAccount_deletes() {
        Account existing = account("1.00", AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(existing));

        accountService.closeAccount(1L);

        verify(accountRepository).delete(existing);
    }

    /**
     * TC-AS-20 (Failure): id 99 does not exist.
     * <p>Expect: ResourceNotFoundException "Account not found".
     */
    @Test
    void closeAccount_notFound() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> accountService.closeAccount(99L));

        assertEquals("Account not found", ex.getMessage());
    }

    /**
     * TC-AS-21 (Edge): id 99 does not exist.
     * <p>Expect: delete is never called.
     */
    @Test
    void closeAccount_notFound_doesNotDelete() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.closeAccount(99L));

        verify(accountRepository, never()).delete(any(Account.class));
    }
}