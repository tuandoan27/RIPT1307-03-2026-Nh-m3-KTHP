// com/borrowapp/admin/service/DashboardService.java
package com.borrowapp.admin.service;

import com.borrowapp.admin.dto.*;
import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository equipmentRepository;

    public DashboardResponse getDashboard() {
        int currentYear  = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        return DashboardResponse.builder()
                .totalBorrows(buildTotalBorrows())
                .currentBorrowing(borrowRequestRepository.countByStatus(RequestStatus.APPROVED))
                .overdue(borrowRequestRepository.countByStatus(RequestStatus.OVERDUE))
                .totalEquipment(equipmentRepository.countByIsDeletedFalse())
                .borrowsByMonth(buildBorrowsByMonth(currentYear))
                .statusRatio(buildStatusRatio())
                .top5Equipment(buildTop5Equipment(currentYear, currentMonth))
                .latest5Pending(buildLatest5Pending())
                .build();
    }

    private Long buildTotalBorrows() {
        return borrowRequestRepository.countByStatusIn(
                List.of(RequestStatus.APPROVED, RequestStatus.RETURNED, RequestStatus.OVERDUE)
        );
    }

    private List<MonthlyBorrowCount> buildBorrowsByMonth(int year) {
        Map<Integer, Long> byMonth = borrowRequestRepository.countGroupByMonth(year)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        return IntStream.rangeClosed(1, 12)
                .mapToObj(m -> MonthlyBorrowCount.builder()
                        .month(m)
                        .count(byMonth.getOrDefault(m, 0L))
                        .build())
                .toList();
    }

    private Map<RequestStatus, Long> buildStatusRatio() {
        return borrowRequestRepository.countGroupByStatus()
                .stream()
                .collect(Collectors.toMap(
                        row -> (RequestStatus) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

    private List<EquipmentBorrowCount> buildTop5Equipment(int year, int month) {
        return borrowRequestRepository
                .findTop5EquipmentByMonth(year, month, PageRequest.of(0, 5))
                .stream()
                .map(row -> EquipmentBorrowCount.builder()
                        .equipmentId(((Number) row[0]).longValue())
                        .equipmentName((String) row[1])
                        .count(((Number) row[2]).longValue())
                        .build())
                .toList();
    }

    private List<PendingRequestItem> buildLatest5Pending() {
        return borrowRequestRepository
                .findTop5ByStatusOrderByCreatedAtDesc(RequestStatus.PENDING)
                .stream()
                .map(r -> PendingRequestItem.builder()
                        .id(r.getId())
                        .studentName(r.getUser().getFullName())
                        .equipmentName(r.getEquipment().getName())
                        .startDate(r.getStartDate())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
    }
}