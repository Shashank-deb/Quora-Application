package com.example.quoraapplication.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentResponseDTO {
    
    private Long id;
    
    private String content;
    
    private UserBasicDTO author;
    
    private Long answerId;
    
    private Long parentCommentId;
    
    private Integer likeCount;
    
    private Integer replyCount;
}