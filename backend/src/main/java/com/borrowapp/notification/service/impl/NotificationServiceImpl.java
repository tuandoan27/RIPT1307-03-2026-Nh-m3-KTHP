package com.borrowapp.notification.service.impl;

import com.borrowapp.common.response.PageResponse;
import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.dto.NotificationResponse;
import com.borrowapp.notification.entity.Notification;
import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.enums.NotificationStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.mapper.NotificationMapper;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.repository.NotificationRepository;
import com.borrowapp.notification.service.EmailService;
import com.borrowapp.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository    notifRepo;
    private final NotificationLogRepository logRepo;
    private final EmailService              emailService;
    private final NotificationMapper        mapper;

    @Override
    @Transactional
    public void sendAndNotify(Long recipientId, String recipientEmail,
                               NotificationType type,
                               String title, String message, String link,
                               String emailSubject, String emailHtmlBody) {
        saveNotification(recipientId, recipientEmail, type, title, message, link);
        emailService.sendAsync(recipientEmail, emailSubject, emailHtmlBody);
    }

    @Override
    @Transactional
    public void notifyOnly(Long recipientId, String recipientEmail,
                            NotificationType type,
                            String title, String message, String link) {
        saveNotification(recipientId, recipientEmail, type, title, message, link);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationBellResponse getBell(Long userId, int page, int pageSize) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(pageSize, 1), 50)
        );
        Page<Notification> resultPage =
                notifRepo.findByRecipientIdOrderByCreatedAtDesc(userId, pageable);

        long unread = notifRepo.countByRecipientIdAndStatus(userId, NotificationStatus.UNREAD);

        List<NotificationResponse> items = resultPage.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return NotificationBellResponse.builder()
                .unreadCount(unread)
                .items(items)
                .total(resultPage.getTotalElements())
                .page(resultPage.getNumber())
                .pageSize(resultPage.getSize())
                .build();
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notif = notifRepo.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Notification not found: " + notificationId));

        if (!notif.getRecipientId().equals(userId)) {
            throw new SecurityException(
                    "Access denied to notification: " + notificationId);
        }

        if (notif.getStatus() == NotificationStatus.UNREAD) {
            notif.markAsRead();
            notifRepo.save(notif);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        int updated = notifRepo.markAllAsRead(userId);
        log.debug("[Notification] markAllAsRead userId={} updated={}", userId, updated);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationLogResponse> getFailedLogs(int page, int pageSize) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(pageSize, 1), 100)
        );
        return logRepo
                .findByStatusOrderByCreatedAtDesc(NotificationLogStatus.FAILED, pageable)
                .map(mapper::toLogResponse);
    }

    @Override
    public void retryEmail(Long notificationLogId) {
        logRepo.findById(notificationLogId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "NotificationLog not found: " + notificationLogId));
        emailService.retryAsync(notificationLogId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationLogResponse> getNotificationLogs(
            int page, int pageSize, NotificationLogStatus status) {

        int pageIndex = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(
                pageIndex,
                Math.min(Math.max(pageSize, 1), 100),
                Sort.by("createdAt").descending()
        );

        Page<NotificationLog> result = (status != null)
                ? logRepo.findByStatus(status, pageable)
                : logRepo.findAll(pageable);

        List<NotificationLogResponse> items = result.getContent()
                .stream()
                .map(mapper::toLogResponse)
                .toList();

        return PageResponse.<NotificationLogResponse>builder()
                .items(items)
                .total(result.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private void saveNotification(Long recipientId, String recipientEmail,
                                   NotificationType type,
                                   String title, String message, String link) {
        Notification notif = Notification.builder()
                .recipientId(recipientId)
                .recipientEmail(recipientEmail)
                .type(type)
                .title(title)
                .message(message)
                .link(link)
                .build();
        notifRepo.save(notif);
    }
}