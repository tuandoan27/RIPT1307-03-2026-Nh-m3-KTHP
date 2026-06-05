package com.borrowapp.activity.dto;

import com.borrowapp.common.constants.ActivityLogAction;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
public class ActivityLogFilterRequest {

    private Long               userId;
    private ActivityLogAction  action;
    private String             targetType;
    private Long               targetId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    private int page     = 0;
    private int pageSize = 20;
}
