// com/borrowapp/equipment/controller/EquipmentController.java
package com.borrowapp.equipment.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.equipment.dto.EquipmentDetailResponse;
import com.borrowapp.equipment.dto.OverlapCheckResponse;
import com.borrowapp.equipment.dto.EquipmentListResponse;
import com.borrowapp.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<EquipmentListResponse>> getEquipmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(required = false) String keyword) {

        EquipmentListResponse data = equipmentService.getEquipmentList(page, pageSize, keyword);
        return ResponseUtil.success("Lấy danh sách thiết bị thành công", data);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> getEquipmentById(
            @PathVariable Long id) {
        EquipmentDetailResponse data = equipmentService.getEquipmentById(id);
        return ResponseUtil.success("Lấy chi tiết thiết bị thành công", data);
    }
    @GetMapping("/{id}/overlap")
    public ResponseEntity<ApiResponse<OverlapCheckResponse>> checkOverlap(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        OverlapCheckResponse data = equipmentService.checkOverlap(id, start, end);
        return ResponseUtil.success("Kiểm tra khả dụng thành công", data);
    }
    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<java.util.List<com.borrowapp.request.dto.BorrowRequestResponse>>> getBookings(
            @PathVariable Long id) {
        return ResponseUtil.success("Lấy danh sách đặt lịch thành công", equipmentService.getBookings(id));
    }
}