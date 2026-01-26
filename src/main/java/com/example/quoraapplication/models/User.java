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
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email", unique = true),
        @Index(name = "idx_username", columnList = "username", unique = true),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(columnDefinition = "LONGTEXT")
    private String bio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;


    // ============================================================================
    // Enums
    // ============================================================================

    public enum Role {
        ROLE_USER,
        ROLE_ADMIN,
        ROLE_MODERATOR
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.ROLE_USER;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * Questions created by this user
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"user", "answers", "comments", "tags", "likedByUsers"})
    private Set<Question> questions = new HashSet<>();

    /**
     * Answers created by this user
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"author", "question", "comments", "likedBy", "acceptedBy"})
    private Set<Answer> answers = new HashSet<>();

    /**
     * Comments created by this user
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"author", "question", "answer", "parentComment", "replies", "likedBy"})
    private Set<Comment> comments = new HashSet<>();

    /**
     * Tags followed by this user
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"followers", "questions"})
    private Set<Tag> followedTags = new HashSet<>();

    /**
     * Questions liked by this user
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_liked_questions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    @JsonIgnoreProperties({"likedByUsers", "user", "answers", "comments", "tags"})
    private Set<Question> likedQuestions = new HashSet<>();

    /**
     * Answers liked by this user
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_liked_answers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "answer_id")
    )
    @JsonIgnoreProperties({"likedBy", "author", "question", "comments"})
    private Set<Answer> likedAnswers = new HashSet<>();

    /**
     * Comments liked by this user
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE, mappedBy = "likedBy")
    @JsonIgnoreProperties({"likedBy", "author", "question", "answer", "parentComment", "replies"})
    private Set<Comment> likedComments = new HashSet<>();

    // ============================================================================
    // Setter Methods with Bidirectional Relationship Management
    // ============================================================================

    /**
     * Set first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Set last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Set bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Add a question created by this user
     */
    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new HashSet<>();
        }
        this.questions.add(question);
        question.setUser(this);
    }

    /**
     * Remove a question created by this user
     */
    public void removeQuestion(Question question) {
        if (this.questions != null) {
            this.questions.remove(question);
            question.setUser(null);
        }
    }

    /**
     * Add an answer created by this user
     */
    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new HashSet<>();
        }
        this.answers.add(answer);
        answer.setAuthor(this);
    }

    /**
     * Remove an answer created by this user
     */
    public void removeAnswer(Answer answer) {
        if (this.answers != null) {
            this.answers.remove(answer);
            answer.setAuthor(null);
        }
    }

    /**
     * Add a comment created by this user
     */
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new HashSet<>();
        }
        this.comments.add(comment);
        comment.setAuthor(this);
    }

    /**
     * Remove a comment created by this user
     */
    public void removeComment(Comment comment) {
        if (this.comments != null) {
            this.comments.remove(comment);
            comment.setAuthor(null);
        }
    }

    /**
     * Follow a tag
     */
    public void followTag(Tag tag) {
        if (this.followedTags == null) {
            this.followedTags = new HashSet<>();
        }
        this.followedTags.add(tag);
        if (!tag.getFollowers().contains(this)) {
            tag.getFollowers().add(this);
        }
    }

    /**
     * Unfollow a tag
     */
    public void unfollowTag(Tag tag) {
        if (this.followedTags != null) {
            this.followedTags.remove(tag);
            if (tag.getFollowers().contains(this)) {
                tag.getFollowers().remove(this);
            }
        }
    }

    /**
     * Like a question
     */
    public void likeQuestion(Question question) {
        if (this.likedQuestions == null) {
            this.likedQuestions = new HashSet<>();
        }
        this.likedQuestions.add(question);
        question.incrementLikeCount();
        if (!question.getLikedByUsers().contains(this)) {
            question.getLikedByUsers().add(this);
        }
    }

    /**
     * Unlike a question
     */
    public void unlikeQuestion(Question question) {
        if (this.likedQuestions != null) {
            this.likedQuestions.remove(question);
            question.decrementLikeCount();
            if (question.getLikedByUsers().contains(this)) {
                question.getLikedByUsers().remove(this);
            }
        }
    }

    /**
     * Like an answer
     */
    public void likeAnswer(Answer answer) {
        if (this.likedAnswers == null) {
            this.likedAnswers = new HashSet<>();
        }
        this.likedAnswers.add(answer);
        answer.incrementLikeCount();
        if (!answer.getLikedBy().contains(this)) {
            answer.getLikedBy().add(this);
        }
    }

    /**
     * Unlike an answer
     */
    public void unlikeAnswer(Answer answer) {
        if (this.likedAnswers != null) {
            this.likedAnswers.remove(answer);
            answer.decrementLikeCount();
            if (answer.getLikedBy().contains(this)) {
                answer.getLikedBy().remove(this);
            }
        }
    }

    /**
     * Like a comment
     */
    public void likeComment(Comment comment) {
        if (this.likedComments == null) {
            this.likedComments = new HashSet<>();
        }
        this.likedComments.add(comment);
        comment.incrementLikeCount();
        if (!comment.getLikedBy().contains(this)) {
            comment.getLikedBy().add(this);
        }
    }

    /**
     * Unlike a comment
     */
    public void unlikeComment(Comment comment) {
        if (this.likedComments != null) {
            this.likedComments.remove(comment);
            comment.decrementLikeCount();
            if (comment.getLikedBy().contains(this)) {
                comment.getLikedBy().remove(this);
            }
        }
    }

    /**
     * Check if user has liked a question
     */
    public boolean hasLikedQuestion(Question question) {
        return this.likedQuestions != null && this.likedQuestions.contains(question);
    }

    /**
     * Check if user has liked an answer
     */
    public boolean hasLikedAnswer(Answer answer) {
        return this.likedAnswers != null && this.likedAnswers.contains(answer);
    }

    /**
     * Check if user has liked a comment
     */
    public boolean hasLikedComment(Comment comment) {
        return this.likedComments != null && this.likedComments.contains(comment);
    }

    /**
     * Check if user is following a tag
     */
    public boolean isFollowingTag(Tag tag) {
        return this.followedTags != null && this.followedTags.contains(tag);
    }

    // ============================================================================
    // JPA Lifecycle Callbacks
    // ============================================================================

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (role == null) {
            role = Role.ROLE_USER;
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
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getId(), user.getId());
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
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}