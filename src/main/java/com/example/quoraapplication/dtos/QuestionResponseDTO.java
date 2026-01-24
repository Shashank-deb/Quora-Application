package com.example.quoraapplication.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class QuestionResponseDTO {
    private Long id;
    private String title;
    private String content;
    private UserBasicDTO user;
    private Set<TagDTO> tags;
}