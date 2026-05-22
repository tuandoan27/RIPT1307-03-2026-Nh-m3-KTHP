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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
}