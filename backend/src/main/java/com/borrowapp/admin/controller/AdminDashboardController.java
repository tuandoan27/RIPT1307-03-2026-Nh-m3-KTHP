// com/borrowapp/admin/controller/AdminDashboardController.java
package com.borrowapp.admin.controller;

import com.borrowapp.admin.dto.DashboardResponse;
import com.borrowapp.admin.service.DashboardService;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseUtil.success("", dashboardService.getDashboard());
    }
}