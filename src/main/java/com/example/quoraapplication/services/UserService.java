package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.TagDTO;
import com.example.quoraapplication.dtos.UserDTO;
import com.example.quoraapplication.dtos.UserResponseDTO;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.TagRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    public UserService(UserRepository userRepository, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Allows a user to follow a tag
     * @param userId - ID of the user who wants to follow the tag
     * @param tagId - ID of the tag to be followed
     * @throws RuntimeException if user or tag is not found
     */
    @Transactional
    public void followTag(Long userId, Long tagId) {
        // Use custom query to eagerly fetch the followedTags collection
        User user = userRepository.findByIdWithTags(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag with ID " + tagId + " not found"));

        // Initialize the followedTags set if it's null
        if (user.getFollowedTags() == null) {
            user.setFollowedTags(new HashSet<>());
        }

        // Add the tag to user's followed tags (Set will handle duplicates automatically)
        user.getFollowedTags().add(tag);

        // Save the updated user
        userRepository.save(user);
    }

    /**
     * Convert User entity to UserResponseDTO
     */
    private UserResponseDTO convertToResponseDTO(User user) {
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        
        // Map followedTags to TagDTOs
        if (user.getFollowedTags() != null) {
            Set<TagDTO> tagDTOs = user.getFollowedTags().stream()
                    .map(tag -> {
                        TagDTO tagDTO = new TagDTO();
                        tagDTO.setId(tag.getId());
                        tagDTO.setName(tag.getName());
                        return tagDTO;
                    })
                    .collect(Collectors.toSet());
            dto.setFollowedTags(tagDTOs);
        }
        
        return dto;
    }
}