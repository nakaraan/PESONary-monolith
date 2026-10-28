package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository <Customer, Long> {
    boolean existsByEmail(String email);
}
