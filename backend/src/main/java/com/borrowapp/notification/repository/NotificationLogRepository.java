package com.borrowapp.notification.repository;

import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {

    Page<NotificationLog> findByStatusOrderByCreatedAtDesc(
            NotificationLogStatus status, Pageable pageable);

    /**
     * Lấy danh sách email FAILED chưa vượt quá maxRetry – dùng cho scheduler.
     */
    @Query("SELECT n FROM NotificationLog n WHERE n.status = 'FAILED' AND n.retryCount < :maxRetry ORDER BY n.createdAt ASC")
    List<NotificationLog> findRetryable(@Param("maxRetry") int maxRetry, Pageable pageable);
}
