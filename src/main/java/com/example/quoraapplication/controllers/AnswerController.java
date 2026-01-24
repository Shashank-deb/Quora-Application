package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.AnswerDTO;
import com.example.quoraapplication.dtos.AnswerResponseDTO;
import com.example.quoraapplication.models.Answer;
import com.example.quoraapplication.services.AnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/answers")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @GetMapping
    public ResponseEntity<List<AnswerResponseDTO>> getAnswersByQuestionId(
            @RequestParam Long questionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<AnswerResponseDTO> answers = answerService.getAnswersByQuestionId(questionId, page, size);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnswerResponseDTO> getAnswerById(@PathVariable Long id) {
        Optional<AnswerResponseDTO> answer = answerService.getAnswerById(id);
        return answer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Answer> createAnswer(@RequestBody AnswerDTO answerDTO) {
        Answer createdAnswer = answerService.createAnswer(answerDTO);
        return ResponseEntity.ok(createdAnswer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{answerId}/like/{userId}")
    public ResponseEntity<Void> likeAnswer(
            @PathVariable Long answerId,
            @PathVariable Long userId) {
        answerService.likeAnswer(answerId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{answerId}/unlike/{userId}")
    public ResponseEntity<Void> unlikeAnswer(
            @PathVariable Long answerId,
            @PathVariable Long userId) {
        answerService.unlikeAnswer(answerId, userId);
        return ResponseEntity.noContent().build();
    }
}