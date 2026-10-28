package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Data access for {@link Customer}. CRUD methods come from {@code JpaRepository}.
 *
 * @author Akaraan
 */
public interface CustomerRepository extends JpaRepository <Customer, Long> {
    /**
     * Checks if an email is already used by a customer.
     * <p><b>Pre-conditions:</b> {@code email} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param email email address to look up
     * @return {@code true} if a customer has this email
     */
    boolean existsByEmail(String email);
}
