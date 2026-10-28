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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                          UserRepository userRepository, CustomerRepository customerRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

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
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole("ROLE_CUSTOMER");

        Customer customer = new Customer();
        customer.setFullName(dto.getFullName());
        customer.setEmail(dto.getEmail());
        customer.setPhone(dto.getPhone());

        user.setCustomer(customer);
        customer.setUser(user);
        userRepository.save(user);      // cascade = ALL saves the customer too

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(201, "User registered successfully", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@Valid @RequestBody LoginDTO dto) {
        // Throws BadCredentialsException on a wrong password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        String token = jwtUtil.generateToken(dto.getUsername());
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", token));
    }
}