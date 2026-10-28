package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.Account;
import com.pesonary.ewalletapi.entity.AccountStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByStatus(AccountStatus status);
    List<Account> findByCustomerId(Long customerId);
}
