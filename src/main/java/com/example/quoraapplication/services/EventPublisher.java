package com.example.quoraapplication.services;

/**
 * Event publisher for publishing domain events
 * This interface defines events that should be published when important actions occur
 */
public interface EventPublisher {
    
    /**
     * Publish when an answer is created
     */
    void publishAnswerCreated(Long answerId, Long questionId, Long userId);
    
    /**
     * Publish when an answer is marked as accepted
     */
    void publishAnswerMarkedAsAccepted(Long answerId, Long questionId);
    
    /**
     * Publish when a comment is created
     */
    void publishCommentCreated(Long commentId, Long answerId, Long userId);
    
    /**
     * Publish when a comment is deleted
     */
    void publishCommentDeleted(Long commentId, Long answerId);
}