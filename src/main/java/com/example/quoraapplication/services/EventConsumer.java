package com.example.quoraapplication.services;


import com.example.quoraapplication.exception.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumer {

    // ===== User Events =====
    @KafkaListener(topics = "user-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeUserEvent(UserEvent event) {
        log.info("Consumed UserEvent: type={}, userId={}", event.getEventType(), event.getUserId());

        switch (event.getEventType()) {
            case "USER_SIGNED_UP":
                handleUserSignedUp(event);
                break;
            case "USER_PROFILE_UPDATED":
                handleUserProfileUpdated(event);
                break;
            default:
                log.warn("Unknown user event type: {}", event.getEventType());
        }
    }

    private void handleUserSignedUp(UserEvent event) {
        // Send welcome email, initialize user profile, etc.
        log.info("Handling USER_SIGNED_UP for user: {}", event.getUserId());
    }

    private void handleUserProfileUpdated(UserEvent event) {
        // Invalidate caches, update search index, etc.
        log.info("Handling USER_PROFILE_UPDATED for user: {}", event.getUserId());
    }

    // ===== Question Events =====
    @KafkaListener(topics = "question-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeQuestionEvent(QuestionEvent event) {
        log.info("Consumed QuestionEvent: type={}, questionId={}", event.getEventType(), event.getQuestionId());

        switch (event.getEventType()) {
            case "QUESTION_CREATED":
                handleQuestionCreated(event);
                break;
            case "QUESTION_EDITED":
                handleQuestionEdited(event);
                break;
            case "QUESTION_DELETED":
                handleQuestionDeleted(event);
                break;
            default:
                log.warn("Unknown question event type: {}", event.getEventType());
        }
    }

    private void handleQuestionCreated(QuestionEvent event) {
        // Index in Elasticsearch, notify followers, etc.
        log.info("Handling QUESTION_CREATED: {}", event.getQuestionId());
    }

    private void handleQuestionEdited(QuestionEvent event) {
        // Update search index, notify watchers, etc.
        log.info("Handling QUESTION_EDITED: {}", event.getQuestionId());
    }

    private void handleQuestionDeleted(QuestionEvent event) {
        // Remove from search index, clean up related data, etc.
        log.info("Handling QUESTION_DELETED: {}", event.getQuestionId());
    }

    // ===== Answer Events =====
    @KafkaListener(topics = "answer-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAnswerEvent(AnswerEvent event) {
        log.info("Consumed AnswerEvent: type={}, answerId={}", event.getEventType(), event.getAnswerId());

        switch (event.getEventType()) {
            case "ANSWER_CREATED":
                handleAnswerCreated(event);
                break;
            case "ANSWER_MARKED_ACCEPTED":
                handleAnswerAccepted(event);
                break;
            default:
                log.warn("Unknown answer event type: {}", event.getEventType());
        }
    }

    private void handleAnswerCreated(AnswerEvent event) {
        // Index in Elasticsearch, notify question author, etc.
        log.info("Handling ANSWER_CREATED: {}", event.getAnswerId());
    }

    private void handleAnswerAccepted(AnswerEvent event) {
        // Update question status, notify answer author, etc.
        log.info("Handling ANSWER_MARKED_ACCEPTED: {}", event.getAnswerId());
    }

    // ===== Engagement Events =====
    @KafkaListener(topics = "engagement-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeEngagementEvent(EngagementEvent event) {
        log.info("Consumed EngagementEvent: type={}, userId={}", event.getEventType(), event.getUserId());

        switch (event.getEventType()) {
            case "USER_FOLLOWED_TAG":
                handleUserFollowedTag(event);
                break;
            case "QUESTION_LIKED":
                handleQuestionLiked(event);
                break;
            default:
                log.warn("Unknown engagement event type: {}", event.getEventType());
        }
    }

    private void handleUserFollowedTag(EngagementEvent event) {
        // Update user preferences, personalize feed, etc.
        log.info("Handling USER_FOLLOWED_TAG: user={}, tag={}", event.getUserId(), event.getTagId());
    }

    private void handleQuestionLiked(EngagementEvent event) {
        // Update like count, notify question author, etc.
        log.info("Handling QUESTION_LIKED: user={}, question={}", event.getUserId(), event.getQuestionId());
    }

    // ===== Notification Events =====
    @KafkaListener(topics = "notification-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeNotificationEvent(NotificationEvent event) {
        log.info("Consumed NotificationEvent: type={}, userId={}", event.getNotificationType(), event.getUserId());

        // Send email, push notification, SMS, etc.
        log.info("Sending notification to user: {}", event.getUserId());
    }

    // ===== Audit Log Events =====
    @KafkaListener(topics = "audit-log-events", groupId = "quora-app", containerFactory = "kafkaListenerContainerFactory")
    public void consumeAuditLogEvent(AuditLogEvent event) {
        log.info("Consumed AuditLogEvent: action={}, userId={}, resource={}", 
                event.getAction(), event.getUserId(), event.getResourceType());

        // Store in audit database, send to compliance system, etc.
        log.debug("Audit log stored: {}", event);
    }
}