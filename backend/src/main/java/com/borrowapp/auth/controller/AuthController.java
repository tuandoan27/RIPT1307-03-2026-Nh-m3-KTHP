// com/borrowapp/auth/controller/AuthController.java
package com.borrowapp.auth.controller;

import com.borrowapp.auth.dto.*;
import com.borrowapp.auth.service.AuthService;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseUtil.created("Đăng ký thành công", authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseUtil.success("Đăng nhập thành công", authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMe(
            @AuthenticationPrincipal String email) {
        return ResponseUtil.success("Lấy thông tin thành công", authService.getMe(email));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal String email,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(email, request);
        return ResponseUtil.success("Đổi mật khẩu thành công");
    }

    @GetMapping("/hash")
    public String hash() {
        return passwordEncoder.encode("123456");
    }
}