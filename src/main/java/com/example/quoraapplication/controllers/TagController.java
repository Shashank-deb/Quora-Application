package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.TagDTO;
import com.example.quoraapplication.dtos.TagResponseDTO;
import com.example.quoraapplication.models.Tag;
import com.example.quoraapplication.services.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public ResponseEntity<List<TagResponseDTO>> getAllTags() {
        List<TagResponseDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TagResponseDTO> getTagById(@PathVariable Long id) {
        Optional<TagResponseDTO> tag = tagService.getTagById(id);
        return tag.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody TagDTO tagDTO) {
        Tag createdTag = tagService.createTag(tagDTO);
        return ResponseEntity.ok(createdTag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }
}