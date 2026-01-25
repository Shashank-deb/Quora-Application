package com.example.quoraapplication.events;

import com.example.quoraapplication.models.Comment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of EventPublisher using Kafka for event distribution
 * Publishes domain events to Kafka topics for asynchronous processing
 */
@Component
@Slf4j
public class EventPublisherImpl implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public EventPublisherImpl(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ============================================================================
    // Event Publishing Methods
    // ============================================================================

    /**
     * Publish answer created event - CORRECTED SIGNATURE
     * Now includes authorId parameter
     */
    @Override
    public void publishAnswerCreated(Long answerId, Long questionId, Long authorId) {
        log.info("Publishing AnswerCreated event for answer ID: {}, question ID: {}, author ID: {}", 
                answerId, questionId, authorId);

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("answerId", answerId);
            event.put("questionId", questionId);
            event.put("authorId", authorId);
            event.put("eventType", "ANSWER_CREATED");
            event.put("timestamp", LocalDateTime.now().toString());

            String eventJson = convertToJson(event);
            kafkaTemplate.send("answer-events", eventJson);

            log.info("AnswerCreated event published successfully");
        } catch (Exception e) {
            log.error("Error publishing AnswerCreated event", e);
        }
    }

    /**
     * Publish answer marked as accepted event
     */
    @Override
    public void publishAnswerMarkedAsAccepted(Long answerId, Long acceptedByUserId) {
        log.info("Publishing AnswerMarkedAsAccepted event for answer ID: {}, accepted by user ID: {}", 
                answerId, acceptedByUserId);

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("answerId", answerId);
            event.put("acceptedByUserId", acceptedByUserId);
            event.put("eventType", "ANSWER_ACCEPTED");
            event.put("timestamp", LocalDateTime.now().toString());

            String eventJson = convertToJson(event);
            kafkaTemplate.send("answer-events", eventJson);

            log.info("AnswerMarkedAsAccepted event published successfully");
        } catch (Exception e) {
            log.error("Error publishing AnswerMarkedAsAccepted event", e);
        }
    }

    /**
     * Publish comment created event
     */
    @Override
    public void publishCommentCreated(Comment comment) {
        log.info("Publishing CommentCreated event for comment ID: {}", comment.getId());

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("commentId", comment.getId());
            event.put("authorId", comment.getAuthor() != null ? comment.getAuthor().getId() : null);
            event.put("questionId", comment.getQuestion() != null ? comment.getQuestion().getId() : null);
            event.put("answerId", comment.getAnswer() != null ? comment.getAnswer().getId() : null);
            event.put("eventType", "COMMENT_CREATED");
            event.put("timestamp", LocalDateTime.now().toString());

            String eventJson = convertToJson(event);
            kafkaTemplate.send("engagement-events", eventJson);

            log.info("CommentCreated event published successfully");
        } catch (Exception e) {
            log.error("Error publishing CommentCreated event", e);
        }
    }

    /**
     * Publish comment deleted event
     */
    @Override
    public void publishCommentDeleted(Long commentId) {
        log.info("Publishing CommentDeleted event for comment ID: {}", commentId);

        try {
            Map<String, Object> event = new HashMap<>();
            event.put("commentId", commentId);
            event.put("eventType", "COMMENT_DELETED");
            event.put("timestamp", LocalDateTime.now().toString());

            String eventJson = convertToJson(event);
            kafkaTemplate.send("engagement-events", eventJson);

            log.info("CommentDeleted event published successfully");
        } catch (Exception e) {
            log.error("Error publishing CommentDeleted event", e);
        }
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Convert map to JSON string
     * Simple implementation - use Jackson ObjectMapper in production
     */
    private String convertToJson(Map<String, Object> map) {
        StringBuilder json = new StringBuilder("{");
        map.forEach((key, value) -> {
            if (value instanceof String) {
                json.append("\"").append(key).append("\":\"").append(value).append("\",");
            } else {
                json.append("\"").append(key).append("\":").append(value).append(",");
            }
        });
        if (json.length() > 1) {
            json.deleteCharAt(json.length() - 1); // Remove last comma
        }
        json.append("}");
        return json.toString();
    }
}