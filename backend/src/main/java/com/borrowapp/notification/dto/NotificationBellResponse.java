package com.borrowapp.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class NotificationBellResponse {
    private long unreadCount;
    private List<NotificationResponse> items;
    private long total;
    private int  page;
    private int  pageSize;
}
