package com.example.quoraapplication.models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class User extends BaseModel{
    private String username;
    private String password;

    @ManyToMany
    @JoinTable(
            name="user_tags",
            joinColumns = @JoinColumn(name="user_id"),
            inverseJoinColumns=@JoinColumn(name="tag_id")
    )
    private Set<Tag> followedTags;
}
