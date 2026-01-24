package com.example.quoraapplication.controllers;

import com.example.quoraapplication.dtos.QuestionDTO;
import com.example.quoraapplication.dtos.QuestionResponseDTO;
import com.example.quoraapplication.models.Question;
import com.example.quoraapplication.services.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponseDTO>> getAllQuestions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<QuestionResponseDTO> questions = questionService.getQuestions(page, size);
        return ResponseEntity.ok(questions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDTO> getQuestionById(@PathVariable Long id) {
        Optional<QuestionResponseDTO> question = questionService.getQuestionById(id);
        return question.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestBody QuestionDTO questionDTO) {
        Question createdQuestion = questionService.createQuestion(questionDTO);
        return ResponseEntity.ok(createdQuestion);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}