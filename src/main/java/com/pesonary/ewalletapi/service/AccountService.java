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

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @accountSecurity.isOwnCustomer(#dto.customerId, authentication.name)")
    public AccountDTO createAccount(AccountDTO dto) {
        Customer customer = customerRepository.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        Account account = new Account();

        account.setAccountType(dto.getAccountType());
        account.setBalance(dto.getBalance());
        account.setCustomer(customer);
        account.setStatus(dto.getStatus() == null ? AccountStatus.ACTIVE : AccountStatus.valueOf(dto.getStatus().toUpperCase()));
        account.setTier(dto.getTier() == null ? WalletTier.BASIC : WalletTier.valueOf(dto.getTier().toUpperCase()));

        if (dto.getBalance().compareTo(account.getTier().getMaxBalance()) > 0) {
            throw new BusinessRuleException("Opening balance exceeds " + account.getTier() + " limit");
        }

        return convertToDTO(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountDTO> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountDTO> getPaginatedAccounts(int page, int size) {
        return accountRepository.findAll(PageRequest.of(page, size)).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<AccountDTO> getAccountsByStatus(String status) {
        return accountRepository.findByStatus(AccountStatus.valueOf(status.toUpperCase())).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @PostAuthorize("hasRole('ADMIN') or returnObject.username == authentication.name")
    public AccountDTO getAccountById(Long id) {
        return convertToDTO(findOrThrow(id));
    }

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

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void closeAccount(Long id) {
        accountRepository.delete(findOrThrow(id));
    }

    private Account findOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

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
            if (account.getCustomer().getUser() != null) {
                dto.setUsername(account.getCustomer().getUser().getUsername());
            }
        }
        return dto;
    }
}
