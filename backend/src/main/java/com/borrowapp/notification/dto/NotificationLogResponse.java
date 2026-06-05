package com.borrowapp.notification.dto;

import com.borrowapp.notification.enums.NotificationLogStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationLogResponse {
    private Long id;
    private String recipient;   // mapped từ toEmail
    private String subject;
    private NotificationLogStatus status;
    private int retryCount;
    private String errorMessage;
    private LocalDateTime createdAt;
}