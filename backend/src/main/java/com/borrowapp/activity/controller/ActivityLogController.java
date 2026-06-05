// com/borrowapp/activity/controller/ActivityLogController.java
package com.borrowapp.activity.controller;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API – xem activity logs.
 * GET /api/activity-logs
 * Query params: userId, action, targetType, targetId, from, to, page, pageSize
 *
 * Log entries chỉ đọc – không có POST/PUT/DELETE.
 */
@RestController
@RequestMapping("/api/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ActivityLogResponse>>> getLogs(
            ActivityLogFilterRequest filter) {

        Page<ActivityLogResponse> page = service.getLogs(filter);
        PageResponse<ActivityLogResponse> data = PageResponse.<ActivityLogResponse>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .build();
        return ResponseUtil.success("", data);
    }
}
