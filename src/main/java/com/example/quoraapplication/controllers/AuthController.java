package com.example.quoraapplication.controllers;


import com.example.quoraapplication.dtos.*;
import com.example.quoraapplication.models.Role;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.UserRepository;
import com.example.quoraapplication.security.JwtTokenProvider;
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


    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Registering new user: {}", registerRequest.getUsername());

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            log.warn("Username already exists: {}", registerRequest.getUsername());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Username is already taken!", null));
        }


        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already exists: {}", registerRequest.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Email address already in use!", null));
        }


        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_USER);
        user.setFollowedTags(new HashSet<>());
        user.setQuestions(new HashSet<>());
        user.setAnswers(new HashSet<>());
        user.setComments(new HashSet<>());

        User result = userRepository.save(user);
        log.info("User registered successfully: {}", result.getId());

        UserResponse userResponse = UserResponse.fromUser(result);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "User registered successfully", userResponse));
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsernameOrEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsernameOrEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = jwtTokenProvider.generateToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(
                    Long.parseLong(authentication.getPrincipal().toString())
            );

            User user = userRepository.findByUsernameOrEmail(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getUsernameOrEmail()
            ).orElseThrow(() -> new RuntimeException("User not found"));

            JwtAuthenticationResponse jwtResponse = JwtAuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(86400000L) // 24 hours in ms
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


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.debug("Refreshing JWT token");

        if (!jwtTokenProvider.validateToken(refreshTokenRequest.getRefreshToken())) {
            log.warn("Invalid refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "Invalid or expired refresh token", null));
        }

        Long userId = jwtTokenProvider.getUserIdFromJWT(refreshTokenRequest.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newAccessToken = jwtTokenProvider.generateTokenFromUserId(userId, user.getUsername());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);

        JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(UserResponse.fromUser(user))
                .build();

        log.info("Token refreshed successfully for user: {}", userId);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
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


    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        log.info("User logged out successfully");
        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully", null));
    }
}
