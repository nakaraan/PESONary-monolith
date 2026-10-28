package com.pesonary.ewalletapi.repository;

import com.pesonary.ewalletapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data access for {@link User}. CRUD methods come from {@code JpaRepository}.
 *
 * @author Akaraan
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Finds a user by username.
     * <p><b>Pre-conditions:</b> {@code username} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param username username to look up
     * @return {@code Optional} containing the user, or empty if none
     */
    Optional<User> findByUsername(String username);
    /**
     * Checks if a username is already taken.
     * <p><b>Pre-conditions:</b> {@code username} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param username username to look up
     * @return {@code true} if a user has this username
     */
    boolean existsByUsername(String username);
}