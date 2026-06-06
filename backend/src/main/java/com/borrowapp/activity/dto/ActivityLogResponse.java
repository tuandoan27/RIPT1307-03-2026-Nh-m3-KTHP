package com.borrowapp.activity.dto;

import com.borrowapp.common.constants.ActivityLogAction;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ActivityLogResponse {

    private Long id;

    /** fullName của người thực hiện, "System" nếu là cron/system action */
    private String performedBy;

    private ActivityLogAction action;
    private String targetType;
    private Long targetId;
    private String detail;
    private LocalDateTime createdAt;
}