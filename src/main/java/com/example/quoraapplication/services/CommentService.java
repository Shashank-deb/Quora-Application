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
    public List<CommentResponseDTO> getCommentsByAnswerId(Long answerId, int page, int size) {
        return commentRepository.findByAnswerId(answerId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getRepliesByCommentId(Long commentId, int page, int size) {
        return commentRepository.findByParentCommentId(commentId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public Optional<CommentResponseDTO> getCommentId(Long id) {
        return commentRepository.findById(id)
                .map(this::convertToResponseDTO);
    }


    public Comment createComment(CommentDTO commentDTO) {
        Comment comment = new Comment();

        comment.setContent(commentDTO.getContent());

        Optional<Answer> answer = answerRepository.findById(commentDTO.getAnswerId());
        answer.ifPresent(comment::setAnswer);


        if (commentDTO.getUserId() != null) {
            Optional<User> user = userRepository.findById(commentDTO.getUserId());
            if (user.isPresent()) {
                comment.setUser(user.get());
            } else {
                throw new RuntimeException("User with ID " + commentDTO.getUserId() + " not found");
            }
        } else {
            throw new RuntimeException("User ID is required for comment creation");
        }

        if (commentDTO.getParentCommentId() != null) {
            Optional<Comment> parentComment = commentRepository.findById(commentDTO.getParentCommentId());
            parentComment.ifPresent(comment::setParentComment);
        }

        comment.setReplies(new HashSet<>());
        comment.setLikedBy(new HashSet<>());

        return commentRepository.save(comment);
    }


    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }


    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        if (comment.getLikedBy() == null) {
            comment.setLikedBy(new HashSet<>());
        }

        comment.getLikedBy().add(user);

        commentRepository.save(comment);
    }


    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        if (comment.getLikedBy() != null) {
            comment.getLikedBy().remove(user);
            commentRepository.save(comment);
        }
    }


    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());


        if (comment.getUser() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(comment.getUser().getId());
            userDTO.setUsername(comment.getUser().getUsername());
            dto.setUser(userDTO);
        }


        if (comment.getAnswer() != null) {
            dto.setAnswerId(comment.getAnswer().getId());
        }

        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }

        if (comment.getLikedBy() != null) {
            dto.setLikeCount(comment.getLikedBy().size());
        } else {
            dto.setLikeCount(0);
        }

        if (comment.getReplies() != null) {
            dto.setReplyCount(comment.getReplies().size());
        } else {
            dto.setReplyCount(0);
        }

        return dto;
    }
}

