package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // ============================================================================
    // Basic Find Methods
    // ============================================================================
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username OR email
     * Useful for login when user might use either
     */
    Optional<User> findByUsernameOrEmail(String username, String email);
    
    // ============================================================================
    // Existence Check Methods
    // ============================================================================
    
    /**
     * Check if username exists
     */
    boolean existsByUsername(String username);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    // ============================================================================
    // Eager Loading Methods (Prevents LazyInitializationException)
    // ============================================================================
    
    /**
     * Find user by ID with eagerly loaded tags
     * Solves N+1 problem by fetching tags in single query
     * Use this when you need to access user.getFollowedTags()
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.followedTags " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithTags(@Param("userId") Long userId);
    
    /**
     * Find user by ID with eagerly loaded questions
     * Use this when you need to access user.getQuestions()
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.questions " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithQuestions(@Param("userId") Long userId);
    
    /**
     * Find user by ID with eagerly loaded answers
     * Use this when you need to access user.getAnswers()
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.answers " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithAnswers(@Param("userId") Long userId);
    
    /**
     * Find user by ID with eagerly loaded comments
     * Use this when you need to access user.getComments()
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.comments " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithComments(@Param("userId") Long userId);
    
    /**
     * Find user by ID with all associations eagerly loaded
     * Use this when you need to access all collections
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.followedTags " +
           "LEFT JOIN FETCH u.questions " +
           "LEFT JOIN FETCH u.answers " +
           "LEFT JOIN FETCH u.comments " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithAllAssociations(@Param("userId") Long userId);
    
    /**
     * Find user by username with tags eagerly loaded
     * Use this for profile pages where you need tags
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.followedTags " +
           "WHERE u.username = :username")
    Optional<User> findByUsernameWithTags(@Param("username") String username);
    
    /**
     * Find user by email with tags eagerly loaded
     * Use this when loading by email and need tags
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.followedTags " +
           "WHERE u.email = :email")
    Optional<User> findByEmailWithTags(@Param("email") String email);
    
    /**
     * Find user by username with all associations
     * Use this for detailed profile pages
     */
    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.followedTags " +
           "LEFT JOIN FETCH u.questions " +
           "LEFT JOIN FETCH u.answers " +
           "LEFT JOIN FETCH u.comments " +
           "WHERE u.username = :username")
    Optional<User> findByUsernameWithAllAssociations(@Param("username") String username);
    
    // ============================================================================
    // Search & Filter Methods
    // ============================================================================
    
    /**
     * Find all active users
     */
    @Query("SELECT u FROM User u WHERE u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findAllActiveUsers();
    
    /**
     * Find all users by role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findByRole(@Param("role") User.Role role);
    
    /**
     * Search users by username (case-insensitive)
     * Returns list of users whose username contains search term
     */
    @Query("SELECT u FROM User u " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY u.username ASC")
    List<User> searchByUsername(@Param("searchTerm") String searchTerm);
    
    /**
     * Search users by email (case-insensitive)
     */
    @Query("SELECT u FROM User u " +
           "WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY u.email ASC")
    List<User> searchByEmail(@Param("searchTerm") String searchTerm);
    
    /**
     * Search users by first or last name (case-insensitive)
     */
    @Query("SELECT u FROM User u " +
           "WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY u.firstName ASC")
    List<User> searchByName(@Param("searchTerm") String searchTerm);
    
    // ============================================================================
    // Relationship Check Methods
    // ============================================================================
    
    /**
     * Check if user follows a specific tag
     * @param userId the user's ID
     * @param tagId the tag's ID
     * @return true if user follows the tag, false otherwise
     */
    @Query("SELECT COUNT(t) > 0 FROM User u " +
           "JOIN u.followedTags t " +
           "WHERE u.id = :userId AND t.id = :tagId")
    boolean userFollowsTag(@Param("userId") Long userId, @Param("tagId") Long tagId);
    
    /**
     * Check if user has liked a question
     * @param userId the user's ID
     * @param questionId the question's ID
     * @return true if user has liked the question, false otherwise
     */
    @Query("SELECT COUNT(q) > 0 FROM User u " +
           "JOIN u.likedQuestions q " +
           "WHERE u.id = :userId AND q.id = :questionId")
    boolean userHasLikedQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);
    
    /**
     * Check if user has liked an answer
     * @param userId the user's ID
     * @param answerId the answer's ID
     * @return true if user has liked the answer, false otherwise
     */
    @Query("SELECT COUNT(a) > 0 FROM User u " +
           "JOIN u.likedAnswers a " +
           "WHERE u.id = :userId AND a.id = :answerId")
    boolean userHasLikedAnswer(@Param("userId") Long userId, @Param("answerId") Long answerId);
    
    // ============================================================================
    // Statistics Methods
    // ============================================================================
    
    /**
     * Get number of users
     */
    long count();
    
    /**
     * Get number of active users
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();
    
    /**
     * Get number of users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") User.Role role);
    
    /**
     * Get number of followers for a tag by user
     * @param userId the user's ID
     * @return number of tags this user follows
     */
    @Query("SELECT COUNT(t) FROM User u " +
           "JOIN u.followedTags t " +
           "WHERE u.id = :userId")
    long countFollowedTags(@Param("userId") Long userId);
    
    /**
     * Get number of questions created by user
     * @param userId the user's ID
     * @return number of questions created by user
     */
    @Query("SELECT COUNT(q) FROM User u " +
           "JOIN u.questions q " +
           "WHERE u.id = :userId")
    long countQuestionsByUser(@Param("userId") Long userId);
    
    /**
     * Get number of answers provided by user
     * @param userId the user's ID
     * @return number of answers provided by user
     */
    @Query("SELECT COUNT(a) FROM User u " +
           "JOIN u.answers a " +
           "WHERE u.id = :userId")
    long countAnswersByUser(@Param("userId") Long userId);
    
    /**
     * Get number of comments made by user
     * @param userId the user's ID
     * @return number of comments made by user
     */
    @Query("SELECT COUNT(c) FROM User u " +
           "JOIN u.comments c " +
           "WHERE u.id = :userId")
    long countCommentsByUser(@Param("userId") Long userId);
}