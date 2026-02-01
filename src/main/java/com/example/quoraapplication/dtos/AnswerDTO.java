package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Answer DTO with Comprehensive Validation
 * 
 * Validates:
 * - QuestionId: Required, must be positive
 * - AuthorId: Required, must be positive
 * - Content: 20-50000 characters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {

    @NotNull(message = "Question ID is required")
    @Positive(message = "Question ID must be positive")
    private Long questionId;

    @NotNull(message = "Author ID is required")
    @Positive(message = "Author ID must be positive")
    private Long authorId;

    @NotBlank(message = "Answer content cannot be blank")
    @Size(min = 20, max = 50000, message = "Answer must be between 20 and 50000 characters")
    private String content;

    @Override
    public String toString() {
        return "AnswerDTO{" +
                "questionId=" + questionId +
                ", authorId=" + authorId +
                ", content='" + (content != null && content.length() > 50 
                    ? content.substring(0, 50) + "..." 
                    : content) + '\'' +
                '}';
    }
}