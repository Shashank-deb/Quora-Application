package com.example.quoraapplication.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/security/JwtAuthenticationFilter.java
 * 
 * Responsibilities:
 * ✅ Intercepts every HTTP request
 * ✅ Extracts JWT from Authorization header
 * ✅ Validates JWT token
 * ✅ Loads user details from database
 * ✅ Sets up Spring Security context with authentication
 * ✅ Gracefully handles invalid/expired tokens
 * 
 * Filter Chain:
 * 1. Request arrives
 * 2. Filter extracts "Bearer <token>" from Authorization header
 * 3. Token is validated using JwtTokenProvider
 * 4. User ID is extracted from token
 * 5. User details are loaded from database
 * 6. Spring Security context is populated
 * 7. Request proceeds to controller
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    /**
     * Main filter method - runs for every request (once per request)
     * 
     * Flow:
     * 1. Try to extract JWT from request header
     * 2. Validate the JWT token
     * 3. Extract userId from token
     * 4. Load user details from database
     * 5. Create authentication token with user details
     * 6. Set authentication in security context
     * 7. Continue filter chain
     * 
     * If any error occurs, request proceeds without authentication
     * (401 Unauthorized will be returned if endpoint requires auth)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Step 1: Extract JWT from request
            String jwt = getJwtFromRequest(request);

            // Step 2: Validate JWT
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                
                // Step 3: Extract userId from JWT
                Long userId = tokenProvider.getUserIdFromJWT(jwt);

                // Step 4: Load user details from database
                UserDetails userDetails = customUserDetailsService.loadUserById(userId);
                
                // Step 5: Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                
                // Set request details for audit logging
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Step 6: Set authentication in security context
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set Spring Security Authentication for user: {}", userDetails.getUsername());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        // Step 7: Continue filter chain (whether authenticated or not)
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     * 
     * Expected format: Authorization: Bearer <token>
     * 
     * @param request - HTTP request
     * @return JWT token string or null if not found
     * 
     * Examples:
     * Valid: Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
     * Invalid: Authorization: Basic xyz...
     * Invalid: Authorization header missing
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token after "Bearer "
            return bearerToken.substring(7);
        }
        
        return null;
    }
}