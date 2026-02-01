package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Question DTO with Comprehensive Validation
 * 
 * Validates:
 * - Title: 5-500 characters, no special chars
 * - Content: 20-50000 characters
 * - UserId: Required, must be positive
 * - TagIds: 1-10 tags
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionDTO {
    
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 500, message = "Title must be between 5 and 500 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\s\\?\\-\\.,'\"()]+$", 
             message = "Title contains invalid characters. Only letters, numbers, and basic punctuation allowed")
    private String title;

    @NotBlank(message = "Content is required")
    @Size(min = 20, max = 50000, message = "Content must be between 20 and 50000 characters")
    private String content;

    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;

    @NotEmpty(message = "At least one tag is required")
    @Size(min = 1, max = 10, message = "You can add minimum 1 and maximum 10 tags")
    private Set<Long> tagIds;

    @Override
    public String toString() {
        return "QuestionDTO{" +
                "id=" + id +
                ", title='" + (title != null && title.length() > 50 ? 
                    title.substring(0, 50) + "..." : title) + '\'' +
                ", userId=" + userId +
                ", tagIds=" + tagIds +
                '}';
    }
}