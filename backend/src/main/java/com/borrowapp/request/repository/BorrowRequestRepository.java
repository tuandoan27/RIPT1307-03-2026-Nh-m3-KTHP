package com.borrowapp.request.repository;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    long countByEquipmentIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long equipmentId,
            RequestStatus status,
            LocalDate endDate,
            LocalDate startDate
    );

    @Query("""
            SELECT COUNT(r) FROM BorrowRequest r
            WHERE r.equipment.id = :equipmentId
              AND r.status = :status
              AND r.startDate <= :end
              AND r.endDate >= :start
            """)
    Long countApprovedOverlap(
            @Param("equipmentId") Long equipmentId,
            @Param("status") RequestStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    Page<BorrowRequest> findByUserIdAndStatus(Long userId, RequestStatus status, Pageable pageable);

    Page<BorrowRequest> findByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT r FROM BorrowRequest r
            JOIN FETCH r.user u
            JOIN FETCH r.equipment e
            WHERE (:status IS NULL OR r.status = :status)
              AND (:keyword IS NULL
                   OR u.fullName LIKE CONCAT('%', CAST(:keyword AS string), '%')
                   OR e.name LIKE CONCAT('%', CAST(:keyword AS string), '%'))
            ORDER BY r.createdAt DESC
            """)
    Page<BorrowRequest> findAllWithFilter(
            @Param("status") RequestStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Dùng cho cron job 00:00 — quét request quá hạn
    @Query("""
            SELECT r FROM BorrowRequest r
            JOIN FETCH r.user u
            WHERE r.status = :status
              AND r.endDate < :today
            """)
    List<BorrowRequest> findOverdueRequests(
            @Param("status") RequestStatus status,
            @Param("today") LocalDate today
    );

    // Dùng cho cron job 08:00 — nhắc hạn trả
    @Query("""
            SELECT r FROM BorrowRequest r
            JOIN FETCH r.user u
            JOIN FETCH r.equipment e
            WHERE r.status = :status
              AND r.endDate IN (:today, :tomorrow)
            """)
    List<BorrowRequest> findDueSoonRequests(
            @Param("status") RequestStatus status,
            @Param("today") LocalDate today,
            @Param("tomorrow") LocalDate tomorrow
    );
}