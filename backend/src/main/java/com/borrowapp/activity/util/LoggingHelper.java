package com.borrowapp.activity.util;

import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Thin helper – wrap ActivityLogService với JSON detail tự động.
 *
 * Cách dùng trong các service khác:
 * <pre>
 *   loggingHelper.log(currentUser.getId(), currentUser.getFullName(),
 *                     ActivityLogAction.APPROVE_REQUEST, "REQUEST", request.getId(),
 *                     Map.of("borrower", request.getUser().getEmail(),
 *                            "device",   request.getDevice().getName()));
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingHelper {

    private final ActivityLogService activityLogService;
    private final ObjectMapper       objectMapper;

    /**
     * Ghi log với actor.
     *
     * @param actorId   ID người thực hiện
     * @param actorName Tên người thực hiện
     * @param action    Loại hành động
     * @param targetType Loại đối tượng bị tác động
     * @param targetId  ID đối tượng
     * @param detailObj Detail – sẽ được tự động serialize sang JSON nếu là Object
     */
    public void log(Long actorId, String actorName, ActivityLogAction action,
                    String targetType, Long targetId, Object detailObj) {
        String detail = toJson(detailObj);
        activityLogService.log(actorId, actorName, action, targetType, targetId, detail);
    }

    /**
     * Ghi log system action (không có actor).
     */
    public void logSystem(ActivityLogAction action,
                          String targetType, Long targetId, Object detailObj) {
        String detail = toJson(detailObj);
        activityLogService.logSystem(action, targetType, targetId, detail);
    }

    // ─── Private ─────────────────────────────────────────────────────────────

    private String toJson(Object obj) {
        if (obj == null) return null;
        if (obj instanceof String s) return s;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("[LoggingHelper] Cannot serialize detail: {}", e.getMessage());
            return obj.toString();
        }
    }
}
