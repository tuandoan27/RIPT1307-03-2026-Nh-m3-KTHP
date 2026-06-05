package com.borrowapp.notification.dto;

import com.borrowapp.notification.enums.NotificationLogStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationLogResponse {
    private Long                   id;
    private String                 toEmail;
    private String                 subject;
    private NotificationLogStatus  status;
    private int                    retryCount;
    private String                 errorMessage;
    private LocalDateTime          createdAt;
    private LocalDateTime          updatedAt;
}
