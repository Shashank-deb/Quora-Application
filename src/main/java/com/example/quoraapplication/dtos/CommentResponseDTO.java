package com.example.quoraapplication.dtos;

import lombok.Data;

@Data
public class CommentResponseDTO {
    private Long id;
    private String content;
    private UserBasicDTO user;
    private Long answerId;
    private Long parentCommentId;
    private Integer likeCount;
    private Integer replyCount;
}