package com.pesonary.ewalletapi.security;

import com.pesonary.ewalletapi.exception.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;        // tools.jackson.databind.ObjectMapper (Boot 4)

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

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