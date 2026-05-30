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
 * Base path: /api/notifications
 *
 * Endpoints:
 *  - GET  /api/notifications                – danh sách thông báo có phân trang
 *  - PUT  /api/notifications/{id}/read      – đánh dấu 1 thông báo là đã đọc
 *  - PUT  /api/notifications/read-all       – đánh dấu tất cả là đã đọc
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService service;
    private final UserRepository       userRepository;

    /**
     * GET /api/notifications?page=1&pageSize=10
     *
     * Trả về danh sách notification của user hiện tại, kèm unreadCount và
     * thông tin phân trang.
     *
     * Lưu ý: page bắt đầu từ 1 để đồng nhất với các endpoint khác của project.
     * NotificationService dùng zero-based PageRequest nên cần trừ 1 khi gọi.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<NotificationBellResponse>> getMyNotifications(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        Long userId = getCurrentUserId();
        NotificationBellResponse data =
                service.getBell(userId, Math.max(page - 1, 0), pageSize);
        return ResponseUtil.success("", data);
    }

    /**
     * PUT /api/notifications/{id}/read
     * Đánh dấu một notification cụ thể là đã đọc.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        service.markAsRead(userId, id);
        return ResponseUtil.success("Đã đánh dấu thông báo là đã đọc");
    }

    /**
     * PUT /api/notifications/read-all
     * Đánh dấu tất cả notification của user hiện tại là đã đọc.
     */
    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        Long userId = getCurrentUserId();
        service.markAllAsRead(userId);
        return ResponseUtil.success("Đã đánh dấu tất cả thông báo là đã đọc");
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Trong project này, principal name = email (set bởi JwtAuthFilter).
     * Lookup user qua UserRepository để lấy ID.
     */
    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy người dùng"));
        return user.getId();
    }
}
