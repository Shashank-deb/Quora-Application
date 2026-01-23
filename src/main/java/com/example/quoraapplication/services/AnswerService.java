package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.AnswerDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.AnswerRepository;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

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

    public List<Answer> getAnswersByQuestionId(Long questionId, int page, int size) {
        return answerRepository.findByQuestionId(questionId, PageRequest.of(page, size)).getContent();
    }

    public Optional<Answer> getAnswerById(Long id) {
        return answerRepository.findById(id);
    }

    public Answer createAnswer(AnswerDTO answerDTO) {
        Answer answer = new Answer();
        answer.setContent(answerDTO.getContent());

        Optional<Question> question = questionRepository.findById(answerDTO.getQuestionId());
        question.ifPresent(answer::setQuestion);

        Optional<User> user = userRepository.findById(answerDTO.getUserId());
        user.ifPresent(answer::setUser);

        return answerRepository.save(answer);
    }

    public void deleteAnswer(Long id) {
        answerRepository.deleteById(id);
    }

    /**
     * Allows a user to like an answer
     * @param answerId - ID of the answer to be liked
     * @param userId - ID of the user who is liking the answer
     * @throws RuntimeException if answer or user is not found
     */
    public void likeAnswer(Long answerId, Long userId) {
        Optional<Answer> answerOptional = answerRepository.findById(answerId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (answerOptional.isEmpty()) {
            throw new RuntimeException("Answer with ID " + answerId + " not found");
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        Answer answer = answerOptional.get();
        User user = userOptional.get();

        // Initialize the likedBy set if it's null
        if (answer.getLikedBy() == null) {
            answer.setLikedBy(new HashSet<>());
        }

        // Add the user to answer's liked by set (Set will handle duplicates automatically)
        answer.getLikedBy().add(user);

        // Save the updated answer
        answerRepository.save(answer);
    }

    /**
     * Allows a user to unlike an answer
     * @param answerId - ID of the answer to be unliked
     * @param userId - ID of the user who is unliking the answer
     * @throws RuntimeException if answer or user is not found
     */
    public void unlikeAnswer(Long answerId, Long userId) {
        Optional<Answer> answerOptional = answerRepository.findById(answerId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (answerOptional.isEmpty()) {
            throw new RuntimeException("Answer with ID " + answerId + " not found");
        }

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        Answer answer = answerOptional.get();
        User user = userOptional.get();

        // Remove the user from answer's liked by set if present
        if (answer.getLikedBy() != null) {
            answer.getLikedBy().remove(user);
            answerRepository.save(answer);
        }
    }
}