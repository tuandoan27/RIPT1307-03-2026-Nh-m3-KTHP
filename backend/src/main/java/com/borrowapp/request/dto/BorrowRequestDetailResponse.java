package com.borrowapp.request.dto;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BorrowRequestDetailResponse {

    private Long id;
    private String studentName;
    private String studentCode;
    private String studentEmail;
    private Long equipmentId;
    private String equipmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RequestStatus status;
    private String note;
    private String reason;
    private Boolean penaltyApplied;
    private LocalDateTime createdAt;

    public static BorrowRequestDetailResponse fromEntity(BorrowRequest request) {
        return BorrowRequestDetailResponse.builder()
                .id(request.getId())
                .studentName(request.getUser().getFullName())
                .studentCode(request.getUser().getStudentCode())
                .studentEmail(request.getUser().getEmail())
                .equipmentId(request.getEquipment().getId())
                .equipmentName(request.getEquipment().getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .note(request.getNote())
                .reason(request.getReason())
                .penaltyApplied(request.getPenaltyApplied())
                .createdAt(request.getCreatedAt())
                .build();
    }
}