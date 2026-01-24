package com.example.quoraapplication.dtos;

import lombok.Data;

@Data
public class AnswerResponseDTO {
    private Long id;
    private String content;
    private UserBasicDTO user;
    private Long questionId;
    private String questionTitle;
    private Integer likeCount;
}