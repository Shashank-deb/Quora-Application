package com.example.quoraapplication.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JWT Authentication Entry Point
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/security/JwtAuthenticationEntryPoint.java
 * 
 * Responsibilities:
 * ✅ Handle unauthorized access attempts
 * ✅ Return JSON error response (not HTML)
 * ✅ Provide meaningful error messages
 * ✅ Set proper HTTP status code (401)
 * 
 * When It's Called:
 * - User tries to access protected endpoint without JWT token
 * - User provides invalid/expired JWT token
 * - JWT token is missing from Authorization header
 * 
 * Response Format:
 * HTTP 401 Unauthorized
 * Content-Type: application/json
 * {
 *   "timestamp": "2024-01-26T10:30:00",
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Full authentication is required to access this resource",
 *   "path": "/api/v1/protected-endpoint"
 * }
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Handle authentication exception
     * Called when authentication fails
     * 
     * @param request - The HTTP request
     * @param response - The HTTP response to write error to
     * @param authException - The authentication exception
     * @throws IOException if writing to response fails
     * @throws ServletException if servlet error occurs
     * 
     * Examples of When This Is Called:
     * 
     * 1. Missing Token:
     *    GET /api/v1/questions HTTP/1.1
     *    → Response: 401 Unauthorized (no Authorization header)
     * 
     * 2. Invalid Token:
     *    GET /api/v1/questions HTTP/1.1
     *    Authorization: Bearer invalid_token
     *    → Response: 401 Unauthorized (token validation failed)
     * 
     * 3. Expired Token:
     *    GET /api/v1/questions HTTP/1.1
     *    Authorization: Bearer expired_token
     *    → Response: 401 Unauthorized (token expired)
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        log.error("Responding with unauthorized error. Message: {}", authException.getMessage());

        // Set response content type and status
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Build error response body
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", "Unauthorized");
        body.put("message", "Full authentication is required to access this resource");
        body.put("path", request.getServletPath());

        // Write response as JSON
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}