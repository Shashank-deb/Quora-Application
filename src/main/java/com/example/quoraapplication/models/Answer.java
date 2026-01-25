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
        @Index(name = "idx_accepted", columnList = "is_accepted"),
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

    @Column(name = "is_accepted", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAccepted = false;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * The question this answer is for
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"answers", "comments", "tags", "likedByUsers", "user"})
    private Question question;

    /**
     * The user who wrote this answer
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"answers", "questions", "comments", "followedTags", "password", "email", "likedAnswers", "likedQuestions"})
    private User author;

    /**
     * The user who marked this answer as accepted (if any)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by_id")
    @JsonIgnoreProperties({"answers", "questions", "comments", "followedTags", "password", "email", "likedAnswers", "likedQuestions"})
    private User acceptedBy;

    /**
     * Comments on this answer
     */
    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"answer", "question", "author", "parentComment", "likedBy"})
    private Set<Comment> comments = new HashSet<>();

    /**
     * Users who have liked this answer
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "answer_likes",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"likedAnswers", "likedQuestions", "answers", "questions", "comments"})
    private Set<User> likedBy = new HashSet<>();

    // ============================================================================
    // Setter Methods with Bidirectional Relationship Management
    // ============================================================================

    /**
     * Set the question for this answer
     * Manages bidirectional relationship
     */
    public void setQuestion(Question question) {
        if (this.question != null && this.question.getAnswers() != null) {
            this.question.getAnswers().remove(this);
        }
        this.question = question;
        if (question != null && !question.getAnswers().contains(this)) {
            question.getAnswers().add(this);
        }
    }

    /**
     * Set the author of this answer
     * Manages bidirectional relationship
     */
    public void setAuthor(User author) {
        if (this.author != null && this.author.getAnswers() != null) {
            this.author.getAnswers().remove(this);
        }
        this.author = author;
        if (author != null && !author.getAnswers().contains(this)) {
            author.getAnswers().add(this);
        }
    }

    /**
     * Set the user who accepted this answer
     * Manages bidirectional relationship
     */
    public void setAcceptedBy(User acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

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
    public void markAsAccepted(User acceptedBy) {
        this.isAccepted = true;
        this.acceptedBy = acceptedBy;
    }

    /**
     * Unmark this answer as accepted
     */
    public void unmarkAsAccepted() {
        this.isAccepted = false;
        this.acceptedBy = null;
    }

    /**
     * Check if a user has liked this answer
     */
    public boolean isLikedBy(User user) {
        return this.likedBy != null && this.likedBy.contains(user);
    }

    /**
     * Get the like count
     */
    public Integer getLikeCount() {
        return this.likeCount != null ? this.likeCount : 0;
    }

    /**
     * Get number of comments
     */
    public Integer getCommentCount() {
        return this.comments != null ? this.comments.size() : 0;
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
                ", content='" + content + '\'' +
                ", isAccepted=" + isAccepted +
                ", likeCount=" + likeCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}