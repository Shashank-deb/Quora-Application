package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.UserDTO;
import com.example.quoraapplication.dtos.UserResponseDTO;
import com.example.quoraapplication.exception.ResourceNotFoundException;
import com.example.quoraapplication.exception.UserAlreadyExistsException;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.TagRepository;
import com.example.quoraapplication.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, 
                      TagRepository tagRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============================================================================
    // Create User
    // ============================================================================

    /**
     * Create a new user
     */
    public User createUser(UserDTO userDTO) {
        log.info("Creating new user with username: {}", userDTO.getUsername());

        // Check if user already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDTO.getUsername());
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDTO.getEmail());
        }

        // Create new user
        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(User.Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    // ============================================================================
    // Retrieve User
    // ============================================================================

    /**
     * Get user by ID
     */
    public Optional<UserResponseDTO> getUserById(Long userId) {
        log.info("Fetching user with ID: {}", userId);
        return userRepository.findById(userId)
                .map(this::convertToResponseDTO);
    }

    /**
     * Get user by username
     */
    public Optional<UserResponseDTO> getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);
        return userRepository.findByUsername(username)
                .map(this::convertToResponseDTO);
    }

    /**
     * Get user by email
     */
    public Optional<UserResponseDTO> getUserByEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return userRepository.findByEmail(email)
                .map(this::convertToResponseDTO);
    }

    /**
     * Get all users with pagination - CORRECTED SIGNATURE
     * Fixed: Now accepts page (int) and size (int) parameters
     */
    public Page<UserResponseDTO> getAllUsers(int page, int size) {
        log.info("Fetching all users with pagination - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> users = userRepository.findAll(pageable);

        return users.map(this::convertToResponseDTO);
    }

    // ============================================================================
    // Update User Profile
    // ============================================================================

    /**
     * Update user's first name
     */
    public User setFirstName(Long userId, String firstName) {
        log.info("Updating first name for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(firstName);
        User updatedUser = userRepository.save(user);

        log.info("First name updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    /**
     * Update user's last name
     */
    public User setLastName(Long userId, String lastName) {
        log.info("Updating last name for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setLastName(lastName);
        User updatedUser = userRepository.save(user);

        log.info("Last name updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    /**
     * Update user's bio
     */
    public User setBio(Long userId, String bio) {
        log.info("Updating bio for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setBio(bio);
        User updatedUser = userRepository.save(user);

        log.info("Bio updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    /**
     * Update user's email
     */
    public User updateEmail(Long userId, String newEmail) {
        log.info("Updating email for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new UserAlreadyExistsException("Email already exists: " + newEmail);
        }

        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);

        log.info("Email updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    /**
     * Update user's password
     */
    public User updatePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Updating password for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);

        log.info("Password updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    /**
     * Update full user profile
     */
    public User updateUserProfile(Long userId, UserDTO userDTO) {
        log.info("Updating user profile for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (userDTO.getFirstName() != null) {
            user.setFirstName(userDTO.getFirstName());
        }
        if (userDTO.getLastName() != null) {
            user.setLastName(userDTO.getLastName());
        }
        if (userDTO.getBio() != null) {
            user.setBio(userDTO.getBio());
        }

        User updatedUser = userRepository.save(user);
        log.info("User profile updated successfully for user ID: {}", userId);
        return updatedUser;
    }

    // ============================================================================
    // Tag Following
    // ============================================================================

    /**
     * Follow a tag - CORRECTED SIGNATURE
     * Fixed: Now accepts tagId (Long) and userId (Long) parameters
     */
    public void followTag(Long userId, Long tagId) {
        log.info("User {} following tag {}", userId, tagId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        user.followTag(tag);
        userRepository.save(user);

        log.info("User {} successfully followed tag {}", userId, tagId);
    }

    /**
     * Unfollow a tag
     */
    public void unfollowTag(Long userId, Long tagId) {
        log.info("User {} unfollowing tag {}", userId, tagId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found with id: " + tagId));

        user.unfollowTag(tag);
        userRepository.save(user);

        log.info("User {} successfully unfollowed tag {}", userId, tagId);
    }

    /**
     * Get followed tags for a user
     */
    public List<String> getFollowedTags(Long userId) {
        log.info("Fetching followed tags for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getFollowedTags().stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    // ============================================================================
    // User Statistics
    // ============================================================================

    /**
     * Get total questions asked by user
     */
    public Integer getQuestionCount(Long userId) {
        log.info("Fetching question count for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getQuestions() != null ? user.getQuestions().size() : 0;
    }

    /**
     * Get total answers given by user
     */
    public Integer getAnswerCount(Long userId) {
        log.info("Fetching answer count for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getAnswers() != null ? user.getAnswers().size() : 0;
    }

    /**
     * Get total comments by user
     */
    public Integer getCommentCount(Long userId) {
        log.info("Fetching comment count for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getComments() != null ? user.getComments().size() : 0;
    }

    // ============================================================================
    // Delete User
    // ============================================================================

    /**
     * Delete a user
     */
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully with ID: {}", userId);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .role(user.getRole().toString())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .questionCount(user.getQuestions() != null ? user.getQuestions().size() : 0)
                .answerCount(user.getAnswers() != null ? user.getAnswers().size() : 0)
                .commentCount(user.getComments() != null ? user.getComments().size() : 0)
                .build();
    }
}