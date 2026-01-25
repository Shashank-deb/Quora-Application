// File: src/main/java/com/example/quoraapplication/model/Answer.java

package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "answers", indexes = {
        @Index(name = "idx_question_id", columnList = "question_id"),
        @Index(name = "idx_author_id", columnList = "author_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Boolean isAccepted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * The question this answer is for
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"answers", "comments", "tags"})
    private Question question;

    /**
     * The user who created this answer
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"followedTags", "password", "email", "questions", "answers", "comments"})
    private User author;

    /**
     * Comments on this answer
     * Lazy loading by default
     */
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"answer"})
    private Set<Comment> comments = new HashSet<>();

    /**
     * Users who have liked this answer
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "likedAnswers")
    @JsonIgnoreProperties({"likedQuestions", "likedAnswers"})
    private Set<User> likedByUsers = new HashSet<>();

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Set the question this answer is for
     */
    public void setQuestion(Question question) {
        this.question = question;
        if (question != null && !question.getAnswers().contains(this)) {
            question.getAnswers().add(this);
        }
    }

    /**
     * Set the author of this answer
     */
    public void setAuthor(User author) {
        this.author = author;
        if (author != null && !author.getAnswers().contains(this)) {
            author.getAnswers().add(this);
        }
    }

    /**
     * Add a comment to this answer
     */
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new HashSet<>();
        }
        this.comments.add(comment);
        comment.setAnswer(this);
    }

    /**
     * Remove a comment from this answer
     */
    public void removeComment(Comment comment) {
        if (this.comments != null) {
            this.comments.remove(comment);
            comment.setAnswer(null);
        }
    }

    /**
     * Increment like count
     */
    public void incrementLikeCount() {
        if (this.likeCount == null) {
            this.likeCount = 0;
        }
        this.likeCount++;
    }

    /**
     * Decrement like count
     */
    public void decrementLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }

    /**
     * Mark this answer as accepted
     */
    public void markAsAccepted() {
        this.isAccepted = true;
    }

    /**
     * Mark this answer as not accepted
     */
    public void markAsNotAccepted() {
        this.isAccepted = false;
    }

    /**
     * Check if user has liked this answer
     */
    public boolean isLikedBy(User user) {
        return this.likedByUsers != null && this.likedByUsers.contains(user);
    }

    /**
     * Get number of comments
     */
    public Integer getCommentCount() {
        return this.comments != null ? this.comments.size() : 0;
    }

    /**
     * Get number of likes
     */
    public Integer getLikeCount() {
        return this.likeCount != null ? this.likeCount : 0;
    }

    // ============================================================================
    // JPA Lifecycle Callbacks
    // ============================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (likeCount == null) {
            likeCount = 0;
        }
        if (isAccepted == null) {
            isAccepted = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================================================
    // equals & hashCode
    // ============================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Answer)) return false;
        Answer answer = (Answer) o;
        return Objects.equals(getId(), answer.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    // ============================================================================
    // toString
    // ============================================================================

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", likeCount=" + likeCount +
                ", isAccepted=" + isAccepted +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}