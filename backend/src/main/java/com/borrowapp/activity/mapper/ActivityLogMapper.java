package com.borrowapp.activity.mapper;

import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .performedBy(log.getUser() != null
                        ? log.getUser().getFullName()
                        : "System")
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}