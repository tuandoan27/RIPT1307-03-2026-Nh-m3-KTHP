// com/borrowapp/equipment/controller/EquipmentController.java
package com.borrowapp.equipment.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.equipment.dto.EquipmentListResponse;
import com.borrowapp.equipment.service.EquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // Thêm vào EquipmentController.java — KHÔNG tạo file mới
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> getEquipmentById(
            @PathVariable Long id) {
        EquipmentDetailResponse data = equipmentService.getEquipmentById(id);
        return ResponseUtil.success("Lấy chi tiết thiết bị thành công", data);
    }
}