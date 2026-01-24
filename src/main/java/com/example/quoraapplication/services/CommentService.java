package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.CommentDTO;
import com.example.quoraapplication.dtos.CommentResponseDTO;
import com.example.quoraapplication.dtos.UserBasicDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Comment;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.CommentRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, 
                         AnswerRepository answerRepository,
                         UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.answerRepository = answerRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByAnswerId(Long answerId, int page, int size){
        return commentRepository.findByAnswerId(answerId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getRepliesByCommentId(Long commentId, int page, int size){
        return commentRepository.findByParentCommentId(commentId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CommentResponseDTO> getCommentId(Long id){
        return commentRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Comment createComment(CommentDTO commentDTO){
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());

        Optional<Answer> answer = answerRepository.findById(commentDTO.getAnswerId());
        answer.ifPresent(comment::setAnswer);

        if(commentDTO.getParentCommentId() != null){
            Optional<Comment> parentComment = commentRepository.findById(commentDTO.getParentCommentId());
            parentComment.ifPresent(comment::setParentComment);
        }

        return commentRepository.save(comment);
    }

    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }

    /**
     * Allows a user to like a comment
     * @param commentId - ID of the comment to be liked
     * @param userId - ID of the user who is liking the comment
     * @throws RuntimeException if comment or user is not found
     */
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Initialize the likedBy set if it's null
        if (comment.getLikedBy() == null) {
            comment.setLikedBy(new HashSet<>());
        }

        // Add the user to comment's liked by set (Set will handle duplicates automatically)
        comment.getLikedBy().add(user);

        // Save the updated comment
        commentRepository.save(comment);
    }

    /**
     * Allows a user to unlike a comment
     * @param commentId - ID of the comment to be unliked
     * @param userId - ID of the user who is unliking the comment
     * @throws RuntimeException if comment or user is not found
     */
    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Remove the user from comment's liked by set if present
        if (comment.getLikedBy() != null) {
            comment.getLikedBy().remove(user);
            commentRepository.save(comment);
        }
    }

    /**
     * Convert Comment entity to CommentResponseDTO
     */
    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        
        // Map user to UserBasicDTO (if you have user in Comment entity)
        // For now, commenting out as Comment doesn't have user field in your original model
        // If you add it later, uncomment this:
        /*
        if (comment.getUser() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(comment.getUser().getId());
            userDTO.setUsername(comment.getUser().getUsername());
            dto.setUser(userDTO);
        }
        */
        
        // Set answer ID
        if (comment.getAnswer() != null) {
            dto.setAnswerId(comment.getAnswer().getId());
        }
        
        // Set parent comment ID
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }
        
        // Get like count without loading the entire collection
        if (comment.getLikedBy() != null) {
            dto.setLikeCount(comment.getLikedBy().size());
        } else {
            dto.setLikeCount(0);
        }
        
        // Get reply count without loading the entire collection
        if (comment.getReplies() != null) {
            dto.setReplyCount(comment.getReplies().size());
        } else {
            dto.setReplyCount(0);
        }
        
        return dto;
    }
}