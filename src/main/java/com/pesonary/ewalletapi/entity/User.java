package com.pesonary.ewalletapi.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Login account. Implements {@code UserDetails} so Spring Security can authenticate it. Maps to the {@code users} table.
 * <p>{@code password} holds a BCrypt hash, never plain text. {@code role} is {@code ROLE_CUSTOMER} or {@code ROLE_ADMIN}. Owning side of the one-to-one with {@code Customer} ({@code customer_id}); {@code CascadeType.ALL} saves the customer profile together with the user on registration.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;           // BCrypt hash, never plain text

    @Column(nullable = false)
    private String role;               // "ROLE_CUSTOMER" or "ROLE_ADMIN"

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "customer_id")
    private Customer customer;         // owning side: users.customer_id

    /**
     * Creates an empty user. Required by JPA; {@code AuthController} fills the fields on registration.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> all fields are null.
     */
    public User() {
    }

    /**
     * Returns the user's role as a Spring Security authority.
     * <p><b>Pre-conditions:</b> {@code role} is set.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return {@code List} containing one {@code SimpleGrantedAuthority} for {@code role}
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    /**
     * Returns the unique username.
     *
     * @return unique username
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Returns the BCrypt-hashed password.
     *
     * @return BCrypt-hashed password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the linked customer profile.
     *
     * @return linked customer profile
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * Sets the linked customer profile.
     *
     * @param customer new linked customer profile
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * Returns the role (ROLE_CUSTOMER or ROLE_ADMIN).
     *
     * @return role (ROLE_CUSTOMER or ROLE_ADMIN)
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role (ROLE_CUSTOMER or ROLE_ADMIN).
     *
     * @param role new role (ROLE_CUSTOMER or ROLE_ADMIN)
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Sets the BCrypt-hashed password.
     *
     * @param password new BCrypt-hashed password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Sets the unique username.
     *
     * @param username new unique username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the database ID.
     *
     * @return database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database ID.
     *
     * @param id new database ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Accounts never expire in this application.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonExpired() { return true; }

    /**
     * Accounts are never locked in this application.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isAccountNonLocked() { return true; }

    /**
     * Passwords never expire in this application.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isCredentialsNonExpired() { return true; }

    /**
     * Every registered user is enabled.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> no state changes.
     *
     * @return always {@code true}
     */
    @Override
    public boolean isEnabled() { return true; }
}