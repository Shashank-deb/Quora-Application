package com.example.quoraapplication.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider
 * 
 * LOCATION: src/main/java/com/example/quoraapplication/security/JwtTokenProvider.java
 * 
 * Responsibilities:
 * ✅ Generate JWT access tokens
 * ✅ Generate JWT refresh tokens
 * ✅ Validate JWT tokens
 * ✅ Extract claims from tokens (userId, username)
 * ✅ Handle token expiration
 * 
 * Token Details:
 * - Algorithm: HS512 (HMAC SHA-512)
 * - Access Token Expiration: 24 hours (configurable)
 * - Refresh Token Expiration: 7 days (configurable)
 * - Signing Key: Minimum 32 characters (configurable)
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${app.jwtSecret:mySecretKeyForJWTTokenGenerationAndValidation12345678901234567890}")
    private String jwtSecret;

    @Value("${app.jwtExpirationInMs:86400000}")
    private int jwtExpirationInMs;

    @Value("${app.refreshTokenExpirationInMs:604800000}")
    private int refreshTokenExpirationInMs;

    /**
     * Generate JWT token from Authentication object
     * 
     * @param authentication - Spring Security Authentication object
     * @return JWT access token as String
     * 
     * Example:
     * Authentication auth = authenticationManager.authenticate(
     *     new UsernamePasswordAuthenticationToken(username, password)
     * );
     * String token = tokenProvider.generateToken(auth);
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return generateTokenFromUserId(userPrincipal.getId(), userPrincipal.getUsername());
    }

    /**
     * Generate JWT token from user ID and username
     * Used when user logs in or token is refreshed
     * 
     * @param userId - User ID
     * @param username - Username
     * @return JWT access token as String
     */
    public String generateTokenFromUserId(Long userId, String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * Generate refresh token for token renewal
     * Refresh tokens have longer expiration (7 days)
     * 
     * @param userId - User ID
     * @return JWT refresh token as String
     */
    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationInMs);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .claim("type", "refresh")
                .compact();
    }

    /**
     * Extract user ID from JWT token
     * 
     * @param token - JWT token string
     * @return User ID as Long
     * @throws Exception if token is invalid or expired
     * 
     * Example:
     * Long userId = tokenProvider.getUserIdFromJWT(token);
     */
    public Long getUserIdFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * Extract username from JWT token
     * 
     * @param token - JWT token string
     * @return Username as String
     * @throws Exception if token is invalid or expired
     */
    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return (String) claims.get("username");
    }

    /**
     * Validate JWT token
     * Checks signature, expiration, and format
     * 
     * @param authToken - JWT token to validate
     * @return true if token is valid, false otherwise
     * 
     * Handles:
     * - Invalid signature
     * - Malformed token
     * - Expired token
     * - Unsupported token format
     * - Empty token
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Get signing key for token generation and verification
     * Uses HMAC SHA-512 algorithm
     * 
     * @return SecretKey for signing
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}