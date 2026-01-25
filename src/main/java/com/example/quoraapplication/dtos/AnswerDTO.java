package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDTO {
    
    @NotBlank(message = "Answer content is required")
    private String content;
    
    private Long questionId;
    
    private Long userId;
}