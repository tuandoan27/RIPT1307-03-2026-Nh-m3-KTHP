package com.borrowapp.activity.controller;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService service;

    /**
     * GET /api/v1/activity-logs
     * Query params: userId, action, targetType, targetId, from, to, page, pageSize
     *
     * Response:
     * {
     *   "success": true,
     *   "message": "OK",
     *   "data": {
     *     "items": [...],
     *     "total": 100,
     *     "page": 0,
     *     "pageSize": 20
     *   }
     * }
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> getLogs(ActivityLogFilterRequest filter) {
        Page<ActivityLogResponse> page = service.getLogs(filter);
        return ResponseUtil.page(page);
    }

    // ❌ Không có POST/PUT/DELETE endpoint – log không được phép xóa hay sửa
}
