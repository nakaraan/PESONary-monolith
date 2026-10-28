
package com.pesonary.ewalletapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security setup: the JWT filter, user lookup, BCrypt, the 401 and 403 handlers, and method security.
 * <p>{@code /api/v1/auth/**}, {@code /actuator/health} and {@code /error} are public; everything else needs a token. Sessions are stateless and CSRF is off because the API uses tokens, not cookies. {@code @EnableMethodSecurity} switches on every {@code @PreAuthorize} and {@code @PostAuthorize} in the services.
 *
 * @author Akaraan
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * Creates the configuration.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param jwtAuthFilter filter that reads bearer tokens
     * @param userDetailsService loads users for login
     * @param authEntryPoint writes 401 responses
     * @param accessDeniedHandler writes 403 responses for URL rules
     */
    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter, CustomUserDetailsService userDetailsService, CustomAuthenticationEntryPoint authEntryPoint, CustomAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * Builds the filter chain used for every request.
     * <p><b>Pre-conditions:</b> all four collaborators are beans.
     * <p><b>Post-conditions:</b> URL rules, stateless sessions, the JSON 401/403 handlers and the JWT filter are active.
     *
     * @param http Spring's security builder
     * @return SecurityFilterChain object applied to every request
     * @throws Exception if the configuration is invalid
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // no cookies, so no CSRF protection needed
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll() // register + login need no token
                        .requestMatchers("/actuator/health", "/error").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // no server session; the token is the only credential
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // set the identity before the rules are checked
        return http.build();
    }

    /**
     * Login provider: loads the user with {@code CustomUserDetailsService} and checks the password with BCrypt.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> the provider bean exists.
     *
     * @return AuthenticationProvider used by {@code authenticationManager()}
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Authentication manager injected into {@code AuthController} for login.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> the manager bean exists.
     *
     * @return AuthenticationManager backed by {@code authenticationProvider()}
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    /**
     * BCrypt encoder: hashes passwords on registration and verifies them on login.
     * <p><b>Pre-conditions:</b> none.
     * <p><b>Post-conditions:</b> the encoder bean exists.
     *
     * @return PasswordEncoder using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
