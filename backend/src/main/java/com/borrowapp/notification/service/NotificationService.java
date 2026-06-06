package com.borrowapp.notification.service;

import com.borrowapp.common.response.PageResponse;
import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.enums.NotificationType;
import org.springframework.data.domain.Page;

public interface NotificationService {


    void sendAndNotify(Long recipientId, String recipientEmail,
                       NotificationType type,
                       String title, String message, String link,
                       String emailSubject, String emailHtmlBody);

    void notifyOnly(Long recipientId, String recipientEmail,
                    NotificationType type,
                    String title, String message, String link);


    NotificationBellResponse getBell(Long userId, int page, int pageSize);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);


    Page<NotificationLogResponse> getFailedLogs(int page, int pageSize);

    void retryEmail(Long notificationLogId);


    PageResponse<NotificationLogResponse> getNotificationLogs(
            int page, int pageSize, NotificationLogStatus status);
}