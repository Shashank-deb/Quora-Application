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
public class AuditLogEvent {
    private String eventType;
    private Long userId;
    private String action;
    private String resourceType;
    private Long resourceId;
    private String details;
    private LocalDateTime timestamp;
}