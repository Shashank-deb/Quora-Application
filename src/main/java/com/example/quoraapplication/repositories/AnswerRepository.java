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
    // Find Answers by Question
    // ============================================================================

    /**
     * Find all answers for a specific question, ordered by creation date (descending)
     */
    Page<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId, Pageable pageable);

    /**
     * Find all accepted answers for a specific question with pagination
     */
    Page<Answer> findByQuestionIdAndIsAcceptedTrue(Long questionId, Pageable pageable);

    /**
     * Find all accepted answers for a question
     */
    List<Answer> findByQuestionIdAndIsAcceptedTrue(Long questionId);

    /**
     * Find all answers for a specific question
     */
    List<Answer> findByQuestionId(Long questionId);

    /**
     * Count answers for a question
     */
    long countByQuestionId(Long questionId);

    // ============================================================================
    // Find Answers by Author
    // ============================================================================

    /**
     * Find all answers by a specific author, ordered by creation date (descending)
     */
    Page<Answer> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    /**
     * Find all answers by a specific author
     */
    List<Answer> findByAuthorId(Long authorId);

    /**
     * Count answers by author
     */
    long countByAuthorId(Long authorId);

    // ============================================================================
    // Find Accepted Answers
    // ============================================================================

    /**
     * Find all accepted answers across all questions
     */
    @Query("SELECT a FROM Answer a WHERE a.isAccepted = true ORDER BY a.createdAt DESC")
    List<Answer> findAllAcceptedAnswers();

    /**
     * Find accepted answers with pagination
     */
    Page<Answer> findByIsAcceptedTrue(Pageable pageable);

    /**
     * Check if an answer is accepted
     */
    @Query("SELECT a.isAccepted FROM Answer a WHERE a.id = :answerId")
    Optional<Boolean> isAnswerAccepted(@Param("answerId") Long answerId);

    // ============================================================================
    // Find by Status
    // ============================================================================

    /**
     * Find answers by acceptance status
     */
    List<Answer> findByIsAccepted(Boolean isAccepted);

    /**
     * Find recently created answers
     */
    @Query("SELECT a FROM Answer a ORDER BY a.createdAt DESC")
    Page<Answer> findRecentAnswers(Pageable pageable);

    /**
     * Find most liked answers
     */
    @Query("SELECT a FROM Answer a ORDER BY a.likeCount DESC")
    Page<Answer> findMostLikedAnswers(Pageable pageable);

    // ============================================================================
    // Find Answers with Likes
    // ============================================================================

    /**
     * Find answers liked by a specific user (using JPA relationship)
     */
    @Query("SELECT DISTINCT a FROM Answer a JOIN a.likedBy u WHERE u.id = :userId ORDER BY a.createdAt DESC")
    Page<Answer> findAnswersLikedByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count likes for an answer
     */
    @Query("SELECT SIZE(a.likedBy) FROM Answer a WHERE a.id = :answerId")
    int countLikesForAnswer(@Param("answerId") Long answerId);

    // ============================================================================
    // Find Comments on Answers
    // ============================================================================

    /**
     * Find answers that have comments
     */
    @Query("SELECT DISTINCT a FROM Answer a WHERE SIZE(a.comments) > 0 ORDER BY a.createdAt DESC")
    Page<Answer> findAnswersWithComments(Pageable pageable);

    /**
     * Count comments for an answer
     */
    @Query("SELECT SIZE(a.comments) FROM Answer a WHERE a.id = :answerId")
    int countCommentsForAnswer(@Param("answerId") Long answerId);

    // ============================================================================
    // Complex Queries
    // ============================================================================

    /**
     * Find answers by question with acceptance and like count consideration
     */
    @Query("SELECT a FROM Answer a WHERE a.question.id = :questionId " +
           "ORDER BY a.isAccepted DESC, a.likeCount DESC, a.createdAt DESC")
    Page<Answer> findByQuestionIdOrderedByRelevance(@Param("questionId") Long questionId, Pageable pageable);

    /**
     * Find answers created after a specific date
     */
    @Query("SELECT a FROM Answer a WHERE a.createdAt > CURRENT_TIMESTAMP - 1 DAY ORDER BY a.createdAt DESC")
    List<Answer> findRecentAnswersLastDay();

    /**
     * Check if a user has answered a specific question
     */
    @Query("SELECT COUNT(a) > 0 FROM Answer a WHERE a.question.id = :questionId AND a.author.id = :userId")
    boolean hasUserAnsweredQuestion(@Param("questionId") Long questionId, @Param("userId") Long userId);
}