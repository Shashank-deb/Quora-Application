package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Tag DTO with Comprehensive Validation
 * 
 * Validates:
 * - Name: 2-50 characters, alphanumeric + hyphen
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagDTO {
    
    private Long id;
    
    @NotBlank(message = "Tag name is required")
    @Size(min = 2, max = 50, message = "Tag name must be 2-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9\\-]+$", 
             message = "Tag name can only contain letters, numbers, and hyphens")
    private String name;

    @Override
    public String toString() {
        return "TagDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}