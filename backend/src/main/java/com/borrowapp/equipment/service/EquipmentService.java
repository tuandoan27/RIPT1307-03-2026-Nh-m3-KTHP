package com.borrowapp.equipment.service;

import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.equipment.dto.*;
import com.borrowapp.equipment.entity.Equipment;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.dto.BorrowRequestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository        equipmentRepository;
    private final BorrowRequestRepository    borrowRequestRepository;
    private final ActivityLogService         activityLogService;

    // ─── Read (giữ nguyên) ────────────────────────────────────────────────────

    public EquipmentListResponse getEquipmentList(int page, int pageSize, String keyword) {
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<EquipmentListItemResponse> resultPage;
        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (hasKeyword) {
            resultPage = equipmentRepository
                    .findByIsDeletedFalseAndNameContainingIgnoreCase(keyword.trim(), pageable)
                    .map(EquipmentListItemResponse::fromEntity);
        } else {
            resultPage = equipmentRepository
                    .findByIsDeletedFalse(pageable)
                    .map(EquipmentListItemResponse::fromEntity);
        }

        return EquipmentListResponse.builder()
                .items(resultPage.getContent())
                .total(resultPage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    public EquipmentDetailResponse getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị với id: " + id));
        return EquipmentDetailResponse.fromEntity(equipment);
    }

    public OverlapCheckResponse checkOverlap(Long equipmentId, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .filter(e -> !e.getIsDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));
        Long approvedCount = borrowRequestRepository.countApprovedOverlap(
                equipmentId, RequestStatus.APPROVED, start, end);
        return OverlapCheckResponse.of(equipmentId, approvedCount, equipment.getTotalQuantity());
    }

    public List<BookingSlotResponse> getBookingSlots(Long equipmentId, LocalDate start, LocalDate end) {
        if (start.isAfter(end)) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }
        equipmentRepository.findByIdAndIsDeletedFalse(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị với id: " + equipmentId));
        List<BorrowRequest> approvedRequests = borrowRequestRepository.findApprovedOverlappingBookings(
                equipmentId, RequestStatus.APPROVED, start, end);
        return approvedRequests.stream()
                .map(r -> BookingSlotResponse.builder()
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .quantity(maxConcurrentOn(r, approvedRequests))
                        .build())
                .toList();
    }

    public List<BorrowRequestResponse> getBookings(Long equipmentId) {
        equipmentRepository.findByIdAndIsDeletedFalse(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thiết bị"));

        return borrowRequestRepository.findByEquipmentIdAndStatus(equipmentId, RequestStatus.APPROVED)
                .stream()
                .map(BorrowRequestResponse::fromEntity)
                .toList();
    }

    // ─── Admin CRUD ───────────────────────────────────────────────────────────

    @Transactional
    public EquipmentDetailResponse createEquipment(EquipmentRequest request,
                                                    Long adminId, String adminName) {
        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .totalQuantity(request.getTotalQuantity())
                .availableQuantity(request.getTotalQuantity()) // available = total khi tạo mới
                .imageUrl(request.getImageUrl())
                .category(request.getCategory())
                .build();

        equipment = equipmentRepository.save(equipment);

        activityLogService.log(adminId, adminName,
                ActivityLogAction.CREATE_DEVICE, "EQUIPMENT", equipment.getId(),
                String.format("Tạo thiết bị: %s, số lượng: %d",
                        equipment.getName(), equipment.getTotalQuantity()));

        return EquipmentDetailResponse.fromEntity(equipment);
    }

    @Transactional
    public EquipmentDetailResponse updateEquipment(Long id, EquipmentRequest request,
                                                    Long adminId, String adminName) {
        Equipment equipment = validateEquipment(id);

        // Nếu đổi totalQuantity: tính delta và cập nhật availableQuantity
        if (!equipment.getTotalQuantity().equals(request.getTotalQuantity())) {
            updateStockWithDelta(equipment, request.getTotalQuantity(),
                    String.format("Cập nhật số lượng từ PUT /equipment/%d", id));
        }

        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        equipment.setImageUrl(request.getImageUrl());
        equipment.setCategory(request.getCategory());
        equipment = equipmentRepository.save(equipment);

        activityLogService.log(adminId, adminName,
                ActivityLogAction.UPDATE_DEVICE, "EQUIPMENT", equipment.getId(),
                String.format("Cập nhật thiết bị: %s", equipment.getName()));

        return EquipmentDetailResponse.fromEntity(equipment);
    }

    @Transactional
    public void deleteEquipment(Long id, Long adminId, String adminName) {
        Equipment equipment = validateEquipment(id);

        // Kiểm tra còn request APPROVED hay không
        Long activeApproved = borrowRequestRepository
                .countByEquipmentIdAndStatus(id, RequestStatus.APPROVED);
        if (activeApproved > 0) {
            throw new BadRequestException(
                    "Không thể xóa thiết bị đang có " + activeApproved + " yêu cầu APPROVED còn hiệu lực");
        }

        equipment.setIsDeleted(true);
        equipmentRepository.save(equipment);

        activityLogService.log(adminId, adminName,
                ActivityLogAction.DELETE_DEVICE, "EQUIPMENT", id,
                String.format("Xóa thiết bị: %s", equipment.getName()));
    }

    @Transactional
    public EquipmentDetailResponse updateStock(Long id, UpdateStockRequest request,
                                                Long adminId, String adminName) {
        Equipment equipment = validateEquipment(id);
        updateStockWithDelta(equipment, request.getNewQuantity(), request.getReason());
        equipment = equipmentRepository.save(equipment);

        activityLogService.log(adminId, adminName,
                ActivityLogAction.UPDATE_STOCK, "EQUIPMENT", id,
                String.format("Cập nhật kho thiết bị '%s': số lượng mới=%d, lý do=%s",
                        equipment.getName(), request.getNewQuantity(), request.getReason()));

        return EquipmentDetailResponse.fromEntity(equipment);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Equipment validateEquipment(Long id) {
        return equipmentRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị với id: " + id));
    }

    private void updateStockWithDelta(Equipment equipment, int newTotal, String reason) {
        int oldTotal = equipment.getTotalQuantity();
        int oldAvailable = equipment.getAvailableQuantity();
        int delta = newTotal - oldTotal;
        int newAvailable = oldAvailable + delta;

        // Số lượng đang APPROVED (đã trừ khỏi available)
        long approvedCount = oldTotal - oldAvailable;

        if (newTotal < approvedCount) {
            throw new BadRequestException(
                    String.format("Không thể giảm số lượng xuống %d — hiện có %d yêu cầu APPROVED",
                            newTotal, (long) approvedCount));
        }

        if (newAvailable < 0) {
            throw new BadRequestException("Số lượng khả dụng sau khi cập nhật không được âm");
        }

        equipment.setTotalQuantity(newTotal);
        equipment.setAvailableQuantity(newAvailable);
    }

    private long maxConcurrentOn(BorrowRequest target, List<BorrowRequest> allRequests) {
        return target.getStartDate()
                .datesUntil(target.getEndDate().plusDays(1))
                .mapToLong(day -> allRequests.stream()
                        .filter(r -> !r.getStartDate().isAfter(day)
                                  && !r.getEndDate().isBefore(day))
                        .count())
                .max()
                .orElse(1L);
    }
}