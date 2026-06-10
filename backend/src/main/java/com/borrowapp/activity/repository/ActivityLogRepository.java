package com.borrowapp.activity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.common.constants.ActivityLogAction;

// BUG FIX: thêm JpaSpecificationExecutor<ActivityLog>
// để ActivityLogServiceImpl có thể gọi repo.findAll(spec, pageable)
public interface ActivityLogRepository
        extends JpaRepository<ActivityLog, Long>,
                JpaSpecificationExecutor<ActivityLog> {

    List<ActivityLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);

    @Query("SELECT a FROM ActivityLog a " +
       "WHERE (cast(:userId as long) IS NULL OR a.userId = :userId) " +
       "AND (:action IS NULL OR a.action = :action) " +
       "AND (:targetType IS NULL OR a.targetType = :targetType) " +
       "AND (cast(:targetId as long) IS NULL OR a.targetId = :targetId) " +
       "AND (cast(:from as localdatetime) IS NULL OR a.createdAt >= :from) " +
       "AND (cast(:to as localdatetime) IS NULL OR a.createdAt <= :to) " +
       "ORDER BY a.createdAt DESC")
Page<ActivityLog> findWithFilters(
        @Param("userId")     Long userId,
        @Param("action")     ActivityLogAction action,
        @Param("targetType") String targetType,
        @Param("targetId")   Long targetId,
        @Param("from")       LocalDateTime from,
        @Param("to")         LocalDateTime to,
        Pageable pageable
);
}