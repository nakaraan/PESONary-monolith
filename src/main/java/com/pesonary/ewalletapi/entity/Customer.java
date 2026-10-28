package com.pesonary.ewalletapi.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer profile: the person who owns wallets. Maps to the {@code customers} table.
 * <p>Inverse side of both relationships: {@code user} is mapped by {@code User.customer} and {@code accounts} by {@code Account.customer}. There is no cascade on {@code accounts}, so deleting a customer who still has wallets fails on the foreign key (409) instead of deleting the wallets.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String email;
    private String phone;

    @OneToOne(mappedBy = "customer")
    private User user;

    @OneToMany(mappedBy = "customer")
    private List<Account> accounts = new ArrayList<>();


    private LocalDateTime createdAt, updatedAt;

    /**
     * Sets the timestamps before the first insert.
     * <p><b>Pre-conditions:</b> called by JPA before {@code persist}.
     * <p><b>Post-conditions:</b> {@code createdAt} and {@code updatedAt} hold the current time.
     */
    @PrePersist
    void onCreate() {
        createdAt = updatedAt = LocalDateTime.now();
    }

    /**
     * Refreshes {@code updatedAt} before every update.
     * <p><b>Pre-conditions:</b> called by JPA before an update is flushed.
     * <p><b>Post-conditions:</b> {@code updatedAt} holds the current time.
     */
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructor
    /**
     * Creates an empty customer. Required by JPA; services fill the fields through setters.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> all fields are null except {@code accounts}, which is an empty list.
     */
    public Customer() {
    }

    // Getters + Setters
    /**
     * Returns the database ID.
     *
     * @return database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the customer's full name.
     *
     * @return customer's full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Sets the customer's full name.
     *
     * @param fullName new customer's full name
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the customer's email address.
     *
     * @return customer's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the customer's email address.
     *
     * @param email new customer's email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the customer's mobile number.
     *
     * @return customer's mobile number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the customer's mobile number.
     *
     * @param phone new customer's mobile number
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the linked login account.
     *
     * @return linked login account
     */
    public User getUser() {
        return user;
    }

    /**
     * Sets the linked login account.
     *
     * @param user new linked login account
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns the wallets owned by this customer.
     *
     * @return wallets owned by this customer
     */
    public List<Account> getAccounts() {
        return accounts;
    }

    /**
     * Sets the wallets owned by this customer.
     *
     * @param accounts new wallets owned by this customer
     */
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the last-update timestamp.
     *
     * @return last-update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
