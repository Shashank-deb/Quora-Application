package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    
    // ============================================================================
    // Find Methods
    // ============================================================================
    
    /**
     * Find answers by question ID with pagination
     */
    Page<Answer> findByQuestionId(Long questionId, Pageable pageable);
    
    /**
     * Find all answers by question ID
     */
    List<Answer> findByQuestionId(Long questionId);
    
    /**
     * Find answer by ID with author eagerly loaded
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.id = :answerId")
    Optional<Answer> findByIdWithAuthor(@Param("answerId") Long answerId);
    
    /**
     * Find answer with all associations eagerly loaded
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "LEFT JOIN FETCH a.question " +
           "LEFT JOIN FETCH a.comments " +
           "WHERE a.id = :answerId")
    Optional<Answer> findByIdWithAllAssociations(@Param("answerId") Long answerId);
    
    // ============================================================================
    // Author/User Methods
    // ============================================================================
    
    /**
     * Find all answers by a specific author
     */
    List<Answer> findByAuthorId(Long authorId);
    
    /**
     * Find all answers by author with pagination
     */
    Page<Answer> findByAuthorId(Long authorId, Pageable pageable);
    
    /**
     * Count answers by author
     */
    long countByAuthorId(Long authorId);
    
    // ============================================================================
    // Count Methods
    // ============================================================================
    
    /**
     * Count answers for a question
     */
    long countByQuestionId(Long questionId);
    
    /**
     * Count accepted answers for a question
     */
    @Query("SELECT COUNT(a) FROM Answer a WHERE a.question.id = :questionId AND a.isAccepted = true")
    long countAcceptedAnswersByQuestion(@Param("questionId") Long questionId);
    
    // ============================================================================
    // Search Methods
    // ============================================================================
    
    /**
     * Search answers by content
     */
    @Query("SELECT a FROM Answer a " +
           "WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.createdAt DESC")
    List<Answer> searchByContent(@Param("searchTerm") String searchTerm);
    
    /**
     * Search answers by content with pagination
     */
    @Query("SELECT a FROM Answer a " +
           "WHERE LOWER(a.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ============================================================================
    // Statistics Methods
    // ============================================================================
    
    /**
     * Get most liked answers for a question
     */
    @Query("SELECT a FROM Answer a " +
           "WHERE a.question.id = :questionId " +
           "ORDER BY a.likeCount DESC")
    List<Answer> getMostLikedAnswersForQuestion(@Param("questionId") Long questionId, Pageable pageable);
    
    /**
     * Get recently created answers
     */
    @Query("SELECT a FROM Answer a ORDER BY a.createdAt DESC")
    List<Answer> getRecentAnswers(Pageable pageable);
    
    /**
     * Get accepted answer for a question
     */
    @Query("SELECT a FROM Answer a " +
           "WHERE a.question.id = :questionId AND a.isAccepted = true")
    Optional<Answer> getAcceptedAnswerForQuestion(@Param("questionId") Long questionId);
    
    /**
     * Check if user has liked an answer
     */
    @Query("SELECT COUNT(u) > 0 FROM Answer a " +
           "JOIN a.likedByUsers u " +
           "WHERE a.id = :answerId AND u.id = :userId")
    boolean userHasLikedAnswer(@Param("answerId") Long answerId, @Param("userId") Long userId);
    
    /**
     * Get user's liked answers
     */
    @Query("SELECT a FROM Answer a " +
           "JOIN a.likedByUsers u " +
           "WHERE u.id = :userId " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> getUserLikedAnswers(@Param("userId") Long userId, Pageable pageable);
}