package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads users for Spring Security, at login and for every request that carries a token.
 * <p>Used by {@code DaoAuthenticationProvider} (login) and {@code JwtAuthenticationFilter} (token requests).
 *
 * @author Akaraan
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Creates the service.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param userRepository data access for users
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by username.
     * <p><b>Pre-conditions:</b> {@code username} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param username username from the login form or the token subject
     * @return UserDetails object containing the user's password hash and role
     * @throws UsernameNotFoundException if no user has this username
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}