package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.dto.ApiResponse;
import com.pesonary.ewalletapi.dto.LoginDTO;
import com.pesonary.ewalletapi.dto.RegisterDTO;
import com.pesonary.ewalletapi.entity.Customer;
import com.pesonary.ewalletapi.entity.User;
import com.pesonary.ewalletapi.repository.CustomerRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import com.pesonary.ewalletapi.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for registration and login under {@code /api/v1/auth}.
 * <p>Registration creates a {@code User} and its {@code Customer} profile in one save. Login checks the password through {@code AuthenticationManager} and returns a JWT in {@code data}.
 *
 * @author Akaraan
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the controller.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to handle requests.
     *
     * @param authenticationManager checks credentials at login
     * @param jwtUtil creates tokens
     * @param userRepository data access for users
     * @param customerRepository data access for customers, used for the duplicate-email check
     * @param passwordEncoder hashes passwords
     */
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserRepository userRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new customer user. {@code POST /api/v1/auth/register}
     * <p><b>Pre-conditions:</b> the body passes validation.
     * <p><b>Post-conditions:</b> a user with role {@code ROLE_CUSTOMER} and its customer profile are saved; the password is stored as a BCrypt hash.
     *
     * @param dto username, password and customer profile
     * @return 201 response, or 409 if the username or email is already used
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, "Username already exists", null));
        }
        if (customerRepository.existsByEmail(dto.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(409, "Email already exists", null));
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // store the BCrypt hash, never plain text
        user.setRole("ROLE_CUSTOMER"); // admins are promoted in MySQL

        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        user.setCustomer(customer); // link both ways; ownership checks walk customer -> user
        customer.setUser(user);
        userRepository.save(user);      // cascade = ALL saves the customer too

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "User registered successfully", null));
    }

    /**
     * Logs a user in and returns a token. {@code POST /api/v1/auth/login}
     * <p><b>Pre-conditions:</b> the body passes validation.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param dto username and password
     * @return 200 response whose {@code data} is the JWT
     * @throws BadCredentialsException if the username or password is wrong (mapped to 401)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginDTO dto) {
        // Throws BadCredentialsException on a wrong password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        String token = jwtUtil.generateToken(dto.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", token));
    }
}