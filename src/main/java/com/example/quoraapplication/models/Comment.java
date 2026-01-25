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
@Table(name = "comments", indexes = {
        @Index(name = "idx_question_id", columnList = "question_id"),
        @Index(name = "idx_answer_id", columnList = "answer_id"),
        @Index(name = "idx_author_id", columnList = "author_id"),
        @Index(name = "idx_parent_comment_id", columnList = "parent_comment_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

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
     * The question this comment is on
     * REQUIRED: Every comment belongs to a question
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"comments", "answers", "tags", "likedByUsers"})
    private Question question;

    /**
     * The answer this comment is on (optional)
     * A comment can be on either a question or an answer, but not both
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id")
    @JsonIgnoreProperties({"comments", "question", "acceptedBy"})
    private Answer answer;

    /**
     * The user who created this comment
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"comments", "questions", "answers", "followedTags", "password", "email"})
    private User author;

    /**
     * Parent comment for reply threading
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({"replies", "likedBy", "parentComment"})
    private Comment parentComment;

    /**
     * Replies to this comment
     */
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"parentComment", "likedBy"})
    private Set<Comment> replies = new HashSet<>();

    /**
     * Users who have liked this comment
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"likedComments", "questions", "answers", "comments"})
    private Set<User> likedBy = new HashSet<>();

    // ============================================================================
    // Setter Methods with Bidirectional Relationship Management
    // ============================================================================

    /**
     * Set the question for this comment
     * Manages bidirectional relationship
     */
    public void setQuestion(Question question) {
        if (this.question != null && this.question.getComments() != null) {
            this.question.getComments().remove(this);
        }
        this.question = question;
        if (question != null && !question.getComments().contains(this)) {
            question.getComments().add(this);
        }
    }

    /**
     * Set the answer for this comment
     * Manages bidirectional relationship
     */
    public void setAnswer(Answer answer) {
        if (this.answer != null && this.answer.getComments() != null) {
            this.answer.getComments().remove(this);
        }
        this.answer = answer;
        if (answer != null && !answer.getComments().contains(this)) {
            answer.getComments().add(this);
        }
    }

    /**
     * Set the author of this comment
     * Manages bidirectional relationship
     */
    public void setAuthor(User author) {
        if (this.author != null && this.author.getComments() != null) {
            this.author.getComments().remove(this);
        }
        this.author = author;
        if (author != null && !author.getComments().contains(this)) {
            author.getComments().add(this);
        }
    }

    /**
     * Set the parent comment for reply threading
     * Manages bidirectional relationship
     */
    public void setParentComment(Comment parentComment) {
        if (this.parentComment != null && this.parentComment.getReplies() != null) {
            this.parentComment.getReplies().remove(this);
        }
        this.parentComment = parentComment;
        if (parentComment != null && !parentComment.getReplies().contains(this)) {
            parentComment.getReplies().add(this);
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Add a reply to this comment
     */
    public void addReply(Comment reply) {
        if (this.replies == null) {
            this.replies = new HashSet<>();
        }
        this.replies.add(reply);
        reply.setParentComment(this);
    }

    /**
     * Remove a reply from this comment
     */
    public void removeReply(Comment reply) {
        if (this.replies != null) {
            this.replies.remove(reply);
            reply.setParentComment(null);
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
     * Check if a user has liked this comment
     */
    public boolean isLikedBy(User user) {
        return this.likedBy != null && this.likedBy.contains(user);
    }

    /**
     * Get number of replies
     */
    public Integer getReplyCount() {
        return this.replies != null ? this.replies.size() : 0;
    }

    /**
     * Get the like count
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
        if (!(o instanceof Comment)) return false;
        Comment comment = (Comment) o;
        return Objects.equals(getId(), comment.getId());
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
        return "Comment{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", likeCount=" + likeCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}