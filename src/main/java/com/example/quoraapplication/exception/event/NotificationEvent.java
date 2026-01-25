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
public class NotificationEvent {
    private String eventType;
    private Long userId;
    private String notificationType;
    private String message;
    private LocalDateTime timestamp;
}