package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.exception.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Writes the JSON 403 response when a URL rule in {@code SecurityConfig} refuses a logged-in user.
 * <p>Method-security refusals ({@code @PreAuthorize}, {@code @PostAuthorize}) do not come here; they reach {@code GlobalExceptionHandler}.
 *
 * @author Akaraan
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;        // tools.jackson.databind.ObjectMapper (Boot 4)

    /**
     * Creates the handler.
     * <p><b>Pre-conditions:</b> Spring injects the application's {@code ObjectMapper}.
     * <p><b>Post-conditions:</b> ready to write responses.
     *
     * @param objectMapper JSON writer provided by Spring (Jackson 3, {@code tools.jackson})
     */
    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Writes a 403 JSON body.
     * <p><b>Pre-conditions:</b> the request is authenticated but a URL rule refused it.
     * <p><b>Post-conditions:</b> the response is committed with status 403; the request goes no further.
     *
     * @param request refused request
     * @param response response to write
     * @param ex reason for the refusal
     * @throws IOException if writing the response fails
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        ErrorResponseDTO body = new ErrorResponseDTO(403, "Forbidden",
                "You do not have permission to access this resource");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}