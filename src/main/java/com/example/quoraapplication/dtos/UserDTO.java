package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User DTO with Comprehensive Validation
 * 
 * Validates:
 * - Username: 3-50 characters, alphanumeric + underscore/hyphen
 * - Password: 8-100 characters
 * - Email: Valid email format
 * - FirstName: Max 100 characters
 * - LastName: Max 100 characters
 * - Bio: Max 500 characters
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", 
             message = "Username can only contain letters, numbers, underscores, and hyphens")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Size(max = 100, message = "First name must be max 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name must be max 100 characters")
    private String lastName;

    @Size(max = 500, message = "Bio must be max 500 characters")
    private String bio;

    @Override
    public String toString() {
        return "UserDTO{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}