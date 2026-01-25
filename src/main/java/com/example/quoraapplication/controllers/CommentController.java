package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.CommentDTO;
import com.example.quoraapplication.dtos.CommentResponseDTO;
import com.example.quoraapplication.models.Comment;
import com.example.quoraapplication.services.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAnswerId(
            @RequestParam Long answerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<CommentResponseDTO> comments = commentService.getCommentsByAnswerId(answerId, page, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/replies")
    public ResponseEntity<List<CommentResponseDTO>> getRepliesByCommentId(
            @RequestParam Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<CommentResponseDTO> replies = commentService.getRepliesByCommentId(commentId, page, size);
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponseDTO> getCommentById(@PathVariable Long id) {
        Optional<CommentResponseDTO> comment = commentService.getCommentById(id);
        return comment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(@Valid @RequestBody CommentDTO commentDTO) {
        Comment createdComment = commentService.createComment(commentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{commentId}/like/{userId}")
    public ResponseEntity<Void> likeComment(
            @PathVariable Long commentId,
            @PathVariable Long userId) {
        commentService.likeComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{commentId}/unlike/{userId}")
    public ResponseEntity<Void> unlikeComment(
            @PathVariable Long commentId,
            @PathVariable Long userId) {
        commentService.unlikeComment(commentId, userId);
        return ResponseEntity.noContent().build();
    }
}