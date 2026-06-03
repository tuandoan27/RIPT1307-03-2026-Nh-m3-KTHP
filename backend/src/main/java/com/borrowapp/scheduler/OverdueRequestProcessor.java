package com.borrowapp.scheduler;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.utils.TransitionValidator;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OverdueRequestProcessor {

    private final BorrowRequestRepository borrowRequestRepository;
    private final UserRepository userRepository;

    /**
     * Xử lý từng request trong transaction riêng lẻ.
     * Trả về true nếu user bị khóa trong lần xử lý này.
     */
    @Transactional
    public boolean process(BorrowRequest request, LocalDate today) {
        // Idempotency — bỏ qua nếu đã xử lý rồi
        if (Boolean.TRUE.equals(request.getPenaltyApplied())) {
            log.warn("[OverdueScheduler] Request id={} đã được xử lý trước đó, bỏ qua.", request.getId());
            return false;
        }

        // Validate transition APPROVED → OVERDUE
        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.OVERDUE)) {
            log.warn("[OverdueScheduler] Transition không hợp lệ cho request id={}, status hiện tại={}.",
                    request.getId(), request.getStatus());
            return false;
        }

        // Tính số ngày quá hạn và điểm phạt
        long daysOverdue = ChronoUnit.DAYS.between(request.getEndDate(), today);
        int penaltyPoint = daysOverdue <= 3 ? 1 : 3;

        // Cập nhật user
        User user = request.getUser();
        user.setPenaltyPoint(user.getPenaltyPoint() + penaltyPoint);

        boolean justLocked = false;
        if (!user.isLocked() && user.getPenaltyPoint() >= 10) {
            user.setLocked(true);
            justLocked = true;
            log.info("[OverdueScheduler] User id={} bị khóa do đạt {} điểm phạt.",
                    user.getId(), user.getPenaltyPoint());
        }

        // Cập nhật request
        request.setStatus(RequestStatus.OVERDUE);
        request.setPenaltyApplied(true);

        userRepository.save(user);
        borrowRequestRepository.save(request);

        log.info("[OverdueScheduler] Request id={} → OVERDUE | user id={} | +{} điểm | tổng={} điểm.",
                request.getId(), user.getId(), penaltyPoint, user.getPenaltyPoint());

        return justLocked;
    }
}