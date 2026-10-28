package com.pesonary.ewalletapi.controller;

import com.pesonary.ewalletapi.entity.User;
import com.pesonary.ewalletapi.repository.CustomerRepository;
import com.pesonary.ewalletapi.repository.UserRepository;
import com.pesonary.ewalletapi.security.CustomUserDetailsService;
import com.pesonary.ewalletapi.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice tests with {@code @WebMvcTest} and MockMvc for {@link AuthController}.
 * <p>Each test method is one documented case in {@code docs/PESONary Test Cases - Almin, Wesner III C..pdf} ({@code TC-AU-01} to {@code TC-AU-07}); the test method name is the trace key.
 * <p>Repositories, the encoder, the authentication manager and {@code JwtUtil} are mocked; filters are off ({@code addFilters = false}).
 *
 * @author Akaraan
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean AuthenticationManager authenticationManager;
    @MockitoBean JwtUtil jwtUtil;
    @MockitoBean UserRepository userRepository;
    @MockitoBean CustomerRepository customerRepository;
    @MockitoBean PasswordEncoder passwordEncoder;
    // JwtAuthenticationFilter is a Filter bean, so the slice tries to build it
    @MockitoBean CustomUserDetailsService customUserDetailsService;

    private static final String REGISTER_BODY = """
            {"username":"alice","password":"password123","fullName":"Alice Cruz","email":"alice@example.com","phone":"09171234567"}
            """;
    private static final String SHORT_PASSWORD_BODY = """
            {"username":"alice","password":"short","fullName":"Alice Cruz","email":"alice@example.com"}
            """;
    private static final String LOGIN_BODY = """
            {"username":"alice","password":"password123"}
            """;
    private static final String BLANK_PASSWORD_BODY = """
            {"username":"alice","password":""}
            """;

    // POST /auth/register
    /**
     * TC-AU-01 (Success): POST /api/v1/auth/register: alice, password123, Alice Cruz, alice@example.com.
     * <p>Expect: 201 "User registered successfully"; saved with role ROLE_CUSTOMER and an encoded password.
     */
    @Test
    void register_created() throws Exception {
        when(passwordEncoder.encode("password123")).thenReturn("ENCODED");

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("ROLE_CUSTOMER", captor.getValue().getRole());
        assertEquals("ENCODED", captor.getValue().getPassword());
    }

    /**
     * TC-AU-02 (Failure): username alice already exists.
     * <p>Expect: 409 "Username already exists"; nothing saved.
     */
    @Test
    void register_duplicateUsername() throws Exception {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TC-AU-03 (Failure): email alice@example.com already exists.
     * <p>Expect: 409 "Email already exists"; nothing saved.
     */
    @Test
    void register_duplicateEmail() throws Exception {
        when(customerRepository.existsByEmail("alice@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(REGISTER_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * TC-AU-04 (Edge): password "short" (under 8 characters).
     * <p>Expect: 400.
     */
    @Test
    void register_shortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(SHORT_PASSWORD_BODY))
                .andExpect(status().isBadRequest());
    }

    // POST /auth/login
    /**
     * TC-AU-05 (Success): POST /api/v1/auth/login: alice, password123.
     * <p>Expect: 200; data is the token.
     */
    @Test
    void login_returnsToken() throws Exception {
        when(jwtUtil.generateToken("alice")).thenReturn("jwt-token");

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("jwt-token"));
    }

    /**
     * TC-AU-06 (Failure): AuthenticationManager throws BadCredentialsException.
     * <p>Expect: 401 "Invalid username or password".
     */
    @Test
    void login_badCredentials() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(LOGIN_BODY))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    /**
     * TC-AU-07 (Edge): password "".
     * <p>Expect: 400; authenticate never called.
     */
    @Test
    void login_blankPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(BLANK_PASSWORD_BODY))
                .andExpect(status().isBadRequest());
        verify(authenticationManager, never()).authenticate(any());
    }
}