package com.example.quoraapplication.events;

import com.example.quoraapplication.models.Comment;

/**
 * Event publisher for publishing domain events
 * This interface defines events that should be published when important actions occur
 */
public interface EventPublisher {
    
    /**
     * Publish when an answer is created
     * @param answerId - the created answer ID
     * @param questionId - the question being answered
     * @param authorId - the user who created the answer
     */
    void publishAnswerCreated(Long answerId, Long questionId, Long authorId);
    
    /**
     * Publish when an answer is marked as accepted
     * @param answerId - the accepted answer ID
     * @param acceptedByUserId - the user who accepted it
     */
    void publishAnswerMarkedAsAccepted(Long answerId, Long acceptedByUserId);
    
    /**
     * Publish when a comment is created
     * @param comment - the created comment
     */
    void publishCommentCreated(Comment comment);
    
    /**
     * Publish when a comment is deleted
     * @param commentId - the deleted comment ID
     */
    void publishCommentDeleted(Long commentId);
}