package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    
    @NotBlank(message = "Comment content is required")
    private String content;
    
    private Long answerId;
    
    private Long userId;
    
    /**
     * Parent comment ID for nested replies (optional)
     */
    private Long parentCommentId;
}