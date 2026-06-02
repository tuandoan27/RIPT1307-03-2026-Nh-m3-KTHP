package com.borrowapp.request.dto;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BorrowRequestListItemResponse {

    private Long id;
    private String studentName;
    private String studentCode;
    private String equipmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RequestStatus status;
    private LocalDateTime createdAt;

    public static BorrowRequestListItemResponse fromEntity(BorrowRequest request) {
        return BorrowRequestListItemResponse.builder()
                .id(request.getId())
                .studentName(request.getUser().getFullName())
                .studentCode(request.getUser().getStudentCode())
                .equipmentName(request.getEquipment().getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .build();
    }
}