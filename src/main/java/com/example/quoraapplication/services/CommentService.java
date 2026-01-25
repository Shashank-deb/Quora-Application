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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    // ============================================================================
    // Retrieve Methods
    // ============================================================================

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByAnswerId(Long answerId, int page, int size) {
        log.debug("Fetching comments for answer ID: {}", answerId);
        return commentRepository.findByAnswerId(answerId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getRepliesByCommentId(Long commentId, int page, int size) {
        log.debug("Fetching replies for comment ID: {}", commentId);
        return commentRepository.findByParentCommentId(commentId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CommentResponseDTO> getCommentById(Long id) {
        log.debug("Fetching comment with ID: {}", id);
        return commentRepository.findByIdWithAllAssociations(id)
                .map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByUserId(Long userId, int page, int size) {
        log.debug("Fetching comments by user ID: {}", userId);
        return commentRepository.findByUserId(userId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ============================================================================
    // Create Methods
    // ============================================================================

    @Transactional
    public Comment createComment(CommentDTO commentDTO) {
        log.info("Creating new comment for answer ID: {}", commentDTO.getAnswerId());

        if (commentDTO.getAnswerId() == null) {
            throw new RuntimeException("Answer ID is required for comment creation");
        }

        if (commentDTO.getUserId() == null) {
            throw new RuntimeException("User ID is required for comment creation");
        }

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());

        // Set the answer
        Answer answer = answerRepository.findById(commentDTO.getAnswerId())
                .orElseThrow(() -> new RuntimeException("Answer with ID " + commentDTO.getAnswerId() + " not found"));
        comment.setAnswer(answer);

        // Set the author
        User author = userRepository.findById(commentDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User with ID " + commentDTO.getUserId() + " not found"));
        comment.setAuthor(author);

        // Set parent comment for nested replies (optional)
        if (commentDTO.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(commentDTO.getParentCommentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment with ID " + commentDTO.getParentCommentId() + " not found"));
            comment.setParentComment(parentComment);
        }

        comment.setLikedBy(new HashSet<>());
        comment.setReplies(new HashSet<>());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment created successfully with ID: {}", savedComment.getId());

        return savedComment;
    }

    // ============================================================================
    // Update Methods
    // ============================================================================

    @Transactional
    public Comment updateComment(Long id, String content) {
        log.info("Updating comment with ID: {}", id);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + id + " not found"));

        comment.setContent(content);
        Comment updatedComment = commentRepository.save(comment);

        log.info("Comment updated successfully with ID: {}", id);
        return updatedComment;
    }

    // ============================================================================
    // Delete Methods
    // ============================================================================

    @Transactional
    public void deleteComment(Long id) {
        log.info("Deleting comment with ID: {}", id);

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + id + " not found"));

        // Delete all replies to this comment (cascade handled by JPA)
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            log.debug("Deleting {} replies for comment ID: {}", comment.getReplies().size(), id);
            comment.getReplies().forEach(reply -> commentRepository.delete(reply));
        }

        commentRepository.delete(comment);
        log.info("Comment deleted successfully with ID: {}", id);
    }

    // ============================================================================
    // Like Management Methods
    // ============================================================================

    @Transactional
    public void likeComment(Long commentId, Long userId) {
        log.info("User {} liking comment {}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Check if user already liked this comment
        if (commentRepository.userHasLikedComment(commentId, userId)) {
            log.warn("User {} has already liked comment {}", userId, commentId);
            throw new RuntimeException("User has already liked this comment");
        }

        if (comment.getLikedBy() == null) {
            comment.setLikedBy(new HashSet<>());
        }

        comment.getLikedBy().add(user);
        comment.incrementLikeCount();

        commentRepository.save(comment);
        log.info("Comment liked successfully");
    }

    @Transactional
    public void unlikeComment(Long commentId, Long userId) {
        log.info("User {} unliking comment {}", userId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment with ID " + commentId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        if (comment.getLikedBy() != null && comment.getLikedBy().contains(user)) {
            comment.getLikedBy().remove(user);
            comment.decrementLikeCount();
            commentRepository.save(comment);
            log.info("Comment unliked successfully");
        } else {
            log.warn("User {} has not liked comment {}", userId, commentId);
            throw new RuntimeException("User has not liked this comment");
        }
    }

    // ============================================================================
    // Search Methods
    // ============================================================================

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> searchComments(String searchTerm, int page, int size) {
        log.debug("Searching comments with term: {}", searchTerm);
        return commentRepository.searchByContent(searchTerm, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ============================================================================
    // Statistics Methods
    // ============================================================================

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getMostLikedComments(int limit) {
        log.debug("Fetching {} most liked comments", limit);
        return commentRepository.getMostLikedComments(PageRequest.of(0, limit))
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getCommentCountForAnswer(Long answerId) {
        log.debug("Getting comment count for answer ID: {}", answerId);
        return commentRepository.countTotalCommentsOnAnswer(answerId);
    }

    @Transactional(readOnly = true)
    public long getReplyCountForComment(Long commentId) {
        log.debug("Getting reply count for comment ID: {}", commentId);
        return commentRepository.countReplies(commentId);
    }

    // ============================================================================
    // DTO Conversion
    // ============================================================================

    private CommentResponseDTO convertToResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();

        dto.setId(comment.getId());
        dto.setContent(comment.getContent());

        // Map author to UserBasicDTO
        if (comment.getAuthor() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(comment.getAuthor().getId());
            userDTO.setUsername(comment.getAuthor().getUsername());
            dto.setAuthor(userDTO);
        }

        // Map answer ID
        if (comment.getAnswer() != null) {
            dto.setAnswerId(comment.getAnswer().getId());
        }

        // Map parent comment ID
        if (comment.getParentComment() != null) {
            dto.setParentCommentId(comment.getParentComment().getId());
        }

        // Set like count
        dto.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);

        // Set reply count
        dto.setReplyCount(comment.getReplyCount());

        return dto;
    }
}