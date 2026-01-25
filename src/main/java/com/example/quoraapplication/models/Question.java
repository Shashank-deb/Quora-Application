package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "questions", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id")
})
@Getter
@Setter
public class Question extends BaseModel {
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @ManyToMany
    @JoinTable(
            name = "question_tags",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnoreProperties({"followers"})
    private Set<Tag> tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"followedTags", "password", "email", "questions", "answers"})
    private User user;
    
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"question"})
    private Set<Answer> answers;
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer viewCount = 0;
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer answerCount = 0;

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
}