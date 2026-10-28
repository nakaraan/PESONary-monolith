package com.pesonary.ewalletapi.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Reads the {@code Authorization: Bearer <token>} header on every request and logs the user in for that request.
 * <p>Runs once per request, before Spring's login filter. A missing or bad token leaves the request anonymous, and the URL rules in {@code SecurityConfig} then return 401. The filter never rejects a request itself.
 *
 * @author Akaraan
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Creates the filter.
     * <p><b>Pre-conditions:</b> Spring injects every dependency.
     * <p><b>Post-conditions:</b> ready to use.
     *
     * @param jwtUtil token parser and validator
     * @param userDetailsService loads the user named in the token
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Validates the bearer token and stores the authentication in {@code SecurityContextHolder}.
     * <p><b>Pre-conditions:</b> called by Spring Security once per request.
     * <p><b>Post-conditions:</b> with a valid token, the context holds the user and their authorities; otherwise the context is empty. The request always continues down the chain.
     *
     * @param request incoming request
     * @param response outgoing response
     * @param filterChain rest of the filter chain
     * @throws ServletException if a later filter fails
     * @throws IOException if a later filter fails
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);        // no token: the URL rules decide (401)
            return;
        }
        String token = header.substring(7);                 // strip "Bearer "
        try {
            String username = jwtUtil.extractUsername(token); // token subject = username
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { // skip if already authenticated
                UserDetails user = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.isTokenValid(token, user)) {
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();           // bad token = anonymous = 401 later
        }
        filterChain.doFilter(request, response);
    }
}