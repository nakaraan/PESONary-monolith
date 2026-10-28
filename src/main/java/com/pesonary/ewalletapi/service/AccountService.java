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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for wallets: open, read, update and close.
 * <p>Every public method takes and returns DTOs; entities never leave this class. Access rules sit on each method ({@code @PreAuthorize}, {@code @PostAuthorize}). Missing records throw {@link ResourceNotFoundException}; refused rules throw {@link BusinessRuleException}.
 *
 * @author Akaraan
 */
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    /**
     * Creates the service.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param accountRepository data access for wallets
     * @param customerRepository data access for customers, used to attach the owner on create
     */
    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Opens a wallet for a customer.
     * <p><b>Pre-conditions:</b> {@code dto} passed validation; the caller is an admin or the customer named by {@code customerId}.
     * <p><b>Post-conditions:</b> a new wallet is saved; status defaults to {@code ACTIVE} and tier to {@code BASIC}.
     *
     * @param dto wallet type, opening balance, owner's {@code customerId}, and optional status and tier
     * @return AccountDTO object containing the saved wallet
     * @throws ResourceNotFoundException if the customer does not exist
     * @throws BusinessRuleException if the opening balance is above the tier's cap
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnCustomer(#dto.customerId, authentication.name)")
    public AccountDTO createAccount(AccountDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = new Account();

        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setCustomer(customer);
        account.setStatus(dto.getStatus() == null ? AccountStatus.ACTIVE : AccountStatus.valueOf(dto.getStatus().toUpperCase())); // default ACTIVE
        account.setTier(dto.getTier() == null ? WalletTier.BASIC : WalletTier.valueOf(dto.getTier().toUpperCase())); // default BASIC

        if (dto.getBalance().compareTo(account.getTier().getMaxBalance()) > 0) { // the tier cap applies to the opening balance too
            throw new BusinessRuleException("Opening balance exceeds " + account.getTier() + " limit");
        }

        return convertToDTO(accountRepository.save(account));
    }

    /**
     * Lists every wallet.
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @return {@code List} of every wallet; empty if none
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Lists wallets one page at a time.
     * <p><b>Pre-conditions:</b> the caller is an admin; {@code page} is 0 or more and {@code size} is 1 or more.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param page zero-based page number
     * @param size wallets per page
     * @return {@code Page} of wallets with paging details
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountDTO> getPaginatedAccounts(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, size)).map(this::convertToDTO);
    }

    /**
     * Lists wallets with a given status.
     * <p><b>Pre-conditions:</b> the caller is an admin; {@code status} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param status status name, any case
     * @return {@code List} of matching wallets; empty if none
     * @throws IllegalArgumentException if {@code status} is not ACTIVE, FROZEN or CLOSED
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountDTO> getAccountsByStatus(String status) {
        return accountRepository.findByStatus(AccountStatus.valueOf(status.toUpperCase())).stream()
                .map(this::convertToDTO)
                .toList();
    }

    /**
     * Returns one wallet by ID.
     * <p><b>Pre-conditions:</b> {@code id} is not null; the caller is an admin or the owner (checked on the result by {@code @PostAuthorize}).
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id wallet's database ID
     * @return AccountDTO object containing the wallet
     * @throws ResourceNotFoundException if no wallet has this ID
     */
    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public AccountDTO getAccountById(Long id) {
        return convertToDTO(findOrThrow(id));
    }

    /**
     * Updates a wallet. This is how an admin freezes, closes or verifies one.
     * <p><b>Pre-conditions:</b> {@code dto} passed validation; the caller is an admin.
     * <p><b>Post-conditions:</b> type and balance are replaced; status and tier change only when {@code dto} has them.
     *
     * @param id wallet's database ID
     * @param dto new values
     * @return AccountDTO object containing the updated wallet
     * @throws ResourceNotFoundException if no wallet has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public AccountDTO updateAccount(Long id, AccountDTO dto) {
        Account account = findOrThrow(id);
        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        if (dto.getStatus() != null) {
            account.setStatus(AccountStatus.valueOf(dto.getStatus().toUpperCase()));
        }
        if (dto.getTier() != null) {
            account.setTier(WalletTier.valueOf(dto.getTier().toUpperCase()));
        }
        return convertToDTO(accountRepository.save(account));
    }

    /**
     * Deletes a wallet.
     * <p><b>Pre-conditions:</b> the caller is an admin.
     * <p><b>Post-conditions:</b> the wallet is deleted; a wallet that still has transactions fails on the foreign key (409).
     *
     * @param id wallet's database ID
     * @throws ResourceNotFoundException if no wallet has this ID
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void closeAccount(Long id) {
        accountRepository.delete(findOrThrow(id));
    }

    /**
     * Loads a wallet or throws.
     * <p><b>Pre-conditions:</b> {@code id} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param id wallet's database ID
     * @return Account entity
     * @throws ResourceNotFoundException if no wallet has this ID
     */
    private Account findOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    /**
     * Maps a wallet entity to its DTO.
     * <p><b>Pre-conditions:</b> {@code account} is not null; called inside a transaction, because {@code customer} is loaded lazily.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param account wallet entity
     * @return AccountDTO object containing the wallet, its owner and the owner's username
     */
    private AccountDTO convertToDTO(Account account) {
        AccountDTO dto = new AccountDTO();
        dto.setId(account.getId());
        dto.setAccountType(account.getAccountType());
        dto.setBalance(account.getBalance());
        dto.setStatus(account.getStatus().name());
        dto.setTier(account.getTier().name());

        if (account.getCustomer() != null) {
            dto.setCustomerId(account.getCustomer().getId());
            dto.setCustomerName(account.getCustomer().getFullName());
            if (account.getCustomer().getUser() != null) { // username drives the @PostAuthorize ownership check
                dto.setUsername(account.getCustomer().getUser().getUsername());
            }
        }
        return dto;
    }
}
