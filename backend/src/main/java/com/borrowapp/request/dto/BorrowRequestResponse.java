// com/borrowapp/request/dto/BorrowRequestResponse.java
package com.borrowapp.request.dto;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BorrowRequestResponse {

    private Long id;
    private Long equipmentId;
    private String equipmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RequestStatus status;
    private String note;
    private LocalDateTime createdAt;

    public static BorrowRequestResponse fromEntity(BorrowRequest request) {
        return BorrowRequestResponse.builder()
                .id(request.getId())
                .equipmentId(request.getEquipment().getId())
                .equipmentName(request.getEquipment().getName())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(request.getStatus())
                .note(request.getNote())
                .createdAt(request.getCreatedAt())
                .build();
    }
}