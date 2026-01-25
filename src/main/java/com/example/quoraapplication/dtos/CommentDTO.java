package com.example.quoraapplication.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {


    private Long id;


    @NotBlank(message = "Comment content cannot be blank")
    private String content;


    @NotNull(message = "Answer ID is required")
    private Long answerId;


    @NotNull(message = "User ID is required")
    private Long userId;


    private Long parentCommentId;


    @Override
    public String toString() {
        return "CommentDTO{" +
                "id=" + id +
                ", content='" + content + '\'' +
                ", answerId=" + answerId +
                ", userId=" + userId +
                ", parentCommentId=" + parentCommentId +
                '}';
    }
}

