package com.borrowapp.activity.dto;

import com.borrowapp.common.constants.ActivityLogAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ActivityLogFilterRequest {

    private int page = 1;
    private int pageSize = 20;

    private ActivityLogAction action;
    private Long userId;

    // ─── Dùng cho getLogs() cũ (findWithFilters) ─────────────────────────────
    private String targetType;
    private Long targetId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;

    // ─── Dùng cho getActivityLogs() mới (Specification) ──────────────────────
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
}