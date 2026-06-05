package com.borrowapp.notification.service;

import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationType;
import org.springframework.data.domain.Page;

public interface NotificationService {

    /**
     * Tạo notification in-app VÀ gửi email bất đồng bộ cùng lúc.
     *
     * @param recipientId    ID người nhận
     * @param recipientEmail Email người nhận
     * @param type           Loại notification
     * @param title          Tiêu đề in-app
     * @param message        Nội dung in-app
     * @param link           Link điều hướng (optional)
     * @param emailSubject   Tiêu đề email
     * @param emailHtmlBody  Nội dung HTML email
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
     *
     * @param userId   ID người dùng hiện tại
     * @param page     Số trang (bắt đầu từ 0)
     * @param pageSize Kích thước trang (tối đa 50)
     */
    NotificationBellResponse getBell(Long userId, int page, int pageSize);

    /**
     * Đánh dấu một notification cụ thể là đã đọc.
     * Kiểm tra ownership trước khi cập nhật.
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
}
