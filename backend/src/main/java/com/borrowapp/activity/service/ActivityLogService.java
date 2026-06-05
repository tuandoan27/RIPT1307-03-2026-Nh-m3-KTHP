package com.borrowapp.activity.service;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.common.constants.ActivityLogAction;
import org.springframework.data.domain.Page;

public interface ActivityLogService {

    /**
     * Ghi log với actor là người dùng.
     * KHÔNG được ném exception ra ngoài – chỉ log lỗi nội bộ.
     *
     * @param actorId   ID người thực hiện (null = system)
     * @param actorName Tên hiển thị người thực hiện
     * @param action    Hành động theo enum
     * @param targetType Loại đối tượng (DEVICE, REQUEST, USER…)
     * @param targetId  ID đối tượng
     * @param detail    Chi tiết (JSON string hoặc plain text)
     */
    void log(Long actorId, String actorName, ActivityLogAction action,
             String targetType, Long targetId, String detail);

    /**
     * Overload tiện lợi cho system action (không có actor).
     */
    void logSystem(ActivityLogAction action,
                   String targetType, Long targetId, String detail);

    /**
     * Lấy danh sách log có filter + phân trang.
     */
    Page<ActivityLogResponse> getLogs(ActivityLogFilterRequest filter);
}
