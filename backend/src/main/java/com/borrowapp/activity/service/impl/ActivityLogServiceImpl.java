package com.borrowapp.activity.service.impl;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.activity.mapper.ActivityLogMapper;
import com.borrowapp.activity.repository.ActivityLogRepository;
import com.borrowapp.activity.repository.ActivityLogSpecification;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository repo;
    private final ActivityLogMapper     mapper;
    private final UserRepository        userRepo;

    /**
     * Ghi log hành động của user.
     *
     * actorId   — id người thực hiện, null nếu là system/cron
     * actorName — giữ lại cho backward compat với caller, không lưu DB
     *             (tên lấy từ user.getFullName() lúc đọc qua mapper)
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId, String actorName, ActivityLogAction action,
                    String targetType, Long targetId, String detail) {
        try {
            // getReferenceById trả về proxy — không query DB, chỉ set user_id FK khi save
            User actor = (actorId != null) ? userRepo.getReferenceById(actorId) : null;

            ActivityLog entry = ActivityLog.builder()
                    .user(actor)
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ActivityLogResponse> getActivityLogs(ActivityLogFilterRequest filter) {
        int pageIndex = Math.max(filter.getPage() - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                filter.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Specification<ActivityLog> spec = ActivityLogSpecification.withFilter(
                filter.getAction(),
                filter.getUserId(),
                filter.getStartDate(),
                filter.getEndDate()
        );

        Page<ActivityLog> page = repo.findAll(spec, pageable);

        List<ActivityLogResponse> items = page.getContent()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return PageResponse.<ActivityLogResponse>builder()
                .items(items)
                .total(page.getTotalElements())
                .page(filter.getPage())
                .pageSize(filter.getPageSize())
                .build();
    }
}