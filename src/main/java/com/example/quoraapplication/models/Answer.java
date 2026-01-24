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
public class Answer extends BaseModel {

    private String content;

    @ManyToOne
    @JoinColumn(name = "question_id")
    @JsonIgnoreProperties({"answers", "user", "tags"})
    private Question question;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"followedTags", "password"})
    private User user;

    @OneToMany(mappedBy = "answer")
    @JsonIgnoreProperties({"answer"})
    private Set<Comment> comments;

    @ManyToMany
    @JoinTable(
            name = "answer_likes",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"followedTags", "password"})
    private Set<User> likedBy;

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
}