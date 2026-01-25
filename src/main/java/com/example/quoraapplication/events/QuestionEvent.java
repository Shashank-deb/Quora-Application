package com.example.quoraapplication.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionEvent {
    private String eventType;
    private Long questionId;
    private Long userId;
    private String title;
    private LocalDateTime timestamp;
}
