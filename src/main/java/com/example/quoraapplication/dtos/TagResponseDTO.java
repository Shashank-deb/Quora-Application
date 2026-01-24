package com.example.quoraapplication.dtos;

import lombok.Data;

@Data
public class TagResponseDTO {
    private Long id;
    private String name;
    private Integer followerCount;
}