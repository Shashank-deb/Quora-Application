package com.example.quoraapplication.services;

import com.example.quoraapplication.dtos.TagDTO;
import com.example.quoraapplication.dtos.TagResponseDTO;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.repositories.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {
    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<TagResponseDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    public Tag createTag(TagDTO tagDTO) {
        Tag tag = new Tag();
        tag.setName(tagDTO.getName());
        return tagRepository.save(tag);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<TagResponseDTO> getTagById(Long id) {
        return tagRepository.findById(id)
                .map(this::convertToResponseDTO);
    }

    /**
     * Convert Tag entity to TagResponseDTO
     */
    private TagResponseDTO convertToResponseDTO(Tag tag) {
        TagResponseDTO dto = new TagResponseDTO();
        dto.setId(tag.getId());
        dto.setName(tag.getName());
        
        // Get follower count without loading the entire collection
        if (tag.getFollowers() != null) {
            dto.setFollowerCount(tag.getFollowers().size());
        } else {
            dto.setFollowerCount(0);
        }
        
        return dto;
    }
}