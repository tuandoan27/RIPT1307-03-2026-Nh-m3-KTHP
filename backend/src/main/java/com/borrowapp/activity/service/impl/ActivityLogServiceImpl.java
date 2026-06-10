package com.borrowapp.activity.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.activity.mapper.ActivityLogMapper;
import com.borrowapp.activity.repository.ActivityLogRepository;
import com.borrowapp.activity.repository.ActivityLogSpecification;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;
    private final ActivityLogMapper     mapper;

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
        // Frontend gửi page bắt đầu từ 1, Spring bắt đầu từ 0
        int zeroBasedPage = Math.max(filter.getPage() - 1, 0);
        int pageSize      = Math.min(Math.max(filter.getPageSize(), 1), 200);

        PageRequest pageable = PageRequest.of(
                zeroBasedPage,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // Dùng Specification thay JPQL để tránh lỗi PostgreSQL
        // "could not determine data type of parameter" khi tham số null
        Specification<ActivityLog> spec = ActivityLogSpecification.withFilter(
                filter.getAction(),
                filter.getUserId(),
                filter.getFrom(),
                filter.getTo(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getTargetType(),
                filter.getTargetId()
        );

        return repo.findAll(spec, pageable).map(mapper::toResponse);
    }
}