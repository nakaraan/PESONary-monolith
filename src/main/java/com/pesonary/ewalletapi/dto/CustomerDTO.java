package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Customer data sent to and from the API. Holds IDs, never entities.
 * <p>Validated with {@code @Valid} on POST and PUT: {@code fullName} and {@code email} are required, and {@code phone} must be {@code 09XXXXXXXXX} or {@code +639XXXXXXXXX}. {@code username} is output only.
 * <p>Getters and setters have no pre-conditions; getters change nothing, setters replace only their own field.
 *
 * @author Akaraan
 */
public class CustomerDTO {
    private Long id;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^(09|\\+639)\\d{9}$", message = "Phone format must follow 09XXXXXXXXX or +639XXXXXXXXX")
    private String phone;
    private String username;

    /**
     * Returns the customer's database ID.
     *
     * @return customer's database ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the customer's database ID.
     *
     * @param id new customer's database ID
     */
    public void setId(Long id) {
        this.id = id;
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
     * Returns the linked username.
     *
     * @return linked username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the linked username.
     *
     * @param username new linked username
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
