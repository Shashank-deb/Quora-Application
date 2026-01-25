package com.example.quoraapplication.services;


import com.example.quoraapplication.exception.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.user-events:user-events}")
    private String userEventsTopic;

    @Value("${kafka.topics.question-events:question-events}")
    private String questionEventsTopic;

    @Value("${kafka.topics.answer-events:answer-events}")
    private String answerEventsTopic;

    @Value("${kafka.topics.engagement-events:engagement-events}")
    private String engagementEventsTopic;

    @Value("${kafka.topics.notification-events:notification-events}")
    private String notificationEventsTopic;

    @Value("${kafka.topics.audit-log-events:audit-log-events}")
    private String auditLogEventsTopic;

    // ===== User Events =====
    public void publishUserSignedUp(Long userId, String username, String email) {
        UserEvent event = UserEvent.builder()
                .eventType("USER_SIGNED_UP")
                .userId(userId)
                .username(username)
                .email(email)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(userEventsTopic, String.valueOf(userId), event);
        log.info("Published USER_SIGNED_UP event for user: {}", userId);
    }

    public void publishUserProfileUpdated(Long userId, String username) {
        UserEvent event = UserEvent.builder()
                .eventType("USER_PROFILE_UPDATED")
                .userId(userId)
                .username(username)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(userEventsTopic, String.valueOf(userId), event);
        log.info("Published USER_PROFILE_UPDATED event for user: {}", userId);
    }

    // ===== Question Events =====
    public void publishQuestionCreated(Long questionId, Long userId, String title) {
        QuestionEvent event = QuestionEvent.builder()
                .eventType("QUESTION_CREATED")
                .questionId(questionId)
                .userId(userId)
                .title(title)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(questionEventsTopic, String.valueOf(questionId), event);
        log.info("Published QUESTION_CREATED event: {}", questionId);
    }

    public void publishQuestionEdited(Long questionId, Long userId) {
        QuestionEvent event = QuestionEvent.builder()
                .eventType("QUESTION_EDITED")
                .questionId(questionId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(questionEventsTopic, String.valueOf(questionId), event);
        log.info("Published QUESTION_EDITED event: {}", questionId);
    }

    public void publishQuestionDeleted(Long questionId, Long userId) {
        QuestionEvent event = QuestionEvent.builder()
                .eventType("QUESTION_DELETED")
                .questionId(questionId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(questionEventsTopic, String.valueOf(questionId), event);
        log.info("Published QUESTION_DELETED event: {}", questionId);
    }

    // ===== Answer Events =====
    public void publishAnswerCreated(Long answerId, Long questionId, Long userId) {
        AnswerEvent event = AnswerEvent.builder()
                .eventType("ANSWER_CREATED")
                .answerId(answerId)
                .questionId(questionId)
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(answerEventsTopic, String.valueOf(answerId), event);
        log.info("Published ANSWER_CREATED event: {}", answerId);
    }

    public void publishAnswerMarkedAsAccepted(Long answerId, Long questionId) {
        AnswerEvent event = AnswerEvent.builder()
                .eventType("ANSWER_MARKED_ACCEPTED")
                .answerId(answerId)
                .questionId(questionId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(answerEventsTopic, String.valueOf(answerId), event);
        log.info("Published ANSWER_MARKED_ACCEPTED event: {}", answerId);
    }

    // ===== Engagement Events =====
    public void publishUserFollowedTag(Long userId, Long tagId, String tagName) {
        EngagementEvent event = EngagementEvent.builder()
                .eventType("USER_FOLLOWED_TAG")
                .userId(userId)
                .tagId(tagId)
                .tagName(tagName)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(engagementEventsTopic, String.valueOf(userId), event);
        log.info("Published USER_FOLLOWED_TAG event: user={}, tag={}", userId, tagId);
    }

    public void publishQuestionLiked(Long userId, Long questionId) {
        EngagementEvent event = EngagementEvent.builder()
                .eventType("QUESTION_LIKED")
                .userId(userId)
                .questionId(questionId)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(engagementEventsTopic, String.valueOf(userId), event);
        log.info("Published QUESTION_LIKED event: user={}, question={}", userId, questionId);
    }

    // ===== Notification Events =====
    public void publishNotificationEvent(Long userId, String notificationType, String message) {
        NotificationEvent event = NotificationEvent.builder()
                .eventType("NOTIFICATION")
                .userId(userId)
                .notificationType(notificationType)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(notificationEventsTopic, String.valueOf(userId), event);
        log.info("Published NOTIFICATION event for user: {}", userId);
    }

    // ===== Audit Log Events =====
    public void publishAuditLog(Long userId, String action, String resourceType, Long resourceId, String details) {
        AuditLogEvent event = AuditLogEvent.builder()
                .eventType("AUDIT_LOG")
                .userId(userId)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        kafkaTemplate.send(auditLogEventsTopic, String.valueOf(userId), event);
        log.debug("Published AUDIT_LOG event: user={}, action={}, resource={}", userId, action, resourceType);
    }
}