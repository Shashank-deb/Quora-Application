package com.example.quoraapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String role;
    private Integer questionCount;
    private Integer answerCount;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================================
    // Additional Computed Fields
    // ============================================================================

    /**
     * Get full name from first and last name
     */
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }

    /**
     * Get total contribution count
     */
    public Integer getTotalContributions() {
        int total = 0;
        if (questionCount != null) total += questionCount;
        if (answerCount != null) total += answerCount;
        if (commentCount != null) total += commentCount;
        return total;
    }

    /**
     * Check if user has completed profile
     */
    public boolean hasCompletedProfile() {
        return firstName != null && lastName != null && bio != null && !bio.trim().isEmpty();
    }

    // ============================================================================
    // toString
    // ============================================================================

    @Override
    public String toString() {
        return "UserResponseDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", bio='" + bio + '\'' +
                ", role='" + role + '\'' +
                ", questionCount=" + questionCount +
                ", answerCount=" + answerCount +
                ", commentCount=" + commentCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}