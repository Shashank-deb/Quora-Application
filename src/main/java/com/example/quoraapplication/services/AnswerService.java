package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.AnswerDTO;
import com.example.quoraapplication.dtos.AnswerResponseDTO;
import com.example.quoraapplication.exception.ResourceNotFoundException;
import com.example.quoraapplication.events.EventPublisher;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    public AnswerService(AnswerRepository answerRepository,
                        QuestionRepository questionRepository,
                        UserRepository userRepository,
                        EventPublisher eventPublisher) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ============================================================================
    // Create Answer
    // ============================================================================

    /**
     * Create a new answer for a question
     */
    public Answer createAnswer(AnswerDTO answerDTO) {
        log.info("Creating answer for question ID: {}", answerDTO.getQuestionId());

        // Fetch the question
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Question not found with id: " + answerDTO.getQuestionId()));

        // Fetch the user (author)
        User author = userRepository.findById(answerDTO.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id: " + answerDTO.getAuthorId()));

        // Create the answer
        Answer answer = Answer.builder()
                .content(answerDTO.getContent())
                .question(question)
                .author(author)
                .isAccepted(false)
                .likeCount(0)
                .build();

        // Save the answer
        Answer savedAnswer = answerRepository.save(answer);
        
        // Update question's answer count
        question.addAnswer(savedAnswer);
        questionRepository.save(question);

        // Add to user's answers
        author.addAnswer(savedAnswer);
        userRepository.save(author);

        // Publish event - CORRECTED SIGNATURE with 3 parameters
        eventPublisher.publishAnswerCreated(
                savedAnswer.getId(), 
                savedAnswer.getQuestion().getId(),
                savedAnswer.getAuthor().getId());

        log.info("Answer created successfully with ID: {}", savedAnswer.getId());
        return savedAnswer;
    }

    // ============================================================================
    // Retrieve Answers
    // ============================================================================

    /**
     * Get all answers for a question with pagination
     */
    public List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId, int page, int size) {
        log.info("Fetching answers for question ID: {}", questionId);

        // Verify question exists
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found with id: " + questionId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Answer> answers = answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId, pageable);

        return answers.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get accepted answers for a question
     */
    public List<AnswerResponseDTO> getAcceptedAnswers(Long questionId, int page, int size) {
        log.info("Fetching accepted answers for question ID: {}", questionId);

        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question not found with id: " + questionId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Answer> answers = answerRepository.findByQuestionIdAndIsAcceptedTrue(questionId, pageable);

        return answers.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get answer by ID
     */
    public Optional<AnswerResponseDTO> getAnswerById(Long answerId) {
        log.info("Fetching answer with ID: {}", answerId);
        return answerRepository.findById(answerId)
                .map(this::convertToResponseDTO);
    }

    /**
     * Get answers by a specific author with pagination
     */
    public List<AnswerResponseDTO> getAnswersByAuthor(Long authorId, int page, int size) {
        log.info("Fetching answers by author ID: {}", authorId);

        if (!userRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("User not found with id: " + authorId);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Answer> answers = answerRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);

        return answers.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get answers by question ID with pagination - sorted by acceptance status
     */
    public Page<Answer> findByQuestionIdOrderByCreatedAtDesc(Long questionId, Pageable pageable) {
        log.info("Fetching answers for question ID: {} with pagination", questionId);
        return answerRepository.findByQuestionIdOrderByCreatedAtDesc(questionId, pageable);
    }

    /**
     * Find accepted answers for a question with pagination
     */
    public Page<Answer> findByQuestionIdAndIsAcceptedTrue(Long questionId, Pageable pageable) {
        log.info("Fetching accepted answers for question ID: {}", questionId);
        return answerRepository.findByQuestionIdAndIsAcceptedTrue(questionId, pageable);
    }

    /**
     * Find answers by author ID ordered by creation date (descending)
     */
    public Page<Answer> findByAuthorIdOrderByCreatedAtDesc(Long authorId, Pageable pageable) {
        log.info("Fetching answers by author ID: {}", authorId);
        return answerRepository.findByAuthorIdOrderByCreatedAtDesc(authorId, pageable);
    }

    // ============================================================================
    // Update Answer
    // ============================================================================

    /**
     * Update answer content
     */
    public Answer updateAnswer(Long answerId, AnswerDTO answerDTO) {
        log.info("Updating answer with ID: {}", answerId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        answer.setContent(answerDTO.getContent());
        Answer updatedAnswer = answerRepository.save(answer);

        log.info("Answer updated successfully with ID: {}", answerId);
        return updatedAnswer;
    }

    /**
     * Mark answer as accepted
     */
    public Answer markAsAccepted(Long answerId, Long userId) {
        log.info("Marking answer {} as accepted by user {}", answerId, userId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        answer.markAsAccepted(user);
        Answer updatedAnswer = answerRepository.save(answer);

        // Publish event
        eventPublisher.publishAnswerMarkedAsAccepted(updatedAnswer.getId(), userId);

        log.info("Answer marked as accepted with ID: {}", answerId);
        return updatedAnswer;
    }

    /**
     * Mark answer as not accepted
     */
    public Answer markAsNotAccepted(Long answerId) {
        log.info("Unmarking answer {} as accepted", answerId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        answer.unmarkAsAccepted();
        Answer updatedAnswer = answerRepository.save(answer);

        log.info("Answer unmarked as accepted with ID: {}", answerId);
        return updatedAnswer;
    }

    // ============================================================================
    // Like/Unlike Answer
    // ============================================================================

    /**
     * Like an answer
     */
    public void likeAnswer(Long answerId, Long userId) {
        log.info("User {} liking answer {}", userId, answerId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (!answer.isLikedBy(user)) {
            user.likeAnswer(answer);
            userRepository.save(user);
            answerRepository.save(answer);
        }

        log.info("Answer liked successfully");
    }

    /**
     * Unlike an answer
     */
    public void unlikeAnswer(Long answerId, Long userId) {
        log.info("User {} unliking answer {}", userId, answerId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (answer.isLikedBy(user)) {
            user.unlikeAnswer(answer);
            userRepository.save(user);
            answerRepository.save(answer);
        }

        log.info("Answer unliked successfully");
    }

    // ============================================================================
    // Delete Answer
    // ============================================================================

    /**
     * Delete an answer
     */
    public void deleteAnswer(Long answerId) {
        log.info("Deleting answer with ID: {}", answerId);

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer not found with id: " + answerId));

        // Update question's answer count
        Question question = answer.getQuestion();
        if (question != null) {
            question.removeAnswer(answer);
            questionRepository.save(question);
        }

        // Remove from user's answers
        User author = answer.getAuthor();
        if (author != null) {
            author.removeAnswer(answer);
            userRepository.save(author);
        }

        answerRepository.deleteById(answerId);
        log.info("Answer deleted successfully with ID: {}", answerId);
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Convert Answer entity to AnswerResponseDTO
     */
    private AnswerResponseDTO convertToResponseDTO(Answer answer) {
        return AnswerResponseDTO.builder()
                .id(answer.getId())
                .content(answer.getContent())
                .isAccepted(answer.getIsAccepted())
                .likeCount(answer.getLikeCount())
                .createdAt(answer.getCreatedAt())
                .updatedAt(answer.getUpdatedAt())
                .authorId(answer.getAuthor() != null ? answer.getAuthor().getId() : null)
                .authorName(answer.getAuthor() != null ? answer.getAuthor().getUsername() : null)
                .questionId(answer.getQuestion() != null ? answer.getQuestion().getId() : null)
                .commentCount(answer.getCommentCount())
                .build();
    }
}