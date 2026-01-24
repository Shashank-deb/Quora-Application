package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.*;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.QuestionRepository;
import com.example.quoraapplication.repositories.TagRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public QuestionService(QuestionRepository questionRepository, UserRepository userRepository, TagRepository tagRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public void deleteQuestion(Long id) {
        questionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDTO> getQuestions(int offset, int limit) {
        return questionRepository.findAll(PageRequest.of(offset, limit))
                .getContent()
                .stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<QuestionResponseDTO> getQuestionById(Long id) {
        return questionRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public Question createQuestion(QuestionDTO questionDTO) {
        Question question = new Question();
        question.setTitle(questionDTO.getTitle());
        question.setContent(questionDTO.getContent());

        Optional<User> user = userRepository.findById(questionDTO.getUserId());
        user.ifPresent(question::setUser);
        
        Set<Tag> tags = questionDTO.getTagIds().stream()
                .map(tagRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        question.setTags(tags);
        return questionRepository.save(question);
    }

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