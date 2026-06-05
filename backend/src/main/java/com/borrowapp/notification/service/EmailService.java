package com.borrowapp.notification.service;

public interface EmailService {

    /**
     * Gửi email bất đồng bộ (fire and forget).
     * - Tạo NotificationLog với status=FAILED trước khi gửi.
     * - Nếu gửi thành công → cập nhật status=SUCCESS.
     * - Nếu lỗi → tăng retryCount, lưu errorMessage, KHÔNG ném exception.
     *
     * @param to      Địa chỉ email nhận
     * @param subject Tiêu đề email
     * @param htmlBody Nội dung HTML
     */
    void sendAsync(String to, String subject, String htmlBody);

    /**
     * Retry gửi lại email đã FAILED theo NotificationLog ID.
     * Chạy bất đồng bộ – ghi log activity RETRY_EMAIL.
     *
     * @param notificationLogId ID của NotificationLog cần retry
     */
    void retryAsync(Long notificationLogId);
}
