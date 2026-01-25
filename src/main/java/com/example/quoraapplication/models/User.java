package com.example.quoraapplication.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "bio", length = 500)
    private String bio;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ============================================================================
    // Relationships
    // ============================================================================

    /**
     * Questions created by this user
     * Lazy loading by default - use findByIdWithQuestions() to eager load
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Question> questions = new HashSet<>();

    /**
     * Answers provided by this user
     * Lazy loading by default - use findByIdWithAnswers() to eager load
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Answer> answers = new HashSet<>();

    /**
     * Comments made by this user
     * Lazy loading by default - use findByIdWithComments() to eager load
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Comment> comments = new HashSet<>();

    /**
     * Tags followed by this user
     * Lazy loading by default - use findByIdWithTags() to eager load
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_followed_tags",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> followedTags = new HashSet<>();

    /**
     * Questions this user has voted on
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_liked_questions",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "question_id")
    )
    private Set<Question> likedQuestions = new HashSet<>();

    /**
     * Answers this user has voted on
     * Lazy loading by default
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_liked_answers",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "answer_id")
    )
    private Set<Answer> likedAnswers = new HashSet<>();

    // ============================================================================
    // UserDetails Implementation
    // ============================================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Add a question created by this user
     */
    public void addQuestion(Question question) {
        if (this.questions == null) {
            this.questions = new HashSet<>();
        }
        this.questions.add(question);
        question.setAuthor(this);
    }

    /**
     * Remove a question created by this user
     */
    public void removeQuestion(Question question) {
        if (this.questions != null) {
            this.questions.remove(question);
            question.setAuthor(null);
        }
    }

    /**
     * Add an answer provided by this user
     */
    public void addAnswer(Answer answer) {
        if (this.answers == null) {
            this.answers = new HashSet<>();
        }
        this.answers.add(answer);
        answer.setAuthor(this);
    }

    /**
     * Remove an answer provided by this user
     */
    public void removeAnswer(Answer answer) {
        if (this.answers != null) {
            this.answers.remove(answer);
            answer.setAuthor(null);
        }
    }

    /**
     * Add a comment made by this user
     */
    public void addComment(Comment comment) {
        if (this.comments == null) {
            this.comments = new HashSet<>();
        }
        this.comments.add(comment);
        comment.setAuthor(this);
    }

    /**
     * Remove a comment made by this user
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
    }

    /**
     * Unfollow a tag
     */
    public void unfollowTag(Tag tag) {
        if (this.followedTags != null) {
            this.followedTags.remove(tag);
        }
    }

    /**
     * Check if user follows a tag
     */
    public boolean followsTag(Tag tag) {
        return this.followedTags != null && this.followedTags.contains(tag);
    }

    /**
     * Like a question
     */
    public void likeQuestion(Question question) {
        if (this.likedQuestions == null) {
            this.likedQuestions = new HashSet<>();
        }
        this.likedQuestions.add(question);
    }

    /**
     * Unlike a question
     */
    public void unlikeQuestion(Question question) {
        if (this.likedQuestions != null) {
            this.likedQuestions.remove(question);
        }
    }

    /**
     * Check if user has liked a question
     */
    public boolean hasLikedQuestion(Question question) {
        return this.likedQuestions != null && this.likedQuestions.contains(question);
    }

    /**
     * Like an answer
     */
    public void likeAnswer(Answer answer) {
        if (this.likedAnswers == null) {
            this.likedAnswers = new HashSet<>();
        }
        this.likedAnswers.add(answer);
    }

    /**
     * Unlike an answer
     */
    public void unlikeAnswer(Answer answer) {
        if (this.likedAnswers != null) {
            this.likedAnswers.remove(answer);
        }
    }

    /**
     * Check if user has liked an answer
     */
    public boolean hasLikedAnswer(Answer answer) {
        return this.likedAnswers != null && this.likedAnswers.contains(answer);
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
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ============================================================================
    // Role Enum
    // ============================================================================

    public enum Role {
        ROLE_USER("User"),
        ROLE_MODERATOR("Moderator"),
        ROLE_ADMIN("Administrator");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // ============================================================================
    // toString (exclude circular references)
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
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}