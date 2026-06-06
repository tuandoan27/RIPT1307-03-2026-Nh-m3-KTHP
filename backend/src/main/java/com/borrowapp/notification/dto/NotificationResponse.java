package com.borrowapp.notification.dto;

import com.borrowapp.notification.enums.NotificationStatus;
import com.borrowapp.notification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long               id;
    private NotificationType   type;
    private String             title;
    private String             message;
    private String             link;
    private NotificationStatus status;
    private LocalDateTime      createdAt;
    private LocalDateTime      readAt;
}
