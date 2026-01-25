package com.example.quoraapplication.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerResponseDTO {

    private Long id;
    private String content;
    private Boolean isAccepted;
    private Integer likeCount;
    private Long authorId;
    private String authorName;
    private Long questionId;
    private Integer commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ============================================================================
    // Helper Methods
    // ============================================================================

    /**
     * Get full answer summary
     */
    public String getSummary() {
        return "Answer by " + authorName + " on question " + questionId + 
               (isAccepted ? " (Accepted)" : "");
    }

    /**
     * Check if answer has content
     */
    public boolean hasContent() {
        return content != null && !content.trim().isEmpty();
    }

    /**
     * Get formatted like count
     */
    public String getFormattedLikeCount() {
        if (likeCount == null || likeCount == 0) {
            return "No likes";
        } else if (likeCount == 1) {
            return "1 like";
        } else {
            return likeCount + " likes";
        }
    }

    /**
     * Get acceptance status
     */
    public String getAcceptanceStatus() {
        return isAccepted != null && isAccepted ? "Accepted" : "Pending";
    }

    // ============================================================================
    // toString
    // ============================================================================

    @Override
    public String toString() {
        return "AnswerResponseDTO{" +
                "id=" + id +
                ", content='" + (content != null && content.length() > 50 
                    ? content.substring(0, 50) + "..." 
                    : content) + '\'' +
                ", isAccepted=" + isAccepted +
                ", likeCount=" + likeCount +
                ", authorId=" + authorId +
                ", authorName='" + authorName + '\'' +
                ", questionId=" + questionId +
                ", commentCount=" + commentCount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}