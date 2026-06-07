package com.borrowapp.testutil;

import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.notification.entity.Notification;
import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.enums.NotificationStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.user.entity.User;

/**
 * Central fixture factory – giữ test data nhất quán qua toàn bộ test suite.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── ActivityLog ───────────────────────────────────────────────────────────

    /**
     * Tạo một ActivityLog có actor (user hợp lệ).
     * ActivityLog.user là @ManyToOne(User) → phải truyền User object.
     */
    public static ActivityLog activityLog(Long id, ActivityLogAction action) {
        User user = User.builder()
                .id(1L)
                .fullName("admin")
                .build();

        return ActivityLog.builder()
                .id(id)
                .user(user)
                .action(action)
                .targetType("REQUEST")
                .targetId(100L)
                .detail("{\"device\":\"Laptop\"}")
                .build();
    }

    /**
     * Tạo một ActivityLog system action (không có actor, user = null).
     */
    public static ActivityLog systemLog(Long id) {
        return ActivityLog.builder()
                .id(id)
                .user(null)          // system action – không có user
                .action(ActivityLogAction.MARK_OVERDUE)
                .targetType("REQUEST")
                .targetId(200L)
                .detail("Auto-marked overdue")
                .build();
    }

    // ── Notification ──────────────────────────────────────────────────────────

    public static Notification unreadNotification(Long id, Long userId) {
        return Notification.builder()
                .id(id)
                .recipientId(userId)
                .recipientEmail("user@example.com")
                .type(NotificationType.REQUEST_APPROVED)
                .title("Yêu cầu được duyệt")
                .message("Yêu cầu mượn Laptop đã được duyệt.")
                .link("/requests/100")
                .status(NotificationStatus.UNREAD)
                .build();
    }

    public static Notification readNotification(Long id, Long userId) {
        Notification n = unreadNotification(id, userId);
        n.markAsRead();
        return n;
    }

    // ── NotificationLog ───────────────────────────────────────────────────────

    public static NotificationLog failedLog(Long id) {
        return NotificationLog.builder()
                .id(id)
                .toEmail("user@example.com")
                .subject("Test Subject")
                .body("<p>Test Body</p>")
                .status(NotificationLogStatus.FAILED)
                .retryCount(1)
                .errorMessage("Connection refused")
                .build();
    }

    public static NotificationLog successLog(Long id) {
        return NotificationLog.builder()
                .id(id)
                .toEmail("user@example.com")
                .subject("Test Subject")
                .body("<p>Test Body</p>")
                .status(NotificationLogStatus.SUCCESS)
                .retryCount(0)
                .build();
    }

    public static NotificationLog freshLog(Long id) {
        return NotificationLog.builder()
                .id(id)
                .toEmail("user@example.com")
                .subject("Test Subject")
                .body("<p>Test Body</p>")
                .status(NotificationLogStatus.FAILED)
                .retryCount(0)
                .build();
    }
}