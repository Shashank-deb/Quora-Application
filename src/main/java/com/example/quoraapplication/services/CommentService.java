package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.CommentDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Comment;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.CommentRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    public List<Comment> getCommentsByAnswerId(Long answerId, int page, int size){
        return commentRepository.findByAnswerId(answerId, PageRequest.of(page, size)).getContent();
    }

    public List<Comment> getRepliesByCommentId(Long commentId, int page, int size){
        return commentRepository.findByParentCommentId(commentId, PageRequest.of(page, size)).getContent();
    }

    public Optional<Comment> getCommentId(Long id){
        return commentRepository.findById(id);
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
    public void likeComment(Long commentId, Long userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (commentOptional.isEmpty()) {
            throw new RuntimeException("Comment with ID " + commentId + " not found");
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        Comment comment = commentOptional.get();
        User user = userOptional.get();

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
    public void unlikeComment(Long commentId, Long userId) {
        Optional<Comment> commentOptional = commentRepository.findById(commentId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (commentOptional.isEmpty()) {
            throw new RuntimeException("Comment with ID " + commentId + " not found");
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        Comment comment = commentOptional.get();
        User user = userOptional.get();

        // Remove the user from comment's liked by set if present
        if (comment.getLikedBy() != null) {
            comment.getLikedBy().remove(user);
            commentRepository.save(comment);
        }
    }
}