package com.borrowapp.activity.service;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ActivityLogService {

    void log(Long actorId, String actorName, ActivityLogAction action,
             String targetType, Long targetId, String detail);


    void logSystem(ActivityLogAction action,
                   String targetType, Long targetId, String detail);


    Page<ActivityLogResponse> getLogs(ActivityLogFilterRequest filter);

    PageResponse<ActivityLogResponse> getActivityLogs(ActivityLogFilterRequest filter);

    List<ActivityLogResponse> getRequestHistory(Long requestId);
}
