// com/borrowapp/admin/dto/PendingRequestItem.java
package com.borrowapp.admin.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class PendingRequestItem {
    private Long id;
    private String studentName;
    private String equipmentName;
    private LocalDate startDate;
    private LocalDateTime createdAt;
}