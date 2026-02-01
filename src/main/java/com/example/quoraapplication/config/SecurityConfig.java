package com.example.quoraapplication.config;

import com.example.quoraapplication.security.CustomUserDetailsService;
import com.example.quoraapplication.security.JwtAuthenticationFilter;
import com.example.quoraapplication.security.JwtTokenProvider;
import com.example.quoraapplication.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 6.3.3 Configuration - FINAL CORRECTED VERSION
 * ✅ Uses requestMatchers() with AntPathRequestMatcher (stable for Spring 6.3.3)
 * ✅ Avoids strict MvcRequestMatcher pattern validation
 * ✅ All endpoint patterns properly configured
 * ✅ No pattern parsing errors
 * ✅ Production-ready JWT authentication
 * ✅ Proper constructor injection with @RequiredArgsConstructor
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // ========================================================================
    // Bean 1: Password Encoder
    // ========================================================================

    /**
     * BCrypt password encoder with strength 10
     * Higher strength = more secure but slower (use 10-12 for production)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    // ========================================================================
    // Bean 2: Authentication Provider
    // ========================================================================

    /**
     * DAO Authentication Provider
     * Authenticates users with username/password against UserDetailsService
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // ========================================================================
    // Bean 3: Authentication Manager
    // ========================================================================

    /**
     * Authentication Manager Bean
     * Required for manual authentication in AuthController
     * Uses AuthenticationConfiguration instead of deprecated pattern
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ========================================================================
    // Bean 4: JWT Authentication Filter
    // ========================================================================

    /**
     * JWT Authentication Filter Bean
     * Validates JWT tokens on each request
     * Should be injected directly into filterChain, not as autowired field
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService);
    }

    // ========================================================================
    // Bean 5: CORS Configuration
    // ========================================================================

    /**
     * CORS Configuration for frontend applications
     * Allows multiple localhost ports for development
     *
     * Backend runs on: http://localhost:1004
     * Allowed frontends:
     *   - React: 3000, 3001, 5173 (Vite)
     *   - Angular: 4200
     *   - Generic: 8080
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Allowed origins
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React dev server
                "http://localhost:3001",      // React alternate port
                "http://localhost:4200",      // Angular dev server
                "http://localhost:5173",      // Vite dev server
                "http://localhost:8080",      // Generic frontend
                "http://localhost:1004"       // Backend itself (for testing)
        ));

        // ✅ Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // ✅ Allowed request headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // ✅ Headers exposed to client
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));

        // ✅ Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // ✅ Cache CORS preflight for 1 hour (3600 seconds)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ========================================================================
    // Bean 6: Security Filter Chain (MAIN CONFIGURATION)
    // ========================================================================

    /**
     * Main Security Configuration for Spring Security 6.3.3
     *
     * ✅ Uses requestMatchers(AntPathRequestMatcher) for pattern matching
     * ✅ Explicitly specifies AntPathRequestMatcher to avoid MvcRequestMatcher
     * ✅ This is the recommended approach for Spring Security 6.1+
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ================================================================
                // 1. CSRF Configuration (Disabled for Stateless API)
                // ================================================================
                .csrf(csrf -> csrf.disable())

                // ================================================================
                // 2. CORS Configuration
                // ================================================================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ================================================================
                // 3. Exception Handling
                // ================================================================
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // ================================================================
                // 4. Session Management (Stateless - No Cookies)
                // ================================================================
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ================================================================
                // 5. Authorization Rules (Using requestMatchers with AntPathRequestMatcher)
                // ================================================================
                .authorizeHttpRequests(authz -> authz

                        // ==================== PUBLIC AUTH ENDPOINTS ====================
                        // No authentication required
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/refresh-token")).permitAll()

                        // ==================== SWAGGER/DOCS ENDPOINTS ====================
                        // Public API documentation
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui.html")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/v3/api-docs/**")).permitAll()

                        // ==================== HEALTH ENDPOINTS ====================
                        // Health checks without authentication
                        .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/actuator/health")).permitAll()

                        // ==================== QUESTIONS ENDPOINTS ====================
                        // GET - Retrieve questions
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions", "GET")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions/**", "GET")).authenticated()

                        // POST - Create question
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions", "POST")).authenticated()

                        // PUT - Update question
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions", "PUT")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions/**", "PUT")).authenticated()

                        // DELETE - Delete question
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions", "DELETE")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/questions/**", "DELETE")).authenticated()

                        // ==================== ANSWERS ENDPOINTS ====================
                        // GET - Retrieve answers
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers", "GET")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers/**", "GET")).authenticated()

                        // POST - Create answer
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers", "POST")).authenticated()

                        // PUT - Update answer
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers", "PUT")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers/**", "PUT")).authenticated()

                        // DELETE - Delete answer
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers", "DELETE")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/answers/**", "DELETE")).authenticated()

                        // ==================== COMMENTS ENDPOINTS ====================
                        // GET - Retrieve comments
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments", "GET")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments/**", "GET")).authenticated()

                        // POST - Create comment
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments", "POST")).authenticated()

                        // PUT - Update comment
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments", "PUT")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments/**", "PUT")).authenticated()

                        // DELETE - Delete comment
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments", "DELETE")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/comments/**", "DELETE")).authenticated()

                        // ==================== USERS ENDPOINTS ====================
                        // GET - Retrieve users
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users", "GET")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users/**", "GET")).authenticated()

                        // POST - Create user
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users", "POST")).authenticated()

                        // PUT - Update user
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users", "PUT")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users/**", "PUT")).authenticated()

                        // DELETE - Delete user
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users", "DELETE")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/users/**", "DELETE")).authenticated()

                        // ==================== TAGS ENDPOINTS ====================
                        // GET - Retrieve tags (public for discovery)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags", "GET")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags/**", "GET")).permitAll()

                        // POST - Create tag (authenticated)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags", "POST")).authenticated()

                        // PUT - Update tag (authenticated)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags", "PUT")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags/**", "PUT")).authenticated()

                        // DELETE - Delete tag (authenticated)
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags", "DELETE")).authenticated()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/tags/**", "DELETE")).authenticated()

                        // ==================== DEFAULT RULE ====================
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // ================================================================
                // 6. Add JWT Filter Before UsernamePasswordAuthenticationFilter
                // ================================================================
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)

                // ================================================================
                // 7. Frame Options (Allow H2 Console)
                // ================================================================
                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

        return http.build();
    }
}