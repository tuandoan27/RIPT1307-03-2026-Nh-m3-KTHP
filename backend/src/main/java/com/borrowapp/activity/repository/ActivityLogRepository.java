package com.borrowapp.activity.repository;

import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.common.constants.ActivityLogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);

    @Query("SELECT a FROM ActivityLog a " +
           "WHERE (:userId IS NULL OR a.userId = :userId) " +
           "AND (:action IS NULL OR a.action = :action) " +
           "AND (:targetType IS NULL OR a.targetType = :targetType) " +
           "AND (:targetId IS NULL OR a.targetId = :targetId) " +
           "AND (:from IS NULL OR a.createdAt >= :from) " +
           "AND (:to IS NULL OR a.createdAt <= :to) " +
           "ORDER BY a.createdAt DESC")
    Page<ActivityLog> findWithFilters(
            @Param("userId") Long userId,
            @Param("action") ActivityLogAction action,
            @Param("targetType") String targetType,
            @Param("targetId") Long targetId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );
}