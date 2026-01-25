package com.example.quoraapplication.services;


import com.example.quoraapplication.dtos.AnswerDTO;
import com.example.quoraapplication.dtos.AnswerResponseDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public AnswerService(AnswerRepository answerRepository,
                         QuestionRepository questionRepository,
                         UserRepository userRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get all answers for a question with pagination
     * @param questionId the question ID
     * @param page page number
     * @param size page size
     * @return list of answer response DTOs
     */
    @Transactional(readOnly = true)
    public List<AnswerResponseDTO> getAnswersByQuestionId(Long questionId, int page, int size) {
        return answerRepository.findCommentsByAnswer(questionId, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a single answer by ID
     * @param id the answer ID
     * @return optional answer response DTO
     */
    @Transactional(readOnly = true)
    public Optional<AnswerResponseDTO> getAnswerById(Long id) {
        return answerRepository.findByIdWithAuthor(id)
                .map(this::convertToResponseDTO);
    }

    /**
     * Create a new answer
     * @param answerDTO the answer data transfer object
     * @return the created answer entity
     * @throws RuntimeException if question or user not found
     */
    @Transactional
    public Answer createAnswer(AnswerDTO answerDTO) {
        Answer answer = Answer.builder()
                .content(answerDTO.getContent())
                .build();

        // Set the question
        Question question = questionRepository.findById(answerDTO.getQuestionId())
                .orElseThrow(() -> new RuntimeException("Question with ID " + answerDTO.getQuestionId() + " not found"));
        answer.setQuestion(question);

        // Set the author
        User author = userRepository.findById(answerDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User with ID " + answerDTO.getUserId() + " not found"));
        answer.setAuthor(author);

        return answerRepository.save(answer);
    }

    /**
     * Update an answer
     * @param id the answer ID
     * @param answerDTO the updated answer data
     * @return the updated answer entity
     * @throws RuntimeException if answer not found
     */
    @Transactional
    public Answer updateAnswer(Long id, AnswerDTO answerDTO) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + id + " not found"));

        answer.setContent(answerDTO.getContent());
        return answerRepository.save(answer);
    }

    /**
     * Delete an answer
     * @param id the answer ID
     * @throws RuntimeException if answer not found
     */
    @Transactional
    public void deleteAnswer(Long id) {
        Answer answer = answerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + id + " not found"));
        answerRepository.delete(answer);
    }

    /**
     * Like an answer
     * @param answerId the answer ID
     * @param userId the user ID
     * @throws RuntimeException if answer or user not found
     */
    @Transactional
    public void likeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + answerId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Add user to liked by set and increment like count
        answer.getLikedByUsers().add(user);
        answer.incrementLikeCount();

        answerRepository.save(answer);
    }

    /**
     * Unlike an answer
     * @param answerId the answer ID
     * @param userId the user ID
     * @throws RuntimeException if answer or user not found
     */
    @Transactional
    public void unlikeAnswer(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + answerId + " not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        // Remove user from liked by set and decrement like count
        if (answer.getLikedByUsers().contains(user)) {
            answer.getLikedByUsers().remove(user);
            answer.decrementLikeCount();
            answerRepository.save(answer);
        }
    }

    /**
     * Mark an answer as accepted
     * @param answerId the answer ID
     * @throws RuntimeException if answer not found
     */
    @Transactional
    public void markAsAccepted(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + answerId + " not found"));

        answer.markAsAccepted();
        answerRepository.save(answer);
    }

    /**
     * Unmark an answer as accepted
     * @param answerId the answer ID
     * @throws RuntimeException if answer not found
     */
    @Transactional
    public void unmarkAsAccepted(Long answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new RuntimeException("Answer with ID " + answerId + " not found"));

        answer.markAsNotAccepted();
        answerRepository.save(answer);
    }

    /**
     * Get number of answers for a question
     * @param questionId the question ID
     * @return count of answers
     */
    @Transactional(readOnly = true)
    public long getAnswerCountByQuestion(Long questionId) {
        return answerRepository.countCommentsByAnswer(questionId);
    }

    /**
     * Get all answers by a user
     * @param userId the user ID
     * @return list of answers by user
     */
    @Transactional(readOnly = true)
    public List<Answer> getAnswersByUser(Long userId) {
        return answerRepository.findCommentsByUser(userId);
    }

    /**
     * Convert Answer entity to AnswerResponseDTO
     * @param answer the answer entity
     * @return the response DTO
     */
    private AnswerResponseDTO convertToResponseDTO(Answer answer) {
        AnswerResponseDTO dto = new AnswerResponseDTO();
        dto.setId(answer.getId());
        dto.setContent(answer.getContent());
        dto.setIsAccepted(answer.getIsAccepted());
        dto.setCreatedAt(answer.getCreatedAt());
        dto.setUpdatedAt(answer.getUpdatedAt());
        
        // Map author to UserBasicDTO
        if (answer.getAuthor() != null) {
            UserBasicDTO userDTO = new UserBasicDTO();
            userDTO.setId(answer.getAuthor().getId());
            userDTO.setUsername(answer.getAuthor().getUsername());
            dto.setAuthor(userDTO);
        }
        
        // Map question info - just ID and title
        if (answer.getQuestion() != null) {
            dto.setQuestionId(answer.getQuestion().getId());
            dto.setQuestionTitle(answer.getQuestion().getTitle());
        }
        
        // Set like count
        dto.setLikeCount(answer.getLikeCount());
        
        // Set comment count
        dto.setCommentCount(answer.getCommentCount());
        
        return dto;
    }
}