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

/**
 * Answer Repository with N+1 Query Fixes
 * 
 * All queries use LEFT JOIN FETCH to load related entities
 * This prevents the N+1 query problem
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    // ============================================================================
    // ✅ FIXED: Answers with Author and Question loaded
    // Prevents N+1 query problem
    // ============================================================================

    /**
     * Find all answers for a specific question with author eagerly loaded
     * Uses LEFT JOIN FETCH to prevent N+1 queries
     * 
     * Before: 1 query for answers + N queries for each author = N+1 total
     * After: 1 query with JOIN to get everything = 1 query total ✅
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "LEFT JOIN FETCH a.question " +
           "WHERE a.question.id = :questionId " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> findByQuestionIdWithAssociations(
            @Param("questionId") Long questionId, Pageable pageable);

    /**
     * Simpler version: answers ordered by creation date with associations
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.question.id = :questionId " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> findByQuestionIdOrderByCreatedAtDesc(
            @Param("questionId") Long questionId, Pageable pageable);

    /**
     * Find accepted answers with associations
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.question.id = :questionId AND a.isAccepted = true " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> findByQuestionIdAndIsAcceptedTrue(
            @Param("questionId") Long questionId, Pageable pageable);

    /**
     * Find accepted answers (without pagination)
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.question.id = :questionId AND a.isAccepted = true " +
           "ORDER BY a.createdAt DESC")
    List<Answer> findByQuestionIdAndIsAcceptedTrueList(Long questionId);

    /**
     * Find all answers for a specific question (without pagination)
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.question.id = :questionId " +
           "ORDER BY a.createdAt DESC")
    List<Answer> findByQuestionId(Long questionId);

    /**
     * Count answers for a question
     */
    long countByQuestionId(Long questionId);

    // ========== ANSWERS BY AUTHOR ==========

    /**
     * Find all answers by a specific author with associations
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "LEFT JOIN FETCH a.question " +
           "WHERE a.author.id = :authorId " +
           "ORDER BY a.createdAt DESC")
    Page<Answer> findByAuthorIdOrderByCreatedAtDesc(
            @Param("authorId") Long authorId, Pageable pageable);

    /**
     * Find all answers by a specific author (without pagination)
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
           "LEFT JOIN FETCH a.author " +
           "WHERE a.author.id = :authorId " +
           "ORDER BY a.createdAt DESC")
    List<Answer> findByAuthorId(Long authorId);

    /**
     * Count answers by author
     */
    long countByAuthorId(Long authorId);

    /**
     * Count answers by question and author
     * Used to check if user already answered this question
     */
    @Query("SELECT COUNT(a) FROM Answer a " +
            "WHERE a.question.id = :questionId AND a.author.id = :authorId")
    long countByQuestionIdAndAuthorId(
            @Param("questionId") Long questionId,
            @Param("authorId") Long authorId);

    /**
     * Find most recent answers with pagination
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
            "LEFT JOIN FETCH a.author " +
            "ORDER BY a.createdAt DESC")
    Page<Answer> findRecentAnswers(Pageable pageable);

    /**
     * Find answers by question with like count ordering
     */
    @Query("SELECT DISTINCT a FROM Answer a " +
            "LEFT JOIN FETCH a.author " +
            "WHERE a.question.id = :questionId " +
            "ORDER BY a.likeCount DESC, a.createdAt DESC")
    Page<Answer> findByQuestionIdOrderByLikes(
            @Param("questionId") Long questionId, Pageable pageable);
}