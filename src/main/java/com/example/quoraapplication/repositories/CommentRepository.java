package com.example.quoraapplication.repositories;


import com.example.quoraapplication.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // ============================================================================
    // Basic Find Methods
    // ============================================================================
    
    /**
     * Find comment by ID with author eagerly loaded
     */
    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAuthor(@Param("commentId") Long commentId);
    
    /**
     * Find comment by ID with all associations loaded
     */
    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "LEFT JOIN FETCH c.question " +
           "LEFT JOIN FETCH c.answer " +
           "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAllAssociations(@Param("commentId") Long commentId);
    
    // ============================================================================
    // Question Comments Methods
    // ============================================================================
    
    /**
     * Find all comments on a question
     * @param questionId the question's ID
     * @return list of comments on the question
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.question.id = :questionId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByQuestion(@Param("questionId") Long questionId);
    
    /**
     * Find all comments on a question with pagination
     * @param questionId the question's ID
     * @param pageable pagination information
     * @return paginated comments on the question
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.question.id = :questionId " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByQuestion(@Param("questionId") Long questionId, Pageable pageable);
    
    /**
     * Find all comments on a question with author eagerly loaded
     * @param questionId the question's ID
     * @return list of comments with author loaded
     */
    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "WHERE c.question.id = :questionId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByQuestionWithAuthor(@Param("questionId") Long questionId);
    
    /**
     * Count comments on a question
     * @param questionId the question's ID
     * @return number of comments on the question
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.question.id = :questionId")
    long countCommentsByQuestion(@Param("questionId") Long questionId);
    
    // ============================================================================
    // Answer Comments Methods
    // ============================================================================
    
    /**
     * Find all comments on an answer
     * @param answerId the answer's ID
     * @return list of comments on the answer
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.answer.id = :answerId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByAnswer(@Param("answerId") Long answerId);
    
    /**
     * Find all comments on an answer with pagination
     * @param answerId the answer's ID
     * @param pageable pagination information
     * @return paginated comments on the answer
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.answer.id = :answerId " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByAnswer(@Param("answerId") Long answerId, Pageable pageable);
    
    /**
     * Find all comments on an answer with author eagerly loaded
     * @param answerId the answer's ID
     * @return list of comments with author loaded
     */
    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "WHERE c.answer.id = :answerId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByAnswerWithAuthor(@Param("answerId") Long answerId);
    
    /**
     * Count comments on an answer
     * @param answerId the answer's ID
     * @return number of comments on the answer
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.answer.id = :answerId")
    long countCommentsByAnswer(@Param("answerId") Long answerId);
    
    // ============================================================================
    // User Comments Methods
    // ============================================================================
    
    /**
     * Find all comments made by a user
     * @param userId the user's ID
     * @return list of comments made by user
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.author.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findCommentsByUser(@Param("userId") Long userId);
    
    /**
     * Find all comments made by a user with pagination
     * @param userId the user's ID
     * @param pageable pagination information
     * @return paginated comments made by user
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE c.author.id = :userId " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findCommentsByUser(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * Count comments made by a user
     * @param userId the user's ID
     * @return number of comments made by user
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :userId")
    long countCommentsByUser(@Param("userId") Long userId);
    
    // ============================================================================
    // Search Methods
    // ============================================================================
    
    /**
     * Search comments by text (case-insensitive)
     * @param searchTerm the search term
     * @return list of comments containing search term
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<Comment> searchByText(@Param("searchTerm") String searchTerm);
    
    /**
     * Search comments by text with pagination
     * @param searchTerm the search term
     * @param pageable pagination information
     * @return paginated search results
     */
    @Query("SELECT c FROM Comment c " +
           "WHERE LOWER(c.text) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> searchByText(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // ============================================================================
    // Statistics Methods
    // ============================================================================
    
    /**
     * Get total number of comments
     */
    long count();
    
    /**
     * Get most liked comments
     * @param limit number of comments to return
     * @return list of most liked comments
     */
    @Query("SELECT c FROM Comment c ORDER BY c.likeCount DESC LIMIT :limit")
    List<Comment> getMostLikedComments(@Param("limit") int limit);
    
    /**
     * Get recently created comments
     * @param limit number of comments to return
     * @return list of recently created comments
     */
    @Query("SELECT c FROM Comment c ORDER BY c.createdAt DESC LIMIT :limit")
    List<Comment> getRecentComments(@Param("limit") int limit);
    
    /**
     * Get recently updated comments
     * @param limit number of comments to return
     * @return list of recently updated comments
     */
    @Query("SELECT c FROM Comment c ORDER BY c.updatedAt DESC LIMIT :limit")
    List<Comment> getRecentlyUpdatedComments(@Param("limit") int limit);
    
    /**
     * Check if user has commented on a question
     * @param userId the user's ID
     * @param questionId the question's ID
     * @return true if user has commented, false otherwise
     */
    @Query("SELECT COUNT(c) > 0 FROM Comment c " +
           "WHERE c.author.id = :userId AND c.question.id = :questionId")
    boolean userHasCommentedOnQuestion(@Param("userId") Long userId, @Param("questionId") Long questionId);
    
    /**
     * Check if user has commented on an answer
     * @param userId the user's ID
     * @param answerId the answer's ID
     * @return true if user has commented, false otherwise
     */
    @Query("SELECT COUNT(c) > 0 FROM Comment c " +
           "WHERE c.author.id = :userId AND c.answer.id = :answerId")
    boolean userHasCommentedOnAnswer(@Param("userId") Long userId, @Param("answerId") Long answerId);
    
    /**
     * Get total comments made on question and its answers
     * @param questionId the question's ID
     * @return total comment count
     */
    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.question.id = :questionId " +
           "OR c.answer.id IN (SELECT a.id FROM Answer a WHERE a.question.id = :questionId)")
    long countTotalCommentsOnQuestion(@Param("questionId") Long questionId);
}