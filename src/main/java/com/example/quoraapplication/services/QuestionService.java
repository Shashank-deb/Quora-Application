package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.*;
import com.example.quoraapplication.exception.InvalidOperationException;
import com.example.quoraapplication.exception.ResourceNotFoundException;
import com.example.quoraapplication.exception.ValidationException;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.TagRepository;
import com.example.quoraapplication.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    public EventPublisher eventPublisher;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    // ============================================================================
    // CREATE - Requires write transaction
    // ============================================================================

    /**
     * Create a new question with proper error handling
     */
    @Transactional(rollbackFor = Exception.class)
    public Question createQuestion(QuestionDTO questionDTO) {
        log.info("Creating new question: {}", questionDTO.getTitle());

        try {
            // Fetch user
            User user = userRepository.findById(questionDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "User not found with id: " + questionDTO.getUserId()));

            // Check user hasn't asked too many questions today
            long questionsToday = questionRepository.findAll()
                    .stream()
                    .filter(q -> q.getUser().getId().equals(user.getId()))
                    .filter(q -> q.getCreatedAt().isAfter(LocalDateTime.now().minusDays(1)))
                    .count();

            if (questionsToday >= 10) {
                throw new InvalidOperationException(
                        "You can ask maximum 10 questions per day");
            }

            // Create question
            Question question = new Question();
            question.setTitle(questionDTO.getTitle());
            question.setContent(questionDTO.getContent());
            question.setUser(user);

            // Fetch and associate tags
            Set<Tag> tags = questionDTO.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Tag not found with id: " + tagId)))
                    .collect(Collectors.toSet());
            question.setTags(tags);

            // Save with error handling
            Question savedQuestion = questionRepository.save(question);
            log.info("Question created successfully with ID: {}", savedQuestion.getId());

            // Publish event
            try {
                eventPublisher.publishAnswerCreated(
                        savedQuestion.getId(),
                        0L,
                        user.getId());
            } catch (Exception e) {
                log.warn("Failed to publish question event, continuing anyway", e);
                // Don't fail the operation if Kafka is down
            }

            return savedQuestion;

        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while creating question: {}", e.getMessage());
            throw new ValidationException("Invalid data provided: duplicate or constraint violation");
        } catch (InvalidDataAccessApiUsageException e) {
            log.error("Invalid data access while creating question: {}", e.getMessage());
            throw new ValidationException("Invalid data provided");
        } catch (Exception e) {
            log.error("Unexpected error while creating question: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create question: " + e.getMessage());
        }
    }

    // ============================================================================
    // READ - Can use readOnly for better performance
    // ============================================================================

    /**
     * Get all questions with pagination
     * ✅ Uses readOnly = true (optimized for reading)
     */
    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getQuestions(int offset, int limit) {
        log.info("Fetching questions - offset: {}, limit: {}", offset, limit);
        return questionRepository.findAllWithAssociations(PageRequest.of(offset, limit))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get question by ID
     * ✅ Uses readOnly = true (optimized for reading)
     */
    @Transactional(readOnly = true)
    public Optional<QuestionResponseDTO> getQuestionById(Long id) {
        log.info("Fetching question with ID: {}", id);
        return questionRepository.findByIdWithAssociations(id)
                .map(this::convertToResponseDTO);
    }

    /**
     * Get recent questions
     * ✅ Uses readOnly = true (optimized for reading)
     */
    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getRecentQuestions(int page, int size) {
        log.info("Fetching recent questions");
        return questionRepository.findRecentQuestions(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get most viewed questions
     * ✅ Uses readOnly = true (optimized for reading)
     */
    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getMostViewedQuestions(int page, int size) {
        log.info("Fetching most viewed questions");
        return questionRepository.findMostViewedQuestions(PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // ============================================================================
    // UPDATE - Requires write transaction
    // ============================================================================

    /**
     * Update a question
     * ✅ Uses class-level @Transactional (allows writes)
     */
    public Question updateQuestion(Long id, QuestionDTO questionDTO) {
        log.info("Updating question with ID: {}", id);

        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + id));

        question.setTitle(questionDTO.getTitle());
        question.setContent(questionDTO.getContent());

        Question updatedQuestion = questionRepository.save(question);
        log.info("Question updated successfully with ID: {}", id);
        return updatedQuestion;
    }

    // ============================================================================
    // DELETE - Requires write transaction
    // ============================================================================

    /**
     * Delete a question
     * ✅ Uses class-level @Transactional (allows writes)
     */
    public void deleteQuestion(Long id) {
        log.info("Deleting question with ID: {}", id);
        questionRepository.deleteById(id);
        log.info("Question deleted successfully with ID: {}", id);
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    /**
     * Convert Question entity to QuestionResponseDTO
     */
    private QuestionResponseDTO convertToResponseDTO(Question question) {
        QuestionResponseDTO dto = new QuestionResponseDTO();
        dto.setId(question.getId());
        dto.setTitle(question.getTitle());
        dto.setContent(question.getContent());

        // Map user to UserBasicDTO
        if (question.getUser() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(question.getUser().getId());
            userDTO.setUsername(question.getUser().getUsername());
            dto.setUser(userDTO);
        }

        // Map tags to TagDTOs
        if (question.getTags() != null) {
            Set<TagDTO> tagDTOs = question.getTags().stream()
                    .map(tag -> {
                        TagDTO tagDTO = new TagDTO();
                        tagDTO.setId(tag.getId());
                        tagDTO.setName(tag.getName());
                        return tagDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setTags(tagDTOs);
        }

        return dto;
    }
}