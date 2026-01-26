package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.UserDTO;
import com.example.quoraapplication.dtos.UserResponseDTO;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ============================================================================
    // Get Users
    // ============================================================================

    /**
     * Get all users with pagination
     * @param page - page number (0-indexed)
     * @param size - page size
     * @return Page of UserResponseDTO
     */
    @GetMapping
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all users - page: {}, size: {}", page, size);
        Page<UserResponseDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user by ID
     * @param userId - the user ID
     * @return UserResponseDTO
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable("id") Long userId) {
        log.info("Fetching user with ID: {}", userId);
        Optional<UserResponseDTO> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user by username
     * @param username - the username
     * @return UserResponseDTO
     */
    @GetMapping("/search/username/{username}")
    public ResponseEntity<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        log.info("Fetching user with username: {}", username);
        Optional<UserResponseDTO> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Get user by email
     * @param email - the email address
     * @return UserResponseDTO
     */
    @GetMapping("/search/email/{email}")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        log.info("Fetching user with email: {}", email);
        Optional<UserResponseDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ============================================================================
    // Update User Profile
    // ============================================================================

    /**
     * Update user's first name
     * @param userId - the user ID
     * @param firstName - the first name
     * @return updated User
     */
    @PutMapping("/{id}/first-name")
    public ResponseEntity<User> updateFirstName(
            @PathVariable("id") Long userId,
            @RequestParam String firstName) {
        log.info("Updating first name for user ID: {}", userId);
        User updatedUser = userService.setFirstName(userId, firstName);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user's last name
     * @param userId - the user ID
     * @param lastName - the last name
     * @return updated User
     */
    @PutMapping("/{id}/last-name")
    public ResponseEntity<User> updateLastName(
            @PathVariable("id") Long userId,
            @RequestParam String lastName) {
        log.info("Updating last name for user ID: {}", userId);
        User updatedUser = userService.setLastName(userId, lastName);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user's bio
     * @param userId - the user ID
     * @param bio - the bio text
     * @return updated User
     */
    @PutMapping("/{id}/bio")
    public ResponseEntity<User> updateBio(
            @PathVariable("id") Long userId,
            @RequestParam String bio) {
        log.info("Updating bio for user ID: {}", userId);
        User updatedUser = userService.setBio(userId, bio);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user email
     * @param userId - the user ID
     * @param newEmail - the new email
     * @return updated User
     */
    @PutMapping("/{id}/email")
    public ResponseEntity<User> updateEmail(
            @PathVariable("id") Long userId,
            @RequestParam String newEmail) {
        log.info("Updating email for user ID: {}", userId);
        User updatedUser = userService.updateEmail(userId, newEmail);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update user password
     * @param userId - the user ID
     * @param oldPassword - the old password
     * @param newPassword - the new password
     * @return updated User
     */
    @PutMapping("/{id}/password")
    public ResponseEntity<User> updatePassword(
            @PathVariable("id") Long userId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        log.info("Updating password for user ID: {}", userId);
        User updatedUser = userService.updatePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Update full user profile
     * @param userId - the user ID
     * @param userDTO - the updated user data
     * @return updated User
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUserProfile(
            @PathVariable("id") Long userId,
            @Valid @RequestBody UserDTO userDTO) {
        log.info("Updating user profile for user ID: {}", userId);
        User updatedUser = userService.updateUserProfile(userId, userDTO);
        return ResponseEntity.ok(updatedUser);
    }

    // ============================================================================
    // Tag Following
    // ============================================================================

    /**
     * Follow a tag
     * @param userId - the user ID
     * @param tagId - the tag ID
     * @return no content response
     */
    @PostMapping("/{userId}/follow-tag/{tagId}")
    public ResponseEntity<Void> followTag(
            @PathVariable Long userId,
            @PathVariable Long tagId) {
        log.info("User {} following tag {}", userId, tagId);
        userService.followTag(userId, tagId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Unfollow a tag
     * @param userId - the user ID
     * @param tagId - the tag ID
     * @return no content response
     */
    @DeleteMapping("/{userId}/follow-tag/{tagId}")
    public ResponseEntity<Void> unfollowTag(
            @PathVariable Long userId,
            @PathVariable Long tagId) {
        log.info("User {} unfollowing tag {}", userId, tagId);
        userService.unfollowTag(userId, tagId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get followed tags for a user
     * @param userId - the user ID
     * @return list of tag names
     */
    @GetMapping("/{id}/followed-tags")
    public ResponseEntity<List<String>> getFollowedTags(@PathVariable("id") Long userId) {
        log.info("Fetching followed tags for user ID: {}", userId);
        List<String> tags = userService.getFollowedTags(userId);
        return ResponseEntity.ok(tags);
    }

    // ============================================================================
    // User Statistics
    // ============================================================================

    /**
     * Get question count for a user
     * @param userId - the user ID
     * @return question count
     */
    @GetMapping("/{id}/statistics/questions")
    public ResponseEntity<Integer> getQuestionCount(@PathVariable("id") Long userId) {
        log.info("Fetching question count for user ID: {}", userId);
        Integer count = userService.getQuestionCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get answer count for a user
     * @param userId - the user ID
     * @return answer count
     */
    @GetMapping("/{id}/statistics/answers")
    public ResponseEntity<Integer> getAnswerCount(@PathVariable("id") Long userId) {
        log.info("Fetching answer count for user ID: {}", userId);
        Integer count = userService.getAnswerCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get comment count for a user
     * @param userId - the user ID
     * @return comment count
     */
    @GetMapping("/{id}/statistics/comments")
    public ResponseEntity<Integer> getCommentCount(@PathVariable("id") Long userId) {
        log.info("Fetching comment count for user ID: {}", userId);
        Integer count = userService.getCommentCount(userId);
        return ResponseEntity.ok(count);
    }

    // ============================================================================
    // Delete User
    // ============================================================================

    /**
     * Delete a user
     * @param userId - the user ID
     * @return no content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {
        log.info("Deleting user with ID: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}