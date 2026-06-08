package com.borrowapp.equipment.controller;

import com.borrowapp.common.response.ApiResponse;
import com.borrowapp.common.response.ResponseUtil;
import com.borrowapp.equipment.dto.*;
import com.borrowapp.equipment.service.EquipmentService;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;
    private final UserRepository   userRepository;

    // ─── Public / Student (giữ nguyên) ───────────────────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<EquipmentListResponse>> getEquipmentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize,
            @RequestParam(required = false)   String keyword) {
        return ResponseUtil.success("Lấy danh sách thiết bị thành công",
                equipmentService.getEquipmentList(page, pageSize, keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> getEquipmentById(
            @PathVariable Long id) {
        return ResponseUtil.success("Lấy chi tiết thiết bị thành công",
                equipmentService.getEquipmentById(id));
    }

    @GetMapping("/{id}/overlap")
    public ResponseEntity<ApiResponse<OverlapCheckResponse>> checkOverlap(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseUtil.success("Kiểm tra khả dụng thành công",
                equipmentService.checkOverlap(id, start, end));
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<List<BookingSlotResponse>>> getBookingSlots(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseUtil.success("", equipmentService.getBookingSlots(id, start, end));
    }

    // ─── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> createEquipment(
            @Valid @RequestBody EquipmentRequest request) {
        User admin = currentAdmin();
        return ResponseUtil.created("Tạo thiết bị thành công",
                equipmentService.createEquipment(request, admin.getId(), admin.getFullName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> updateEquipment(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {
        User admin = currentAdmin();
        return ResponseUtil.success("Cập nhật thiết bị thành công",
                equipmentService.updateEquipment(id, request, admin.getId(), admin.getFullName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEquipment(@PathVariable Long id) {
        User admin = currentAdmin();
        equipmentService.deleteEquipment(id, admin.getId(), admin.getFullName());
        return ResponseUtil.success("Xóa thiết bị thành công");
    }

    @PutMapping("/{id}/stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<EquipmentDetailResponse>> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStockRequest request) {
        User admin = currentAdmin();
        return ResponseUtil.success("Cập nhật kho thành công",
                equipmentService.updateStock(id, request, admin.getId(), admin.getFullName()));
    }

    // ─── Private helper ───────────────────────────────────────────────────────

    /**
     * Lấy User hiện tại từ SecurityContext.
     * JwtAuthFilter set principal = email (String), không phải UserDetails object.
     */
    private User currentAdmin() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại: " + email));
    }
    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<java.util.List<com.borrowapp.request.dto.BorrowRequestResponse>>> getBookings(
            @PathVariable Long id) {
        return ResponseUtil.success("Lấy danh sách đặt lịch thành công", equipmentService.getBookings(id));
    }
}