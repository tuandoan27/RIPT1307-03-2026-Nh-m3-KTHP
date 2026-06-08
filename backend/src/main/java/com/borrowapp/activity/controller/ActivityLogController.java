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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin API – xem activity logs.
 * GET /api/admin/activity-logs
 * Query params: userId, action, targetType, targetId, from, to, page, pageSize
 *
 * Log entries chỉ đọc – không có POST/PUT/DELETE.
 */
@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ActivityLogResponse>>> getActivityLogs(
            @ModelAttribute ActivityLogFilterRequest filter
    ) {
        Page<ActivityLogResponse> page = activityLogService.getLogs(filter);
        PageResponse<ActivityLogResponse> data = PageResponse.<ActivityLogResponse>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .page(page.getNumber())
                .pageSize(page.getSize())
                .build();
        return ResponseUtil.success("", data);
    }
}
