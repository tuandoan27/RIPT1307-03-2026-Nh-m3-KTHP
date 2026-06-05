package com.borrowapp.notification.mapper;

import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.dto.NotificationResponse;
import com.borrowapp.notification.entity.Notification;
import com.borrowapp.notification.entity.NotificationLog;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .link(n.getLink())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }

    public NotificationLogResponse toLogResponse(NotificationLog l) {
        return NotificationLogResponse.builder()
                .id(l.getId())
                .toEmail(l.getToEmail())
                .subject(l.getSubject())
                .status(l.getStatus())
                .retryCount(l.getRetryCount())
                .errorMessage(l.getErrorMessage())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
