package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.repository.AccountRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ownership checks used in method-security expressions as {@code @accountSecurity}.
 * <p>Answers "does this logged-in user own this wallet or customer profile?" by walking {@code Account -> Customer -> User}. Returns {@code false} instead of throwing, so a failed check becomes a 403.
 *
 * @author Akaraan
 */
@Component("accountSecurity")
public class AccountSecurity {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;

    /**
     * Creates the checker.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param userRepository data access for users
     * @param accountRepository data access for wallets
     */
    public AccountSecurity(UserRepository userRepository, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Checks if a wallet belongs to the given user.
     * <p><b>Pre-conditions:</b> called from a method-security expression; either argument may be null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param accountId wallet ID to check
     * @param username logged-in username ({@code authentication.name})
     * @return {@code true} only if the wallet exists and its customer's user has this username
     */
    @Transactional(readOnly = true)
    public boolean isOwnAccount(Long accountId, String username) {
        if (accountId == null || username == null) return false;
        return accountRepository.findById(accountId)
                .map(a -> a.getCustomer() != null && a.getCustomer().getUser() != null
                        && username.equals(a.getCustomer().getUser().getUsername()))
                .orElse(false);
    }

    /**
     * Checks if a customer profile belongs to the given user.
     * <p><b>Pre-conditions:</b> called from a method-security expression; either argument may be null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param customerId customer ID to check
     * @param username logged-in username ({@code authentication.name})
     * @return {@code true} only if the user exists and is linked to this customer
     */
    @Transactional(readOnly = true)
    public boolean isOwnCustomer(Long customerId, String username) {
        if (customerId == null || username == null) return false;
        return userRepository.findByUsername(username)
                .map(u -> u.getCustomer() != null && customerId.equals(u.getCustomer().getId()))
                .orElse(false);
    }
}