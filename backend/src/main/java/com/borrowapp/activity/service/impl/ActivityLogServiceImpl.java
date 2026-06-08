package com.borrowapp.activity.service.impl;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.activity.mapper.ActivityLogMapper;
import com.borrowapp.activity.repository.ActivityLogRepository;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;
    private final ActivityLogMapper     mapper;

    /**
     * REQUIRES_NEW: log ghi trong transaction độc lập.
     * Dù outer transaction rollback, log vẫn được lưu.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId, String actorName, ActivityLogAction action,
                    String targetType, Long targetId, String detail) {
        try {
            ActivityLog entry = ActivityLog.builder()
                    .userId(actorId)
                    .userName(actorName)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .detail(detail)
                    .build();
            repo.save(entry);
        } catch (Exception ex) {
            // Không ném exception – chỉ log lỗi nội bộ để không ảnh hưởng luồng chính
            log.error("[ActivityLog] Failed to persist log | action={} targetType={} targetId={} | err={}",
                    action, targetType, targetId, ex.getMessage(), ex);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSystem(ActivityLogAction action,
                          String targetType, Long targetId, String detail) {
        log(null, "SYSTEM", action, targetType, targetId, detail);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogResponse> getLogs(ActivityLogFilterRequest filter) {
        PageRequest pageable = PageRequest.of(
                Math.max(filter.getPage(), 0),
                Math.min(Math.max(filter.getPageSize(), 1), 100)
        );
        return repo.findWithFilters(
                filter.getUserId(),
                filter.getAction(),
                filter.getTargetType(),
                filter.getTargetId(),
                filter.getFrom(),
                filter.getTo(),
                pageable
        ).map(mapper::toResponse);
    }
}
