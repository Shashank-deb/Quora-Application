package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_answer_id", columnList = "answer_id"),
        @Index(name = "idx_parent_comment_id", columnList = "parent_comment_id"),
        @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
public class Comment extends BaseModel {

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "answer_id", nullable = false)
    @JsonIgnoreProperties({"comments", "likedBy", "question", "user"})
    private Answer answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"followedTags", "password", "email"})
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({"parentComment", "replies", "answer", "likedBy"})
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties({"parentComment", "answer"})
    private Set<Comment> replies;

    @ManyToMany
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"followedTags", "password", "email"})
    private Set<User> likedBy;

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
}