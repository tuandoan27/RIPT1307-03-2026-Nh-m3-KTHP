// com/borrowapp/request/controller/BorrowRequestController.java
package com.borrowapp.request.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.request.dto.BorrowRequestResponse;
import com.borrowapp.request.dto.CreateBorrowRequestRequest;
import com.borrowapp.request.service.BorrowRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<BorrowRequestResponse>> createRequest(
            @Valid @RequestBody CreateBorrowRequestRequest request) {

        BorrowRequestResponse data = borrowRequestService.createRequest(request);
        return ResponseUtil.created("Tạo yêu cầu mượn thành công", data);
    }
}