package com.borrowapp.notification.service;

import com.borrowapp.common.response.PageResponse;
import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.enums.NotificationType;
import org.springframework.data.domain.Page;

public interface NotificationService {

    /**
     * Tạo notification in-app VÀ gửi email bất đồng bộ cùng lúc.
     */
    void sendAndNotify(Long recipientId, String recipientEmail,
                       NotificationType type,
                       String title, String message, String link,
                       String emailSubject, String emailHtmlBody);

    /**
     * Tạo notification in-app only – KHÔNG gửi email.
     */
    void notifyOnly(Long recipientId, String recipientEmail,
                    NotificationType type,
                    String title, String message, String link);

    /**
     * Bell API – lấy danh sách notification + unreadCount.
     */
    NotificationBellResponse getBell(Long userId, int page, int pageSize);

    /**
     * Đánh dấu một notification cụ thể là đã đọc.
     */
    void markAsRead(Long userId, Long notificationId);

    /**
     * Đánh dấu tất cả notification của người dùng là đã đọc.
     */
    void markAllAsRead(Long userId);

    /**
     * Lấy danh sách email gửi thất bại – dành cho ADMIN.
     */
    Page<NotificationLogResponse> getFailedLogs(int page, int pageSize);

    /**
     * Retry gửi lại một email thất bại theo NotificationLog ID.
     */
    void retryEmail(Long notificationLogId);

    /**
     * Lấy danh sách notification logs (với filter status) – dành cho ADMIN.
     */
    PageResponse<NotificationLogResponse> getNotificationLogs(
            int page, int pageSize, NotificationLogStatus status);
}
