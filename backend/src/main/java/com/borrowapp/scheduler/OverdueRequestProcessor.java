package com.borrowapp.scheduler;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.utils.TransitionValidator;
import com.borrowapp.notification.service.EmailService;
import com.borrowapp.penalty.entity.Penalty;
import com.borrowapp.penalty.repository.PenaltyRepository;
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
    private final UserRepository          userRepository;
    private final PenaltyRepository       penaltyRepository;
    private final EmailService            emailService;

    @Transactional
    public boolean process(BorrowRequest request, LocalDate today) {
        // Idempotency — bỏ qua nếu đã xử lý rồi
        if (Boolean.TRUE.equals(request.getPenaltyApplied())) {
            log.warn("[OverdueScheduler] Request id={} đã được xử lý trước đó, bỏ qua.", request.getId());
            return false;
        }

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.OVERDUE)) {
            log.warn("[OverdueScheduler] Transition không hợp lệ cho request id={}, status hiện tại={}.",
                    request.getId(), request.getStatus());
            return false;
        }

        long daysOverdue = ChronoUnit.DAYS.between(request.getEndDate(), today);
        int penaltyPoints = daysOverdue <= 3 ? 1 : 3;
        String reason = String.format("Quá hạn %d ngày", daysOverdue);

        User user = request.getUser();
        user.setPenaltyPoint(user.getPenaltyPoint() + penaltyPoints);

        boolean justLocked = false;
        if (!user.isLocked() && user.getPenaltyPoint() >= 10) {
            user.setLocked(true);
            justLocked = true;
            log.info("[OverdueScheduler] User id={} bị khóa do đạt {} điểm phạt.",
                    user.getId(), user.getPenaltyPoint());
        }

        request.setStatus(RequestStatus.OVERDUE);
        request.setPenaltyApplied(true);


        String equipmentName = request.getEquipment().getName();

        userRepository.save(user);
        borrowRequestRepository.save(request);

        penaltyRepository.save(Penalty.builder()
                .user(user)
                .borrowRequest(request)
                .points(penaltyPoints)
                .reason(reason)
                .build());

        log.info("[OverdueScheduler] Request id={} → OVERDUE | equipment='{}' | user id={} | +{} điểm | tổng={} điểm.",
                request.getId(), equipmentName, user.getId(), penaltyPoints, user.getPenaltyPoint());


        emailService.sendOverdueWarning(user, request);

        return justLocked;
    }
}