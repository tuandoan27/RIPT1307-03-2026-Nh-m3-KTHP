// com/borrowapp/notification/controller/NotificationController.java
package com.borrowapp.notification.controller;

import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.service.NotificationService;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Notification API – dành cho người dùng đã đăng nhập.
 *
 * Endpoints (khớp spec):
 *   GET /api/notifications              – danh sách + unreadCount + phân trang
 *   PUT /api/notifications/{id}/read    – đánh dấu 1 thông báo đã đọc
 *   PUT /api/notifications/read-all     – đánh dấu tất cả đã đọc
 *
 * Principal trong project Tuấn là email (JwtAuthFilter set principal = email),
 * nên controller resolve userId qua UserRepository.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService service;
    private final UserRepository      userRepository;

    /**
     * GET /api/notifications?page=0&pageSize=10
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationBellResponse>> getNotifications(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long currentUserId = currentUserId();
        NotificationBellResponse data = service.getBell(currentUserId, page, pageSize);
        return ResponseUtil.success("", data);
    }

    /**
     * PUT /api/notifications/{id}/read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        service.markAsRead(currentUserId(), id);
        return ResponseUtil.success("Đã đánh dấu thông báo là đã đọc");
    }

    /**
     * PUT /api/notifications/read-all
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        service.markAllAsRead(currentUserId());
        return ResponseUtil.success("Đã đánh dấu tất cả thông báo là đã đọc");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private Long currentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return u.getId();
    }
}
