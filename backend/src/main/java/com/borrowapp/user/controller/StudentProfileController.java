// com/borrowapp/user/controller/StudentProfileController.java
package com.borrowapp.user.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.user.dto.ChangePasswordRequest;
import com.borrowapp.user.dto.UserProfileResponse;
import com.borrowapp.user.dto.PenaltyResponse;
import com.borrowapp.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class StudentProfileController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        return ResponseUtil.success("", userService.getMyProfile());
    }

    @GetMapping("/me/penalties")
    public ResponseEntity<ApiResponse<List<PenaltyResponse>>> getMyPenalties() {
        return ResponseUtil.success("", userService.getMyPenalties());
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseUtil.success("Đổi mật khẩu thành công");
    }
}