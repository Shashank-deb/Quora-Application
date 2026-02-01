package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Question Repository with N+1 Query Fixes
 * 
 * All queries use LEFT JOIN FETCH to load related entities
 * This prevents the N+1 query problem
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find question by ID with all associations loaded
     * Prevents N+1 queries when accessing: user, tags, answers
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "LEFT JOIN FETCH q.tags " +
           "WHERE q.id = :questionId")
    Optional<Question> findByIdWithAssociations(@Param("questionId") Long questionId);

    /**
     * Find all questions with user and tags loaded
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "LEFT JOIN FETCH q.tags")
    Page<Question> findAllWithAssociations(Pageable pageable);

    /**
     * Find questions by tags
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "LEFT JOIN FETCH q.tags t " +
           "WHERE t.id IN :tagIds")
    Page<Question> findQuestionByTags(
            @Param("tagIds") Set<Long> tagIds, Pageable pageable);

    /**
     * Find recent questions
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "ORDER BY q.createdAt DESC")
    Page<Question> findRecentQuestions(Pageable pageable);

    /**
     * Find most viewed questions
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "ORDER BY q.viewCount DESC")
    Page<Question> findMostViewedQuestions(Pageable pageable);

    /**
     * Find most liked questions
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "ORDER BY q.likeCount DESC")
    Page<Question> findMostLikedQuestions(Pageable pageable);

    /**
     * Find unanswered questions
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "WHERE q.answerCount = 0 " +
           "ORDER BY q.createdAt DESC")
    Page<Question> findUnansweredQuestions(Pageable pageable);

    /**
     * Search questions by title or content
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "LEFT JOIN FETCH q.tags " +
           "WHERE LOWER(q.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(q.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY q.createdAt DESC")
    Page<Question> searchByTitleOrContent(
            @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find questions by specific author
     */
    @Query("SELECT DISTINCT q FROM Question q " +
           "LEFT JOIN FETCH q.user " +
           "LEFT JOIN FETCH q.tags " +
           "WHERE q.user.id = :userId " +
           "ORDER BY q.createdAt DESC")
    Page<Question> findByUserIdWithAssociations(
            @Param("userId") Long userId, Pageable pageable);
}