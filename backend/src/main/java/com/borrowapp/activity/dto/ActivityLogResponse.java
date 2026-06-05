package com.borrowapp.activity.dto;

import com.borrowapp.common.constants.ActivityLogAction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private Long               id;
    private Long               userId;
    private String             userName;
    private ActivityLogAction  action;
    private String             targetType;
    private Long               targetId;
    private String             detail;
    private LocalDateTime      createdAt;
}
