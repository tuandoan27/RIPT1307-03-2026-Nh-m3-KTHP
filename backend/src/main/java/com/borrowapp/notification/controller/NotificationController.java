package com.borrowapp.notification.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Notification Bell API – dành cho người dùng đã đăng nhập.
 *
 * Base path: /api/v1/notifications
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService service;

    /**
     * GET /api/v1/notifications/bell?page=0&pageSize=10
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "OK",
     *   "data": {
     *     "unreadCount": 3,
     *     "items": [...],
     *     "total": 25,
     *     "page": 0,
     *     "pageSize": 10
     *   }
     * }
     */
    @GetMapping("/bell")
    public ApiResponse<NotificationBellResponse> getBell(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        NotificationBellResponse data =
                service.getBell(currentUserId, page, pageSize);
        return ResponseUtil.ok(data);
    }

    /**
     * PATCH /api/v1/notifications/{id}/read
     * Đánh dấu 1 notification là đã đọc.
     */
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(
            @AuthenticationPrincipal(expression = "id") Long currentUserId,
            @PathVariable Long id) {

        service.markAsRead(currentUserId, id);
        return ResponseUtil.ok(null, "Marked as read");
    }

    /**
     * PATCH /api/v1/notifications/read-all
     * Đánh dấu tất cả notification của user hiện tại là đã đọc.
     */
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(
            @AuthenticationPrincipal(expression = "id") Long currentUserId) {

        service.markAllAsRead(currentUserId);
        return ResponseUtil.ok(null, "All notifications marked as read");
    }
}
