// com/borrowapp/equipment/service/EquipmentService.java
package com.borrowapp.equipment.service;

import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.equipment.dto.OverlapCheckResponse;
import com.borrowapp.equipment.dto.EquipmentDetailResponse;     
import com.borrowapp.equipment.dto.EquipmentListItemResponse;
import com.borrowapp.equipment.dto.EquipmentListResponse;
import com.borrowapp.equipment.entity.Equipment; 
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.request.repository.BorrowRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final BorrowRequestRepository borrowRequestRepository;

    public EquipmentListResponse getEquipmentList(int page, int pageSize, String keyword) {

        // FE truyền page từ 1, Pageable cần 0-based
        Pageable pageable = PageRequest.of(
                page - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

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
                equipmentId,
                RequestStatus.APPROVED,
                start,
                end
        );
 
        return OverlapCheckResponse.of(equipmentId, approvedCount, equipment.getTotalQuantity());
    }
}