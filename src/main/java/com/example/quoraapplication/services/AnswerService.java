package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.AnswerDTO;
import com.example.quoraapplication.dtos.AnswerResponseDTO;
import com.example.quoraapplication.dtos.UserBasicDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    /**
     * Get all answers for a question with pagination
     */
    @Transactional(readOnly = true)
    public List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId, int page, int size) {
        log.debug("Fetching answers for question: {}", questionId);
        return answerRepository.findByQuestionId(questionId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a single answer by ID
     */
    @Transactional(readOnly = true)
    public Optional<AnswerResponseDTO> getAnswerById(Long id) {
        log.debug("Fetching answer: {}", id);
        return answerRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    /**
     * Create a new answer
     */
    @Transactional
    public Answer createAnswer(AnswerDTO answerDTO) {
        log.info("Creating answer for question: {}", answerDTO.getQuestionId());
        
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> {
                    log.error("Question not found: {}", answerDTO.getQuestionId());
                    return new RuntimeException("Question with ID " + answerDTO.getQuestionId() + " not found");
                });

        User author = userRepository.findById(answerDTO.getUserId())
                .orElseThrow(() -> {
                    log.error("User not found: {}", answerDTO.getUserId());
                    return new RuntimeException("User with ID " + answerDTO.getUserId() + " not found");
                });

        Answer answer = Answer.builder()
                .content(answerDTO.getContent())
                .question(question)
                .author(author)
                .likeCount(0)
                .isAccepted(false)
                .build();

        Answer savedAnswer = answerRepository.save(answer);
        
        // Publish event
        eventPublisher.publishAnswerCreated(savedAnswer.getId(), question.getId(), author.getId());
        
        log.info("Answer created successfully: {}", savedAnswer.getId());
        return savedAnswer;
    }

    /**
     * Update an answer
     */
    @Transactional
    public Answer updateAnswer(Long id, AnswerDTO answerDTO) {
        log.info("Updating answer: {}", id);
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", id);
                    return new RuntimeException("Answer with ID " + id + " not found");
                });

        answer.setContent(answerDTO.getContent());
        Answer updatedAnswer = answerRepository.save(answer);
        
        log.info("Answer updated successfully: {}", id);
        return updatedAnswer;
    }

    /**
     * Delete an answer
     */
    @Transactional
    public void deleteAnswer(Long id) {
        log.info("Deleting answer: {}", id);
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", id);
                    return new RuntimeException("Answer with ID " + id + " not found");
                });

        answerRepository.delete(answer);
        log.info("Answer deleted successfully: {}", id);
    }

    /**
     * Like an answer
     */
    @Transactional
    public void likeAnswer(Long answerId, Long userId) {
        log.info("User {} liking answer {}", userId, answerId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", answerId);
                    return new RuntimeException("Answer with ID " + answerId + " not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("User with ID " + userId + " not found");
                });

        if (!answer.isLikedBy(user)) {
            answer.getLikedByUsers().add(user);
            answer.incrementLikeCount();
            answerRepository.save(answer);
            log.info("Answer liked successfully: {}", answerId);
        }
    }

    /**
     * Unlike an answer
     */
    @Transactional
    public void unlikeAnswer(Long answerId, Long userId) {
        log.info("User {} unliking answer {}", userId, answerId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", answerId);
                    return new RuntimeException("Answer with ID " + answerId + " not found");
                });

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("User with ID " + userId + " not found");
                });

        if (answer.isLikedBy(user)) {
            answer.getLikedByUsers().remove(user);
            answer.decrementLikeCount();
            answerRepository.save(answer);
            log.info("Answer unliked successfully: {}", answerId);
        }
    }

    /**
     * Mark an answer as accepted
     */
    @Transactional
    public void markAsAccepted(Long answerId) {
        log.info("Marking answer as accepted: {}", answerId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", answerId);
                    return new RuntimeException("Answer with ID " + answerId + " not found");
                });

        answer.markAsAccepted();
        answerRepository.save(answer);
        
        // Publish event
        if (answer.getQuestion() != null) {
            eventPublisher.publishAnswerMarkedAsAccepted(answerId, answer.getQuestion().getId());
        }
        
        log.info("Answer marked as accepted: {}", answerId);
    }

    /**
     * Unmark an answer as accepted
     */
    @Transactional
    public void unmarkAsAccepted(Long answerId) {
        log.info("Unmarking answer as accepted: {}", answerId);
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> {
                    log.error("Answer not found: {}", answerId);
                    return new RuntimeException("Answer with ID " + answerId + " not found");
                });

        answer.markAsNotAccepted();
        answerRepository.save(answer);
        log.info("Answer unmarked as accepted: {}", answerId);
    }

    /**
     * Get number of answers for a question
     */
    @Transactional(readOnly = true)
    public long getAnswerCountByQuestion(Long questionId) {
        return answerRepository.countByQuestionId(questionId);
    }

    /**
     * Get all answers by a user
     */
    @Transactional(readOnly = true)
    public List<Answer> getAnswersByUser(Long userId) {
        return answerRepository.findByAuthorId(userId);
    }

    /**
     * Convert Answer entity to AnswerResponseDTO
     */
    private AnswerResponseDTO convertToResponseDTO(Answer answer) {
        AnswerResponseDTO dto = new AnswerResponseDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setIsAccepted(answer.getIsAccepted());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setUpdatedAt(answer.getUpdatedAt());
        dto.setLikeCount(answer.getLikeCount());
        dto.setCommentCount(answer.getCommentCount());
        
        if (answer.getAuthor() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(answer.getAuthor().getId());
            userDTO.setUsername(answer.getAuthor().getUsername());
            dto.setAuthor(userDTO);
        }
        
        if (answer.getQuestion() != null) {
            dto.setQuestionId(answer.getQuestion().getId());
            dto.setQuestionTitle(answer.getQuestion().getTitle());
        }
        
        return dto;
    }
}