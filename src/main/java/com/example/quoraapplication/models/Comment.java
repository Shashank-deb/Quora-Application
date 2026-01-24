package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Getter
@Setter
public class Comment extends BaseModel {

    private String content;

    @ManyToOne
    @JoinColumn(name = "answer_id")
    @JsonIgnoreProperties({"comments", "likedBy", "question", "user"})
    private Answer answer;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnoreProperties({"parentComment", "replies", "answer", "likedBy"})
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment")
    @JsonIgnoreProperties({"parentComment", "answer"})
    private Set<Comment> replies;

    @ManyToMany
    @JoinTable(
            name = "comment_likes",
            joinColumns = @JoinColumn(name = "comment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"followedTags", "password"})
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