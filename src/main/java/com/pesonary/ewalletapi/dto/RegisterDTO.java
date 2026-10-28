package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Sign-up data for {@code POST /api/v1/auth/register}: login credentials plus the customer profile.
 * <p>{@code password} must be 8 to 72 characters (BCrypt reads at most 72 bytes). {@code phone} is optional but must follow {@code 09XXXXXXXXX} or {@code +639XXXXXXXXX}.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
public class RegisterDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")  // BCrypt ignores/rejects past 72 bytes
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^(09|\\+639)\\d{9}$", message = "Phone format must follow 09XXXXXXXXX or +639XXXXXXXXX")
    private String phone;

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
     * Returns the plain-text password (hashed before saving).
     *
     * @return plain-text password (hashed before saving)
     */
    public String getPassword() { return password; }
    /**
     * Sets the plain-text password (hashed before saving).
     *
     * @param password new plain-text password (hashed before saving)
     */
    public void setPassword(String password) { this.password = password; }
    /**
     * Returns the customer's full name.
     *
     * @return customer's full name
     */
    public String getFullName() { return fullName; }
    /**
     * Sets the customer's full name.
     *
     * @param fullName new customer's full name
     */
    public void setFullName(String fullName) { this.fullName = fullName; }
    /**
     * Returns the customer's email address.
     *
     * @return customer's email address
     */
    public String getEmail() { return email; }
    /**
     * Sets the customer's email address.
     *
     * @param email new customer's email address
     */
    public void setEmail(String email) { this.email = email; }
    /**
     * Returns the customer's mobile number.
     *
     * @return customer's mobile number
     */
    public String getPhone() { return phone; }
    /**
     * Sets the customer's mobile number.
     *
     * @param phone new customer's mobile number
     */
    public void setPhone(String phone) { this.phone = phone; }
}