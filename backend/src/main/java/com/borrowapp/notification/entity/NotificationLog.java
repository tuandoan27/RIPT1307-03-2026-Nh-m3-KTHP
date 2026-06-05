package com.borrowapp.notification.entity;

import com.borrowapp.notification.enums.NotificationLogStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs", indexes = {
    @Index(name = "idx_nlog_status",     columnList = "status"),
    @Index(name = "idx_nlog_to_email",   columnList = "to_email"),
    @Index(name = "idx_nlog_created_at", columnList = "created_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "to_email", nullable = false)
    private String toEmail;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private NotificationLogStatus status = NotificationLogStatus.FAILED;

    /** Số lần đã thử gửi */
    @Builder.Default
    @Column(name = "retry_count")
    private int retryCount = 0;

    /** Thông báo lỗi cuối cùng */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Domain methods ───────────────────────────────────────────────────────

    public void incrementRetry(String error) {
        this.retryCount++;
        this.errorMessage = error;
        this.status = NotificationLogStatus.FAILED;
    }

    public void markSuccess() {
        this.status = NotificationLogStatus.SUCCESS;
        this.errorMessage = null;
    }

    public void markRetrying() {
        this.status = NotificationLogStatus.RETRYING;
    }
}
