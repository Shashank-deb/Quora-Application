package com.example.quoraapplication.config;

import com.example.quoraapplication.security.CustomUserDetailsService;
import com.example.quoraapplication.security.JwtAuthenticationFilter;
import com.example.quoraapplication.security.JwtTokenProvider;
import com.example.quoraapplication.security.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 6.3.3 Configuration - CORRECTED VERSION
 * ✅ Uses modern requestMatchers() instead of deprecated antMatchers()
 * ✅ Uses authorizeHttpRequests() instead of deprecated authorizeRequests()
 * ✅ Proper constructor injection with @RequiredArgsConstructor
 * ✅ No field injection warnings
 * ✅ Production-ready JWT authentication
 * ✅ Zero Lombok warnings
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
    // API Path Constants
    // ========================================================================

    private static final String AUTH_REGISTER = "/api/v1/auth/register";
    private static final String AUTH_LOGIN = "/api/v1/auth/login";
    private static final String AUTH_REFRESH = "/api/v1/auth/refresh-token";

    private static final String QUESTIONS_BASE = "/api/v1/questions";
    private static final String QUESTIONS_WILDCARD = "/api/v1/questions/**";

    private static final String ANSWERS_BASE = "/api/v1/answers";
    private static final String ANSWERS_WILDCARD = "/api/v1/answers/**";

    private static final String COMMENTS_BASE = "/api/v1/comments";
    private static final String COMMENTS_WILDCARD = "/api/v1/comments/**";

    private static final String SWAGGER_UI = "/swagger-ui.html";
    private static final String SWAGGER_UI_RESOURCES = "/swagger-ui/**";
    private static final String SWAGGER_DOCS = "/v3/api-docs";
    private static final String SWAGGER_DOCS_RESOURCES = "/v3/api-docs/**";

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
     * API Design:
     * ✅ csrf(csrf -> csrf.disable())                   [Lambda-based, modern]
     * ✅ cors(cors -> cors.configurationSource(...))    [Lambda-based, modern]
     * ✅ authorizeHttpRequests(authz -> authz...)       [Replaces authorizeRequests]
     * ✅ requestMatchers()                               [Replaces antMatchers]
     * ✅ NO .and() chaining - uses lambda scope instead [Cleaner syntax]
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
                // 5. Authorization Rules (Spring Security 6.3.3 API)
                // ================================================================
                .authorizeHttpRequests(authz -> authz

                        // ==================== PUBLIC AUTH ENDPOINTS ====================
                        // No authentication required
                        .requestMatchers(AUTH_REGISTER).permitAll()
                        .requestMatchers(AUTH_LOGIN).permitAll()
                        .requestMatchers(AUTH_REFRESH).permitAll()

                        // ==================== SWAGGER/DOCS ENDPOINTS ====================
                        // Public API documentation
                        .requestMatchers(SWAGGER_UI).permitAll()
                        .requestMatchers(SWAGGER_UI_RESOURCES).permitAll()
                        .requestMatchers(SWAGGER_DOCS).permitAll()
                        .requestMatchers(SWAGGER_DOCS_RESOURCES).permitAll()

                        // ==================== HEALTH ENDPOINTS ====================
                        // Health checks without authentication
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        // ==================== QUESTIONS ENDPOINTS ====================
                        // GET - Retrieve questions
                        .requestMatchers(HttpMethod.GET, QUESTIONS_BASE).authenticated()
                        .requestMatchers(HttpMethod.GET, QUESTIONS_WILDCARD).authenticated()

                        // POST - Create question
                        .requestMatchers(HttpMethod.POST, QUESTIONS_BASE).authenticated()

                        // PUT - Update question
                        .requestMatchers(HttpMethod.PUT, QUESTIONS_BASE).authenticated()
                        .requestMatchers(HttpMethod.PUT, QUESTIONS_WILDCARD).authenticated()

                        // DELETE - Delete question
                        .requestMatchers(HttpMethod.DELETE, QUESTIONS_BASE).authenticated()
                        .requestMatchers(HttpMethod.DELETE, QUESTIONS_WILDCARD).authenticated()

                        // ==================== ANSWERS ENDPOINTS ====================
                        // GET - Retrieve answers
                        .requestMatchers(HttpMethod.GET, ANSWERS_BASE).authenticated()
                        .requestMatchers(HttpMethod.GET, ANSWERS_WILDCARD).authenticated()

                        // POST - Create answer
                        .requestMatchers(HttpMethod.POST, ANSWERS_BASE).authenticated()

                        // PUT - Update answer
                        .requestMatchers(HttpMethod.PUT, ANSWERS_BASE).authenticated()
                        .requestMatchers(HttpMethod.PUT, ANSWERS_WILDCARD).authenticated()

                        // DELETE - Delete answer
                        .requestMatchers(HttpMethod.DELETE, ANSWERS_BASE).authenticated()
                        .requestMatchers(HttpMethod.DELETE, ANSWERS_WILDCARD).authenticated()

                        // ==================== COMMENTS ENDPOINTS ====================
                        // GET - Retrieve comments
                        .requestMatchers(HttpMethod.GET, COMMENTS_BASE).authenticated()
                        .requestMatchers(HttpMethod.GET, COMMENTS_WILDCARD).authenticated()

                        // POST - Create comment
                        .requestMatchers(HttpMethod.POST, COMMENTS_BASE).authenticated()

                        // PUT - Update comment
                        .requestMatchers(HttpMethod.PUT, COMMENTS_BASE).authenticated()
                        .requestMatchers(HttpMethod.PUT, COMMENTS_WILDCARD).authenticated()

                        // DELETE - Delete comment
                        .requestMatchers(HttpMethod.DELETE, COMMENTS_BASE).authenticated()
                        .requestMatchers(HttpMethod.DELETE, COMMENTS_WILDCARD).authenticated()

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