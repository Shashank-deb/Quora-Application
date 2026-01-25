package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "answers", indexes = {
    @Index(name = "idx_question_id", columnList = "question_id"),
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
public class Answer extends BaseModel {
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonIgnoreProperties({"answers", "user", "tags"})
    private Question question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"followedTags", "password", "email", "questions", "answers"})
    private User user;

    @OneToMany(mappedBy = "answer", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"answer"})
    private Set<Comment> comments;

    @ManyToMany
    @JoinTable(
            name = "answer_likes",
            joinColumns = @JoinColumn(name = "answer_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties({"followedTags", "password", "email"})
    private Set<User> likedBy;
    
    @Column(columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean isAccepted = false;

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