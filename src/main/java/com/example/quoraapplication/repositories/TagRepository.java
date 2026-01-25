package com.example.quoraapplication.repositories;

import com.example.quoraapplication.models.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    

    Optional<Tag> findByName(String name);
    

    boolean existsByName(String name);
    

    List<Tag> findAllByOrderByNameAsc();
    

    @Query("SELECT t FROM Tag t ORDER BY t.followerCount DESC")
    List<Tag> findAllByPopularity();
    

    @Query("SELECT t FROM Tag t ORDER BY t.questionCount DESC")
    List<Tag> findAllByUsage();
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.followers " +
           "WHERE t.id = :tagId")
    Optional<Tag> findByIdWithFollowers(@Param("tagId") Long tagId);
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.questions " +
           "WHERE t.id = :tagId")
    Optional<Tag> findByIdWithQuestions(@Param("tagId") Long tagId);
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.followers " +
           "LEFT JOIN FETCH t.questions " +
           "WHERE t.id = :tagId")
    Optional<Tag> findByIdWithAllAssociations(@Param("tagId") Long tagId);
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.followers " +
           "WHERE t.name = :name")
    Optional<Tag> findByNameWithFollowers(@Param("name") String name);
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.questions " +
           "WHERE t.name = :name")
    Optional<Tag> findByNameWithQuestions(@Param("name") String name);
    

    @Query("SELECT DISTINCT t FROM Tag t " +
           "LEFT JOIN FETCH t.followers " +
           "LEFT JOIN FETCH t.questions " +
           "WHERE t.name = :name")
    Optional<Tag> findByNameWithAllAssociations(@Param("name") String name);
    

    @Query("SELECT t FROM Tag t " +
           "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.name ASC")
    List<Tag> searchByName(@Param("searchTerm") String searchTerm);
    

    @Query("SELECT t FROM Tag t " +
           "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.name ASC")
    List<Tag> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    

    @Query("SELECT t FROM Tag t ORDER BY t.followerCount DESC LIMIT :limit")
    List<Tag> getTopTagsByFollowers(@Param("limit") int limit);
    

    @Query("SELECT t FROM Tag t ORDER BY t.questionCount DESC LIMIT :limit")
    List<Tag> getTopTagsByQuestions(@Param("limit") int limit);
    

    @Query("SELECT COUNT(u) > 0 FROM Tag t " +
           "JOIN t.followers u " +
           "WHERE t.id = :tagId AND u.id = :userId")
    boolean isFollowedByUser(@Param("tagId") Long tagId, @Param("userId") Long userId);
    

    @Query("SELECT COUNT(u) FROM Tag t " +
           "JOIN t.followers u " +
           "WHERE t.id = :tagId")
    long countFollowers(@Param("tagId") Long tagId);
    

    @Query("SELECT COUNT(q) FROM Tag t " +
           "JOIN t.questions q " +
           "WHERE t.id = :tagId")
    long countQuestions(@Param("tagId") Long tagId);
    

    long count();
    

    @Query("SELECT t FROM Tag t " +
           "WHERE t.createdAt >= :startDate AND t.createdAt <= :endDate " +
           "ORDER BY t.createdAt DESC")
    List<Tag> findTagsCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate,
                                     @Param("endDate") java.time.LocalDateTime endDate);
    

    @Query("SELECT t FROM Tag t ORDER BY t.updatedAt DESC LIMIT :limit")
    List<Tag> getRecentlyUpdatedTags(@Param("limit") int limit);
    

    @Query("SELECT t FROM Tag t ORDER BY t.createdAt DESC LIMIT :limit")
    List<Tag> getRecentlyCreatedTags(@Param("limit") int limit);
    

    @Query("SELECT t FROM Tag t " +
           "JOIN t.followers u " +
           "WHERE u.id = :userId " +
           "ORDER BY t.name ASC")
    List<Tag> findTagsFollowedByUser(@Param("userId") Long userId);
    

    @Query("SELECT t FROM Tag t " +
           "JOIN t.questions q " +
           "WHERE q.id = :questionId " +
           "ORDER BY t.name ASC")
    List<Tag> findTagsForQuestion(@Param("questionId") Long questionId);
}