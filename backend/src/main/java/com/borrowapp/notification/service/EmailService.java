package com.borrowapp.notification.service;

import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.user.entity.User;

public interface EmailService {

    /**
     * Gửi email bất đồng bộ (fire and forget).
     * - Tạo NotificationLog với status=FAILED trước khi gửi.
     * - Nếu gửi thành công → cập nhật status=SUCCESS.
     * - Nếu lỗi → tăng retryCount, lưu errorMessage, KHÔNG ném exception.
     */
    void sendAsync(String to, String subject, String htmlBody);

    /**
     * Retry gửi lại email đã FAILED theo NotificationLog ID.
     * Chạy bất đồng bộ – ghi log activity RETRY_EMAIL.
     */
    void retryAsync(Long notificationLogId);

    // ─── Template helpers (từ Duc1) ──────────────────────────────────────────

    void sendDueSoonReminder(User user, BorrowRequest request);

    void sendOverdueWarning(User user, BorrowRequest request);

    void sendRequestApproved(User user, BorrowRequest request);

    void sendRequestRejected(User user, BorrowRequest request, String reason);
}
