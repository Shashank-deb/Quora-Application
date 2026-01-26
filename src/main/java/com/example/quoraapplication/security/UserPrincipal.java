package com.example.quoraapplication.security;

import com.example.quoraapplication.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * UserPrincipal - Spring Security User Implementation
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/security/UserPrincipal.java
 * 
 * Purpose:
 * ✅ Implements Spring Security's UserDetails interface
 * ✅ Holds user information for authentication context
 * ✅ Provides authorities/roles for authorization
 * ✅ Encapsulates user account state (enabled, locked, etc.)
 * 
 * Information Stored:
 * - User ID
 * - Username
 * - Email
 * - Password (hashed)
 * - Authorities (roles)
 * - Account status (enabled, locked, expired)
 * 
 * Usage:
 * User user = userRepository.findByUsername("john");
 * UserDetails userDetails = UserPrincipal.create(user);
 * // userDetails can now be used by Spring Security
 */
@Data
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;

    @JsonIgnore
    private String password;

    private Collection<? extends GrantedAuthority> authorities;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;

    /**
     * Factory method to create UserPrincipal from User entity
     * 
     * @param user - User entity from database
     * @return UserPrincipal configured with user details and authorities
     * 
     * Example:
     * User user = new User();
     * user.setId(1L);
     * user.setUsername("john");
     * user.setPassword("hashed_password");
     * user.setRole(User.Role.ROLE_USER);
     * 
     * UserDetails userDetails = UserPrincipal.create(user);
     * // userDetails.getAuthorities() returns [ROLE_USER]
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        // Add user's role as authority
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority(user.getRole().name()));
        }

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(user.isActive())
                .build();
    }

    /**
     * Get username for authentication
     * Required by UserDetails interface
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Get encrypted password
     * Required by UserDetails interface
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Get user authorities (roles)
     * Required by UserDetails interface
     * 
     * Example: [ROLE_USER, ROLE_ADMIN]
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Check if account has not expired
     * Required by UserDetails interface
     */
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    /**
     * Check if account is not locked
     * Required by UserDetails interface
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Check if credentials have not expired
     * Required by UserDetails interface
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    /**
     * Check if account is enabled
     * Required by UserDetails interface
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Equals - compare by user ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    /**
     * Hash code - based on user ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}