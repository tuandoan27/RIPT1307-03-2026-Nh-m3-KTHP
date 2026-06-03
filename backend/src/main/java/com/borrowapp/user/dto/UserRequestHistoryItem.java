package com.borrowapp.user.dto;

import com.borrowapp.common.constants.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestHistoryItem {
    private Long id;
    private String equipmentName;
    private LocalDate startDate;
    private LocalDate endDate;
    private RequestStatus status;
    private LocalDateTime createdAt;
}