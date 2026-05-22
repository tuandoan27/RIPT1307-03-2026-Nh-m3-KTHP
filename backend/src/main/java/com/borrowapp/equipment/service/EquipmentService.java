// com/borrowapp/equipment/service/EquipmentService.java
package com.borrowapp.equipment.service;

import com.borrowapp.equipment.dto.EquipmentListItemResponse;
import com.borrowapp.equipment.dto.EquipmentListResponse;
import com.borrowapp.equipment.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;

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
}