package com.example.quoraapplication.exception.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EngagementEvent {
    private String eventType;
    private Long userId;
    private Long questionId;
    private Long tagId;
    private String tagName;
    private LocalDateTime timestamp;
}