package com.borrowapp.user.controller;

import com.borrowapp.common.constants.Role;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.user.dto.AdjustPenaltyRequest;
import com.borrowapp.user.dto.UserDetailResponse;
import com.borrowapp.user.dto.UserListItemResponse;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import com.borrowapp.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService    userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<UserListItemResponse>>> getUsers(
            @RequestParam(required = false)    String search,
            @RequestParam(required = false)    Role role,
            @RequestParam(required = false)    Boolean isLocked,
            @RequestParam(defaultValue = "1")  int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ResponseUtil.success("", userService.getUsers(search, role, isLocked, page, pageSize));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserDetail(@PathVariable Long id) {
        return ResponseUtil.success("", userService.getUserDetail(id));
    }

    @PutMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        userService.lockUser(id);
        return ResponseUtil.success("Khóa tài khoản thành công.");
    }

    @PutMapping("/{id}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ResponseUtil.success("Mở khóa tài khoản thành công.");
    }

    @PutMapping("/{id}/reset-penalty")
    public ResponseEntity<ApiResponse<Void>> resetPenalty(@PathVariable Long id) {
        userService.resetPenalty(id);
        return ResponseUtil.success("Reset điểm phạt thành công.");
    }

    // ─── Mới: Adjust penalty ──────────────────────────────────────────────────

    @PutMapping("/{id}/adjust-penalty")
    public ResponseEntity<ApiResponse<UserDetailResponse>> adjustPenalty(
            @PathVariable Long id,
            @Valid @RequestBody AdjustPenaltyRequest request) {
        User admin = currentAdmin();
        return ResponseUtil.success("Điều chỉnh điểm phạt thành công",
                userService.adjustPenalty(id, request, admin.getId(), admin.getFullName()));
    }

    private User currentAdmin() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại: " + email));
    }
}