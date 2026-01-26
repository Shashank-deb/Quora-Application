package com.example.quoraapplication.security;

import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService Implementation
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/security/CustomUserDetailsService.java
 * 
 * Responsibilities:
 * ✅ Load user details from database by username/email
 * ✅ Load user details from database by user ID
 * ✅ Convert User entity to Spring Security UserDetails
 * ✅ Handle user not found scenarios
 * 
 * Usage:
 * - Used by Spring Security during authentication
 * - Used by JWT filter to load user after token validation
 * - Can also be used to load user details by ID for API calls
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load user by username or email
     * Spring Security calls this during login with the username
     * 
     * @param usernameOrEmail - Username or email from login request
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     * 
     * Example:
     * User logs in with:
     * - username: "john_doe" → loads user with this username
     * - email: "john@example.com" → loads user with this email
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("Loading user by username or email: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "User not found with username or email: " + usernameOrEmail);
                });

        log.debug("User found: {}", user.getUsername());
        return UserPrincipal.create(user);
    }

    /**
     * Load user by user ID
     * JWT filter calls this after extracting user ID from token
     * 
     * @param id - User ID from JWT token
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     * 
     * Example:
     * JWT contains: subject = "123" (user ID)
     * loadUserById(123L) → returns UserPrincipal for user with ID 123
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User not found with id: " + id);
                });

        log.debug("User found by ID: {}", user.getId());
        return UserPrincipal.create(user);
    }
}