package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // ============================================================================
    // Find By ID Methods
    // ============================================================================

    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAuthor(@Param("commentId") Long commentId);

    @Query("SELECT DISTINCT c FROM Comment c " +
           "LEFT JOIN FETCH c.author " +
           "LEFT JOIN FETCH c.answer " +
           "LEFT JOIN FETCH c.likedBy " +
           "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithAllAssociations(@Param("commentId") Long commentId);

    // ============================================================================
    // Answer Comments Methods
    // ============================================================================

    @Query("SELECT c FROM Comment c " +
           "WHERE c.answer.id = :answerId AND c.parentComment IS NULL " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByAnswerId(@Param("answerId") Long answerId);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.answer.id = :answerId AND c.parentComment IS NULL " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByAnswerId(@Param("answerId") Long answerId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.answer.id = :answerId AND c.parentComment IS NULL")
    long countByAnswerId(@Param("answerId") Long answerId);

    // ============================================================================
    // Nested Comments (Replies) Methods
    // ============================================================================

    @Query("SELECT c FROM Comment c " +
           "WHERE c.parentComment.id = :parentCommentId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.parentComment.id = :parentCommentId " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByParentCommentId(@Param("parentCommentId") Long parentCommentId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.parentComment.id = :parentCommentId")
    long countByParentCommentId(@Param("parentCommentId") Long parentCommentId);

    // ============================================================================
    // User Comments Methods
    // ============================================================================

    @Query("SELECT c FROM Comment c " +
           "WHERE c.author.id = :userId " +
           "ORDER BY c.createdAt DESC")
    List<Comment> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.author.id = :userId " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // ============================================================================
    // Search Methods
    // ============================================================================

    @Query("SELECT c FROM Comment c " +
           "WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    List<Comment> searchByContent(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Comment c " +
           "WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY c.createdAt DESC")
    Page<Comment> searchByContent(@Param("searchTerm") String searchTerm, Pageable pageable);

    // ============================================================================
    // Statistics Methods
    // ============================================================================

    @Query("SELECT c FROM Comment c " +
           "ORDER BY c.likeCount DESC")
    List<Comment> getMostLikedComments(Pageable pageable);

    @Query("SELECT c FROM Comment c " +
           "WHERE c.answer.id = :answerId " +
           "ORDER BY c.likeCount DESC")
    List<Comment> getMostLikedCommentsByAnswer(@Param("answerId") Long answerId, Pageable pageable);

    @Query("SELECT COUNT(u) > 0 FROM Comment c " +
           "JOIN c.likedBy u " +
           "WHERE c.id = :commentId AND u.id = :userId")
    boolean userHasLikedComment(@Param("commentId") Long commentId, @Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.answer.id = :answerId")
    long countTotalCommentsOnAnswer(@Param("answerId") Long answerId);

    @Query("SELECT COUNT(c) FROM Comment c " +
           "WHERE c.parentComment.id = :commentId")
    long countReplies(@Param("commentId") Long commentId);
}