// File: src/main/java/com/example/quoraapplication/model/Tag.java

package com.example.quoraapplication.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tag name is required")
    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "follower_count", nullable = false)
    private Integer followerCount = 0;

    @Column(name = "question_count", nullable = false)
    private Integer questionCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * Questions tagged with this tag
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "tags")
    private Set<Question> questions = new HashSet<>();

    /**
     * Users following this tag
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "followedTags")
    private Set<User> followers = new HashSet<>();

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Add a question with this tag
     */
    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new HashSet<>();
        }
        this.questions.add(question);
        this.questionCount = this.questions.size();
    }

    /**
     * Remove a question from this tag
     */
    public void removeQuestion(Question question) {
        if (this.questions != null) {
            this.questions.remove(question);
            this.questionCount = this.questions.size();
        }
    }

    /**
     * Add a follower to this tag
     */
    public void addFollower(User user) {
        if (this.followers == null) {
            this.followers = new HashSet<>();
        }
        this.followers.add(user);
        this.followerCount = this.followers.size();
    }

    /**
     * Remove a follower from this tag
     */
    public void removeFollower(User user) {
        if (this.followers != null) {
            this.followers.remove(user);
            this.followerCount = this.followers.size();
        }
    }

    /**
     * Check if a user follows this tag
     */
    public boolean hasFollower(User user) {
        return this.followers != null && this.followers.contains(user);
    }

    /**
     * Get question count
     */
    public Integer getQuestionCount() {
        return this.questions != null ? this.questions.size() : 0;
    }

    /**
     * Get follower count
     */
    public Integer getFollowerCount() {
        return this.followers != null ? this.followers.size() : 0;
    }

    // ============================================================================
    // JPA Lifecycle Callbacks
    // ============================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (followerCount == null) {
            followerCount = 0;
        }
        if (questionCount == null) {
            questionCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        this.questionCount = this.questions != null ? this.questions.size() : 0;
        this.followerCount = this.followers != null ? this.followers.size() : 0;
    }

    // ============================================================================
    // toString (exclude circular references)
    // ============================================================================

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", followerCount=" + followerCount +
                ", questionCount=" + questionCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}