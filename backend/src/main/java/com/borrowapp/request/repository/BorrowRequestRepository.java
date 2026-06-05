// com/borrowapp/request/repository/BorrowRequestRepository.java
package com.borrowapp.request.repository;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    // ── Overlap check (tạo request) ───────────────────────────────────────
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

    // ── Lấy danh sách request ─────────────────────────────────────────────
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

    // ── Cron job 00:00 — quét request quá hạn ────────────────────────────
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

    // ── Cron job 08:00 — nhắc hạn trả ────────────────────────────────────
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

    // ── Booking slots API ─────────────────────────────────────────────────
    @Query("""
            SELECT r FROM BorrowRequest r
            WHERE r.equipment.id = :equipmentId
              AND r.status = :status
              AND r.startDate <= :end
              AND r.endDate >= :start
            ORDER BY r.startDate ASC
            """)
    List<BorrowRequest> findApprovedOverlappingBookings(
            @Param("equipmentId") Long equipmentId,
            @Param("status") RequestStatus status,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // ── Admin Dashboard ───────────────────────────────────────────────────
    Long countByStatusIn(Collection<RequestStatus> statuses);

    Long countByStatus(RequestStatus status);

    @Query("""
            SELECT month(r.createdAt), COUNT(r)
            FROM BorrowRequest r
            WHERE year(r.createdAt) = :year
            GROUP BY month(r.createdAt)
            ORDER BY month(r.createdAt) ASC
            """)
    List<Object[]> countGroupByMonth(@Param("year") int year);

    @Query("SELECT r.status, COUNT(r) FROM BorrowRequest r GROUP BY r.status")
    List<Object[]> countGroupByStatus();

    @Query("""
            SELECT r.equipment.id, r.equipment.name, COUNT(r)
            FROM BorrowRequest r
            WHERE year(r.createdAt) = :year AND month(r.createdAt) = :month
            GROUP BY r.equipment.id, r.equipment.name
            ORDER BY COUNT(r) DESC
            """)
    List<Object[]> findTop5EquipmentByMonth(
            @Param("year") int year,
            @Param("month") int month,
            Pageable pageable
    );

    List<BorrowRequest> findTop5ByStatusOrderByCreatedAtDesc(RequestStatus status);
    Long countByEquipmentIdAndStatus(Long equipmentId, RequestStatus status);
}