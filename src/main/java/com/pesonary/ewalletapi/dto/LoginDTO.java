package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Credentials for {@code POST /api/v1/auth/login}. Both fields are required.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
public class LoginDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    /**
     * Returns the username.
     *
     * @return username
     */
    public String getUsername() { return username; }
    /**
     * Sets the username.
     *
     * @param username new username
     */
    public void setUsername(String username) { this.username = username; }
    /**
     * Returns the plain-text password.
     *
     * @return plain-text password
     */
    public String getPassword() { return password; }
    /**
     * Sets the plain-text password.
     *
     * @param password new plain-text password
     */
    public void setPassword(String password) { this.password = password; }
}