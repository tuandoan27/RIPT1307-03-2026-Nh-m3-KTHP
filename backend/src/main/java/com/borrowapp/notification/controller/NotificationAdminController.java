package com.borrowapp.notification.controller;

import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.service.NotificationService;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationAdminController {

    private final NotificationService  service;
    private final ActivityLogService   activityLogService;   // ← thêm
    private final UserRepository       userRepository;       // ← thêm

    @GetMapping("/failed-emails")
    public ResponseEntity<ApiResponse<PageResponse<NotificationLogResponse>>> getFailedEmails(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Page<NotificationLogResponse> result = service.getFailedLogs(page, pageSize);
        PageResponse<NotificationLogResponse> data = PageResponse.<NotificationLogResponse>builder()
                .items(result.getContent())
                .total(result.getTotalElements())
                .page(result.getNumber())
                .pageSize(result.getSize())
                .build();
        return ResponseUtil.success("", data);
    }

    @PostMapping("/retry-email/{id}")
    public ResponseEntity<ApiResponse<Void>> retryEmail(@PathVariable Long id) {
        service.retryEmail(id);

        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User admin   = userRepository.findByEmail(email).orElse(null);
            activityLogService.log(
                    admin != null ? admin.getId()       : null,
                    admin != null ? admin.getFullName() : "ADMIN",
                    ActivityLogAction.MANUAL_SEND_EMAIL,
                    "NOTIFICATION_LOG", id,
                    "Retry email thủ công"
            );
        } catch (Exception ex) {
            log.error("[RetryEmail] Log failed | logId={} err={}", id, ex.getMessage());
        }

        return ResponseUtil.success("Email retry queued successfully");
    }

    @PostMapping("/retry-all-failed")
public ResponseEntity<ApiResponse<Map<String, Object>>> retryAllFailed() {
    Page<NotificationLogResponse> failedPage = service.getFailedLogs(0, 100);
    failedPage.getContent().forEach(l -> service.retryEmail(l.getId()));

    try {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin   = userRepository.findByEmail(email).orElse(null);
        activityLogService.log(
                admin != null ? admin.getId()       : null,
                admin != null ? admin.getFullName() : "ADMIN",
                ActivityLogAction.MANUAL_SEND_EMAIL,
                "NOTIFICATION_LOG", null,
                "Gửi lại tất cả email thất bại, đã xếp hàng: " + failedPage.getTotalElements() + " email"
        );
    } catch (Exception ex) {
        log.error("[RetryAllFailed] Log failed | err={}", ex.getMessage());
    }

    return ResponseUtil.success(
            failedPage.getTotalElements() + " email(s) queued for retry",
            Map.of("queued", failedPage.getTotalElements())
    );
}

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<PageResponse<NotificationLogResponse>>> getNotificationLogs(
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false)    NotificationLogStatus status
    ) {
        PageResponse<NotificationLogResponse> result =
                service.getNotificationLogs(page, pageSize, status);
        return ResponseUtil.success("", result);
    }
}