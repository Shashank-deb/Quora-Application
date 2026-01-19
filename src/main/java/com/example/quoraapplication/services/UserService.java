package com.example.quoraapplication.services;


import com.example.quoraapplication.dtos.UserDTO;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.models.User;
import com.example.quoraapplication.repositories.TagRepository;
import com.example.quoraapplication.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    private TagRepository tagRepository;

    public UserService(UserRepository userRepository, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
    }

    public User createUser(UserDTO userDTO){
        User user= new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());
        return userRepository.save(user);
    }

    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    /**
     * Allows a user to follow a tag
     * @param userId - ID of the user who wants to follow the tag
     * @param tagId - ID of the tag to be followed
     * @throws RuntimeException if user or tag is not found
     */
    public void followTag(Long userId, Long tagId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Tag> tagOptional = tagRepository.findById(tagId);

        if (userOptional.isEmpty()) {
            throw new RuntimeException("User with ID " + userId + " not found");
        }

        if (tagOptional.isEmpty()) {
            throw new RuntimeException("Tag with ID " + tagId + " not found");
        }

        User user = userOptional.get();
        Tag tag = tagOptional.get();

        // Initialize the followedTags set if it's null
        if (user.getFollowedTags() == null) {
            user.setFollowedTags(new HashSet<>());
        }

        // Add the tag to user's followed tags (Set will handle duplicates automatically)
        user.getFollowedTags().add(tag);

        // Save the updated user
        userRepository.save(user);
    }
}