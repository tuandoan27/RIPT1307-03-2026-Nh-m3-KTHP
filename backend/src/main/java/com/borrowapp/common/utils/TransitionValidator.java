package com.borrowapp.common.utils;

import com.borrowapp.common.constants.RequestStatus;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

public class TransitionValidator {

    /*
     * PENDING  → APPROVED, REJECTED
     * APPROVED → RETURNED, OVERDUE
     * OVERDUE  → RETURNED
     * REJECTED, RETURNED → không đi đâu được (terminal)
     */
    private static final Map<RequestStatus, Set<RequestStatus>> VALID_TRANSITIONS =
            new EnumMap<>(RequestStatus.class);

    static {
        VALID_TRANSITIONS.put(RequestStatus.PENDING,   Set.of(RequestStatus.APPROVED, RequestStatus.REJECTED));
        VALID_TRANSITIONS.put(RequestStatus.APPROVED,  Set.of(RequestStatus.RETURNED, RequestStatus.OVERDUE));
        VALID_TRANSITIONS.put(RequestStatus.OVERDUE,   Set.of(RequestStatus.RETURNED));
        VALID_TRANSITIONS.put(RequestStatus.REJECTED,  Set.of());
        VALID_TRANSITIONS.put(RequestStatus.RETURNED,  Set.of());
    }

    private TransitionValidator() {
        // Utility class — không khởi tạo
    }

    /**
     * Kiểm tra transition có hợp lệ không.
     *
     * @param from trạng thái hiện tại
     * @param to   trạng thái muốn chuyển
     * @return true nếu hợp lệ
     */
    public static boolean isValidTransition(RequestStatus from, RequestStatus to) {
        if (from == null || to == null) return false;
        return VALID_TRANSITIONS.getOrDefault(from, Set.of()).contains(to);
    }
}
