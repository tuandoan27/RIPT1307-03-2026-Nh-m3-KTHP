package com.borrowapp.notification.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API – quản lý email thất bại và retry.
 *
 * Base path: /api/admin/notifications
 */
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationAdminController {

    private final NotificationService service;

    /**
     * GET /api/admin/notifications/failed-emails?page=1&pageSize=20
     * Xem danh sách email gửi thất bại có phân trang.
     */
    @GetMapping("/failed-emails")
    public ResponseEntity<ApiResponse<PageResponse<NotificationLogResponse>>> getFailedEmails(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<NotificationLogResponse> result =
                service.getFailedLogs(Math.max(page - 1, 0), pageSize);

        PageResponse<NotificationLogResponse> data = PageResponse.<NotificationLogResponse>builder()
                .items(result.getContent())
                .total(result.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
        return ResponseUtil.success("", data);
    }

    /**
     * POST /api/admin/notifications/retry-email/{id}
     * Retry gửi lại 1 email cụ thể.
     */
    @PostMapping("/retry-email/{id}")
    public ResponseEntity<ApiResponse<Void>> retryEmail(@PathVariable Long id) {
        service.retryEmail(id);
        return ResponseUtil.success("Đã đưa email vào hàng đợi retry");
    }

    /**
     * POST /api/admin/notifications/retry-all-failed
     * Retry toàn bộ email FAILED (tối đa 100 bản ghi mỗi lần gọi).
     */
    @PostMapping("/retry-all-failed")
    public ResponseEntity<ApiResponse<Map<String, Long>>> retryAllFailed() {
        Page<NotificationLogResponse> failedPage = service.getFailedLogs(0, 100);
        failedPage.getContent().forEach(l -> service.retryEmail(l.getId()));

        return ResponseUtil.success(
                "Đã đưa " + failedPage.getTotalElements() + " email vào hàng đợi retry",
                Map.of("queued", failedPage.getTotalElements())
        );
    }
}
