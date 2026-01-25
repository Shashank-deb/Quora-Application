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
public class AnswerEvent {
    private String eventType;
    private Long answerId;
    private Long questionId;
    private Long userId;
    private LocalDateTime timestamp;
}
