package com.example.quoraapplication.config;

/**
 * Kafka Topics Constants
 * Centralized configuration for all Kafka topic names
 */
public final class KafkaTopics {
    
    private KafkaTopics() {
        throw new AssertionError("Cannot instantiate utility class");
    }
    
    // User Events
    public static final String USER_EVENTS = "user-events";
    
    // Question Events
    public static final String QUESTION_EVENTS = "question-events";
    
    // Answer Events
    public static final String ANSWER_EVENTS = "answer-events";
    
    // Comment Events (mapped to engagement)
    public static final String ENGAGEMENT_EVENTS = "engagement-events";
    
    // Notification Events
    public static final String NOTIFICATION_EVENTS = "notification-events";
    
    // Audit Log Events
    public static final String AUDIT_LOG_EVENTS = "audit-log-events";
    
    /**
     * Get all topic names for validation
     */
    public static String[] getAllTopics() {
        return new String[]{
            USER_EVENTS,
            QUESTION_EVENTS,
            ANSWER_EVENTS,
            ENGAGEMENT_EVENTS,
            NOTIFICATION_EVENTS,
            AUDIT_LOG_EVENTS
        };
    }
}