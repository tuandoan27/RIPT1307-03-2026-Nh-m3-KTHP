// com/borrowapp/request/repository/BorrowRequestRepository.java
package com.borrowapp.request.repository;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    // Đếm overlap: request APPROVED có khoảng ngày giao nhau với [startDate, endDate]
    // Điều kiện giao nhau: startDate <= endDate_mới AND endDate >= startDate_mới
    long countByEquipmentIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long equipmentId,
            RequestStatus status,
            LocalDate endDate,
            LocalDate startDate
    );
}