package com.pesonary.ewalletapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

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
}
