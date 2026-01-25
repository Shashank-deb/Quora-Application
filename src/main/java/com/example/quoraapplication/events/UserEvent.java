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
public class UserEvent {
    private String eventType;
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime timestamp;
}







