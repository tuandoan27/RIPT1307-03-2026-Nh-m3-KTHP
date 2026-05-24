package com.borrowapp.notification.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin API – quản lý email thất bại và retry.
 *
 * Base path: /api/v1/admin/notifications
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationAdminController {

    private final NotificationService service;

    /**
     * GET /api/v1/admin/notifications/failed-emails?page=0&pageSize=20
     *
     * Xem danh sách email gửi thất bại có phân trang.
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "OK",
     *   "data": {
     *     "items": [...],
     *     "total": 5,
     *     "page": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping("/failed-emails")
    public ApiResponse<Object> getFailedEmails(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<NotificationLogResponse> result =
                service.getFailedLogs(page, pageSize);
        return ResponseUtil.page(result);
    }

    /**
     * POST /api/v1/admin/notifications/retry-email/{id}
     *
     * Retry gửi lại 1 email cụ thể theo NotificationLog ID.
     * Chạy bất đồng bộ – response trả về ngay lập tức.
     */
    @PostMapping("/retry-email/{id}")
    public ApiResponse<Void> retryEmail(@PathVariable Long id) {
        service.retryEmail(id);
        return ResponseUtil.ok(null, "Email retry queued successfully");
    }

    /**
     * POST /api/v1/admin/notifications/retry-all-failed
     *
     * Retry toàn bộ email FAILED (lấy tối đa 100 bản ghi một lần).
     * Scheduler tự động retry mỗi 15 phút, endpoint này dành cho retry thủ công.
     */
    @PostMapping("/retry-all-failed")
    public ApiResponse<Object> retryAllFailed() {
        Page<NotificationLogResponse> failedPage =
                service.getFailedLogs(0, 100);

        failedPage.getContent()
                .forEach(logResp -> service.retryEmail(logResp.getId()));

        return ResponseUtil.ok(
                java.util.Map.of("queued", failedPage.getTotalElements()),
                failedPage.getTotalElements() + " email(s) queued for retry"
        );
    }
}
