package com.example.quoraapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {

    @NotNull(message = "Question ID is required")
    private Long questionId;

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotBlank(message = "Answer content cannot be blank")
    private String content;

    // ============================================================================
    // toString
    // ============================================================================

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