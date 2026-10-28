package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.Account;
import com.pesonary.ewalletapi.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Data access for {@link Account}. CRUD methods come from {@code JpaRepository}.
 * <p>Derived query names must match field names: {@code findByCustomerId} walks {@code Account.customer.id}.
 *
 * @author Akaraan
 */
public interface AccountRepository extends JpaRepository<Account, Long> {
    /**
     * Finds every wallet with the given status.
     * <p><b>Pre-conditions:</b> {@code status} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param status wallet status to match
     * @return {@code List} of matching wallets; empty if none
     */
    List<Account> findByStatus(AccountStatus status);
    /**
     * Finds every wallet owned by a customer.
     * <p><b>Pre-conditions:</b> {@code customerId} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param customerId owner's customer ID
     * @return {@code List} of the customer's wallets; empty if none
     */
    List<Account> findByCustomerId(Long customerId);
}
