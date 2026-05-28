// com/borrowapp/request/controller/BorrowRequestController.java
package com.borrowapp.request.controller;

import com.borrowapp.request.dto.BorrowRequestListItemResponse;
import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.request.dto.BorrowRequestResponse;
import com.borrowapp.request.dto.BorrowRequestDetailResponse;
import com.borrowapp.request.dto.CreateBorrowRequestRequest;
import com.borrowapp.request.dto.RejectRequestBody;
import com.borrowapp.request.service.BorrowRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.borrowapp.common.constants.RequestStatus;


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

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<BorrowRequestListItemResponse>>> getMyRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) RequestStatus status
    ) {
        PageResponse<BorrowRequestListItemResponse> data = borrowRequestService.getMyRequests(page, pageSize, status);
        return ResponseUtil.success("", data);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRequest(@PathVariable Long id) {
        borrowRequestService.approveRequest(id);
        return ResponseUtil.success("Duyệt yêu cầu thành công");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequestBody body
    ) {
        borrowRequestService.rejectRequest(id, body);
        return ResponseUtil.success("Từ chối yêu cầu thành công");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<Void>> returnRequest(@PathVariable Long id) {
        borrowRequestService.returnRequest(id);
        return ResponseUtil.success("Xác nhận trả thiết bị thành công");
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public ResponseEntity<ApiResponse<BorrowRequestDetailResponse>> getRequestById(@PathVariable Long id) {
        BorrowRequestDetailResponse data = borrowRequestService.getRequestById(id);
        return ResponseUtil.success("", data);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<BorrowRequestListItemResponse>>> getAllRequests(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) RequestStatus status,
        @RequestParam(required = false) String keyword
    ) {
    PageResponse<BorrowRequestListItemResponse> data =
            borrowRequestService.getAllRequests(page, pageSize, status, keyword);
    return ResponseUtil.success("", data);
    }
}