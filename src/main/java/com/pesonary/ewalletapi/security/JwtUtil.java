package com.pesonary.ewalletapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * Creates and validates JSON Web Tokens signed with HS256.
 * <p>The secret and lifetime come from {@code app.jwt.secret} and {@code app.jwt.expiration-ms} in {@code application.properties}, not hard-coded. The secret must be at least 32 bytes, or startup fails with {@code WeakKeyException}.
 *
 * @author Akaraan
 */
@Component
public class JwtUtil {

    private final Key signingKey;                    // java.security.Key
    private final long expirationMs;

    /**
     * Builds the signing key from the configured secret.
     * <p><b>Pre-conditions:</b> {@code secret} is at least 32 bytes in UTF-8; {@code expirationMs} is positive.
     * <p><b>Post-conditions:</b> the key and lifetime are fixed.
     *
     * @param secret value of {@code app.jwt.secret}
     * @param expirationMs token lifetime in milliseconds ({@code app.jwt.expiration-ms})
     */
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    /**
     * Creates a signed token for a user.
     * <p><b>Pre-conditions:</b> {@code username} is not null and the user has just authenticated.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param username token subject
     * @return compact JWT string, valid for {@code expirationMs}
     */
    public String generateToken(String username) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Parses a token and verifies its signature and expiry.
     * <p><b>Pre-conditions:</b> {@code token} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param token compact JWT string
     * @return Claims object containing the subject, issue time and expiry
     * @throws JwtException if the token is expired, tampered with or signed with another key
     */
    private Claims parseClaims(String token) {
        // Throws JwtException if the token is expired, tampered with or signed with another key
        return Jwts.parserBuilder().setSigningKey(signingKey).build()
                .parseClaimsJws(token).getBody();
    }

    /**
     * Reads the username from a token.
     * <p><b>Pre-conditions:</b> {@code token} is not null.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param token compact JWT string
     * @return token subject (the username)
     * @throws JwtException if the token is invalid
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Checks that a token belongs to the given user and has not expired.
     * <p><b>Pre-conditions:</b> {@code token} is not null; {@code user} was loaded for the token's subject.
     * <p><b>Post-conditions:</b> no data changes.
     *
     * @param token compact JWT string
     * @param user user loaded for the token's subject
     * @return {@code true} if the subject matches and the token has not expired; {@code false} on any token error
     */
    public boolean isTokenValid(String token, UserDetails user) {
        try {
            Claims claims = parseClaims(token);
            return claims.getSubject().equals(user.getUsername())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}