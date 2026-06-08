package com.borrowapp.activity.entity;

import com.borrowapp.common.constants.ActivityLogAction;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
    @Index(name = "idx_alog_user_id",    columnList = "user_id"),
    @Index(name = "idx_alog_action",     columnList = "action"),
    @Index(name = "idx_activity_target", columnList = "target_type, target_id"),
    @Index(name = "idx_alog_created_at", columnList = "created_at")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người thực hiện hành động.
     * Dùng raw userId thay vì @ManyToOne để tránh phụ thuộc User module.
     */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityLogAction action;

    /** Loại đối tượng bị tác động: DEVICE, REQUEST, USER… */
    @Column(name = "target_type", length = 50)
    private String targetType;

    /** ID của đối tượng bị tác động */
    @Column(name = "target_id")
    private Long targetId;

    /** Mô tả chi tiết – JSON hoặc plain-text */
    @Column(columnDefinition = "TEXT")
    private String detail;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
