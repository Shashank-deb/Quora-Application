// File: src/main/java/com/example/quoraapplication/model/Comment.java

package com.example.quoraapplication.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_question_id", columnList = "question_id"),
        @Index(name = "idx_answer_id", columnList = "answer_id"),
        @Index(name = "idx_author_id", columnList = "author_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Comment text is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * The question this comment is on (optional if comment is on answer)
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question;

    /**
     * The answer this comment is on (optional if comment is on question)
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    private Answer answer;

    /**
     * The user who created this comment
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Set the question this comment is on
     */
    public void setQuestion(Question question) {
        this.question = question;
        if (question != null && !question.getComments().contains(this)) {
            question.getComments().add(this);
        }
    }

    /**
     * Set the answer this comment is on
     */
    public void setAnswer(Answer answer) {
        this.answer = answer;
        if (answer != null && !answer.getComments().contains(this)) {
            answer.getComments().add(this);
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
     * Check if this comment is on a question
     */
    public boolean isQuestionComment() {
        return this.question != null;
    }

    /**
     * Check if this comment is on an answer
     */
    public boolean isAnswerComment() {
        return this.answer != null;
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
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================================================
    // toString (exclude circular references)
    // ============================================================================

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", likeCount=" + likeCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", author=" + (author != null ? author.getUsername() : "null") +
                '}';
    }
}