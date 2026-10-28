package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.exception.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Writes the JSON 401 response when a protected endpoint is called without a valid token.
 * <p>Covers a missing header, an expired token and a tampered token: in each case {@code JwtAuthenticationFilter} leaves the request anonymous.
 *
 * @author Akaraan
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;        // tools.jackson.databind.ObjectMapper (Boot 4)

    /**
     * Creates the handler.
     * <p><b>Pre-conditions:</b> Spring injects the application's {@code ObjectMapper}.
     * <p><b>Post-conditions:</b> ready to write responses.
     *
     * @param objectMapper JSON writer provided by Spring (Jackson 3, {@code tools.jackson})
     */
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Writes a 401 JSON body.
     * <p><b>Pre-conditions:</b> the request reached a protected URL without an authentication.
     * <p><b>Post-conditions:</b> the response is committed with status 401; the request goes no further.
     *
     * @param request rejected request
     * @param response response to write
     * @param authException reason authentication failed
     * @throws IOException if writing the response fails
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        ErrorResponseDTO body = new ErrorResponseDTO(401, "Unauthorized",
                "Access denied: No token or invalid token provided");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}