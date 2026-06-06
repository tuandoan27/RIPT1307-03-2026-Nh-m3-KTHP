package com.borrowapp.notification.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class NotificationAdminController {

    private final NotificationService service;

    // ─── Endpoint cũ — giữ nguyên ────────────────────────────────────────────

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
        return ResponseUtil.success("Email retry queued successfully");
    }

    @PostMapping("/retry-all-failed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryAllFailed() {
        Page<NotificationLogResponse> failedPage = service.getFailedLogs(0, 100);
        failedPage.getContent().forEach(l -> service.retryEmail(l.getId()));
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