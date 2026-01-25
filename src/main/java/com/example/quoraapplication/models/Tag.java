package com.example.quoraapplication.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "tags", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
public class Tag extends BaseModel {
    
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer followerCount = 0;

    @ManyToMany(mappedBy = "followedTags")
    @JsonIgnoreProperties({"followedTags", "password", "email"})
    private Set<User> followers;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tag)) return false;
        Tag tag = (Tag) o;
        return Objects.equals(getId(), tag.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}