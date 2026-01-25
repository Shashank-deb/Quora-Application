package com.example.quoraapplication.events;

import com.example.quoraapplication.models.Comment;

/**
 * Event Publisher Interface for publishing domain events
 * Defines contract for publishing various application events
 */
public interface EventPublisher {

    /**
     * Publish event when an answer is created
     * @param answerId - the ID of the created answer
     * @param questionId - the ID of the question
     * @param authorId - the ID of the user who created the answer
     */
    void publishAnswerCreated(Long answerId, Long questionId, Long authorId);

    /**
     * Publish event when an answer is marked as accepted
     * @param answerId - the ID of the answer
     * @param acceptedByUserId - the ID of the user who accepted it
     */
    void publishAnswerMarkedAsAccepted(Long answerId, Long acceptedByUserId);

    /**
     * Publish event when a comment is created
     * @param comment - the created comment object
     */
    void publishCommentCreated(Comment comment);

    /**
     * Publish event when a comment is deleted
     * @param commentId - the ID of the deleted comment
     */
    void publishCommentDeleted(Long commentId);
}