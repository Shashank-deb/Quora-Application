package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comment DTO with Comprehensive Validation
 * 
 * Validates:
 * - Content: 5-5000 characters
 * - AnswerId: Required, must be positive
 * - UserId: Required, must be positive
 * - ParentCommentId: Optional, must be positive if provided
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDTO {
    
    @NotBlank(message = "Comment content is required")
    @Size(min = 5, max = 5000, message = "Comment must be between 5 and 5000 characters")
    private String content;
    
    @NotNull(message = "Answer ID is required")
    @Positive(message = "Answer ID must be positive")
    private Long answerId;
    
    @NotNull(message = "User ID is required")
    @Positive(message = "User ID must be positive")
    private Long userId;
    
    @Positive(message = "Parent comment ID must be positive")
    private Long parentCommentId;

    public boolean hasParentComment() {
        return parentCommentId != null && parentCommentId > 0;
    }

    @Override
    public String toString() {
        return "CommentDTO{" +
                "answerId=" + answerId +
                ", userId=" + userId +
                ", parentCommentId=" + parentCommentId +
                ", content='" + (content != null && content.length() > 50 
                    ? content.substring(0, 50) + "..." 
                    : content) + '\'' +
                '}';
    }
}