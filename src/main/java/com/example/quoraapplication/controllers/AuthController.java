package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.*;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.UserRepository;
import com.example.quoraapplication.security.JwtTokenProvider;
import com.example.quoraapplication.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

/**
 * Authentication Controller
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/controllers/AuthController.java
 * 
 * BASE URL: /api/v1/auth
 * 
 * Endpoints:
 * ✅ POST /register     - Register new user
 * ✅ POST /login        - Login and get JWT tokens
 * ✅ POST /refresh      - Refresh access token using refresh token
 * ✅ GET /me            - Get current authenticated user profile
 * ✅ POST /logout       - Logout (clear security context)
 * 
 * Response Format:
 * All responses include a wrapper object with status, message, and data
 * 
 * Error Handling:
 * - Returns appropriate HTTP status codes (200, 201, 400, 401, 409)
 * - Includes descriptive error messages
 * - Validates input data using @Valid annotations
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register New User
     * 
     * POST /api/v1/auth/register
     * 
     * Request Body:
     * {
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "password": "SecurePassword@123"
     * }
     * 
     * Response 201 Created:
     * {
     *   "success": true,
     *   "message": "User registered successfully",
     *   "data": {
     *     "id": 1,
     *     "username": "john_doe",
     *     "email": "john@example.com",
     *     "createdAt": "2024-01-26T10:30:00",
     *     "updatedAt": "2024-01-26T10:30:00"
     *   }
     * }
     * 
     * Response 400 Bad Request (username exists):
     * {
     *   "success": false,
     *   "message": "Username is already taken!",
     *   "data": null
     * }
     * 
     * Response 400 Bad Request (email exists):
     * {
     *   "success": false,
     *   "message": "Email address already in use!",
     *   "data": null
     * }
     * 
     * Validations:
     * ✅ Username: 3-50 characters, required
     * ✅ Email: Valid email format, required
     * ✅ Password: 6-100 characters, required
     * ✅ Username uniqueness
     * ✅ Email uniqueness
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Username already exists: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Username is already taken!", null));
        }

        // Check if email already exists
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already exists: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Email address already in use!", null));
        }

        // Create new user entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(User.Role.ROLE_USER);
        user.setFollowedTags(new HashSet<>());
        user.setQuestions(new HashSet<>());
        user.setAnswers(new HashSet<>());
        user.setComments(new HashSet<>());

        // Save user to database
        User result = userRepository.save(user);
        log.info("User registered successfully: {}", result.getId());

        // Convert to response DTO
        UserResponse userResponse = UserResponse.fromUser(result);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User registered successfully", userResponse));
    }

    /**
     * User Login
     * 
     * POST /api/v1/auth/login
     * 
     * Request Body:
     * {
     *   "usernameOrEmail": "john_doe",  // or email: "john@example.com"
     *   "password": "SecurePassword@123"
     * }
     * 
     * Response 200 OK:
     * {
     *   "access_token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "token_type": "Bearer",
     *   "expires_in": 86400000,
     *   "user": {
     *     "id": 1,
     *     "username": "john_doe",
     *     "email": "john@example.com",
     *     "createdAt": "2024-01-26T10:30:00"
     *   }
     * }
     * 
     * Response 401 Unauthorized (invalid credentials):
     * {
     *   "success": false,
     *   "message": "Invalid username/email or password",
     *   "data": null
     * }
     * 
     * Validations:
     * ✅ Username/email exists
     * ✅ Password matches (case-sensitive)
     * ✅ Account is active
     * 
     * Token Details:
     * - Access Token expiration: 24 hours
     * - Refresh Token expiration: 7 days
     * - Algorithm: HS512 (HMAC SHA-512)
     */
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());

        try {
            // Authenticate user with username/email and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // ✅ FIX: Extract UserPrincipal correctly to get user ID
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();

            // Generate JWT tokens
            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

            // Fetch user from database
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Build response
            JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L) // 24 hours in milliseconds
                    .user(UserResponse.fromUser(user))
                    .build();

            log.info("User logged in successfully: {}", user.getId());
            return ResponseEntity.ok(jwtResponse);

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsernameOrEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid username/email or password", null));
        }
    }

    /**
     * Refresh Access Token
     * 
     * POST /api/v1/auth/refresh
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     * 
     * Response 200 OK:
     * {
     *   "access_token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "refresh_token": "eyJhbGciOiJIUzUxMiJ9...",
     *   "token_type": "Bearer",
     *   "expires_in": 86400000,
     *   "user": {...}
     * }
     * 
     * Response 401 Unauthorized (invalid refresh token):
     * {
     *   "success": false,
     *   "message": "Invalid or expired refresh token",
     *   "data": null
     * }
     * 
     * Usage:
     * When access token expires (after 24 hours), client uses refresh token
     * to get new access token without re-entering credentials
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.debug("Refreshing JWT token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshTokenRequest.getRefreshToken())) {
            log.warn("Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid or expired refresh token", null));
        }

        // Extract user ID from refresh token
        Long userId = jwtTokenProvider.getUserIdFromJWT(refreshTokenRequest.getRefreshToken());
        
        // Fetch user from database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate new tokens
        String newAccessToken = jwtTokenProvider.generateTokenFromUserId(userId, user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        // Build response
        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .user(UserResponse.fromUser(user))
                .build();

        log.info("Token refreshed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get Current Authenticated User
     * 
     * GET /api/v1/auth/me
     * 
     * Headers:
     * Authorization: Bearer <access_token>
     * 
     * Response 200 OK:
     * {
     *   "id": 1,
     *   "username": "john_doe",
     *   "email": "john@example.com",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "bio": "Software Engineer",
     *   "createdAt": "2024-01-26T10:30:00",
     *   "updatedAt": "2024-01-26T10:30:00"
     * }
     * 
     * Response 401 Unauthorized (no valid token):
     * {
     *   "success": false,
     *   "message": "User not authenticated",
     *   "data": null
     * }
     * 
     * Usage:
     * Client calls this endpoint after login to fetch current user details
     * Useful for populating user profile in frontend
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        log.debug("Fetching current user info");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not authenticated", null));
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Current user info retrieved: {}", user.getId());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * Logout User
     * 
     * POST /api/v1/auth/logout
     * 
     * Headers:
     * Authorization: Bearer <access_token>
     * 
     * Response 200 OK:
     * {
     *   "success": true,
     *   "message": "User logged out successfully",
     *   "data": null
     * }
     * 
     * Note:
     * Since we use stateless JWT authentication, logout only clears
     * the security context. Token remains valid until expiration.
     * For immediate token revocation, implement token blacklist.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully", null));
    }
}