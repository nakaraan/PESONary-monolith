package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.repository.AccountRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("accountSecurity")
public class AccountSecurity {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    public AccountSecurity(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public boolean isOwnAccount(Long accountId, String username) {
        if (accountId == null || username == null) return false;
        return accountRepository.findById(accountId)
                .map(a -> a.getCustomer() != null && a.getCustomer().getUser() != null
                        && username.equals(a.getCustomer().getUser().getUsername()))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isOwnCustomer(Long customerId, String username) {
        if (customerId == null || username == null) return false;
        return userRepository.findByUsername(username)
                .map(u -> u.getCustomer() != null && customerId.equals(u.getCustomer().getId()))
                .orElse(false);
    }
}