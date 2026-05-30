package com.borrowapp.notification.repository;

import com.borrowapp.notification.entity.Notification;
import com.borrowapp.notification.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    long countByRecipientIdAndStatus(Long recipientId, NotificationStatus status);

    @Modifying
    @Query("UPDATE Notification n " +
           "SET n.status = 'READ', n.readAt = CURRENT_TIMESTAMP " +
           "WHERE n.recipientId = :userId AND n.status = 'UNREAD'")
    int markAllAsRead(@Param("userId") Long userId);
}