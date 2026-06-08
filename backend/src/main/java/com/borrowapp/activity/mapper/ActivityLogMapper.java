package com.borrowapp.activity.mapper;

import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .userId(log.getUserId())
                .userName(log.getUserName() != null ? log.getUserName() : "SYSTEM")
                .action(log.getAction())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .detail(log.getDetail())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
