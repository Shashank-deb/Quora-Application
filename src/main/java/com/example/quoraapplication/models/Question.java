// File: src/main/java/com/example/quoraapplication/model/Question.java

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
@Table(name = "questions", indexes = {
        @Index(name = "idx_author_id", columnList = "author_id"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0;
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer answerCount = 0;
    
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
     * The user who created this question
     * Lazy loading by default
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"followedTags", "password", "email", "questions", "answers", "comments"})
    private User author;

    /**
     * Tags associated with this question
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"followers", "questions"})
    private Set<Tag> tags = new HashSet<>();

    /**
     * Answers to this question
     * Lazy loading by default
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"question"})
    private Set<Answer> answers = new HashSet<>();

    /**
     * Comments on this question
     * Lazy loading by default
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"question"})
    private Set<Comment> comments = new HashSet<>();

    /**
     * Users who have liked this question
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "likedQuestions")
    @JsonIgnoreProperties({"likedQuestions", "likedAnswers"})
    private Set<User> likedByUsers = new HashSet<>();

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Set the author of this question
     */
    public void setAuthor(User author) {
        this.author = author;
        if (author != null && !author.getQuestions().contains(this)) {
            author.getQuestions().add(this);
        }
    }

    /**
     * Add an answer to this question
     */
    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new HashSet<>();
        }
        this.answers.add(answer);
        answer.setQuestion(this);
        this.answerCount = this.answers.size();
    }

    /**
     * Remove an answer from this question
     */
    public void removeAnswer(Answer answer) {
        if (this.answers != null) {
            this.answers.remove(answer);
            answer.setQuestion(null);
            this.answerCount = this.answers.size();
        }
    }

    /**
     * Add a comment to this question
     */
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new HashSet<>();
        }
        this.comments.add(comment);
        comment.setQuestion(this);
    }

    /**
     * Remove a comment from this question
     */
    public void removeComment(Comment comment) {
        if (this.comments != null) {
            this.comments.remove(comment);
            comment.setQuestion(null);
        }
    }

    /**
     * Add a tag to this question
     */
    public void addTag(Tag tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }
        this.tags.add(tag);
        if (!tag.getQuestions().contains(this)) {
            tag.getQuestions().add(this);
        }
    }

    /**
     * Remove a tag from this question
     */
    public void removeTag(Tag tag) {
        if (this.tags != null) {
            this.tags.remove(tag);
            if (tag.getQuestions().contains(this)) {
                tag.getQuestions().remove(this);
            }
        }
    }

    /**
     * Increment view count
     */
    public void incrementViewCount() {
        this.viewCount++;
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
     * Check if user has liked this question
     */
    public boolean isLikedBy(User user) {
        return this.likedByUsers != null && this.likedByUsers.contains(user);
    }

    /**
     * Get number of answers
     */
    public Integer getAnswerCount() {
        return this.answers != null ? this.answers.size() : 0;
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
        if (viewCount == null) {
            viewCount = 0;
        }
        if (answerCount == null) {
            answerCount = 0;
        }
        if (likeCount == null) {
            likeCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        this.answerCount = this.answers != null ? this.answers.size() : 0;
    }

    // ============================================================================
    // equals & hashCode
    // ============================================================================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Question)) return false;
        Question question = (Question) o;
        return Objects.equals(getId(), question.getId());
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
        return "Question{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", viewCount=" + viewCount +
                ", answerCount=" + answerCount +
                ", likeCount=" + likeCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}