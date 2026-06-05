// com/borrowapp/admin/dto/DashboardResponse.java
package com.borrowapp.admin.dto;

import com.borrowapp.common.constants.RequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class DashboardResponse {
    private Long totalBorrows;
    private Long currentBorrowing;
    private Long overdue;
    private Long totalEquipment;
    private List<MonthlyBorrowCount> borrowsByMonth;
    private Map<RequestStatus, Long> statusRatio;
    private List<EquipmentBorrowCount> top5Equipment;
    private List<PendingRequestItem> latest5Pending;
}