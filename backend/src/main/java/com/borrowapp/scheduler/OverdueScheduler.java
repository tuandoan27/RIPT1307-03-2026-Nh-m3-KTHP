package com.borrowapp.scheduler;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdueScheduler {

    private final BorrowRequestRepository borrowRequestRepository;
    private final OverdueRequestProcessor overdueRequestProcessor;

    @Scheduled(cron = "0 0 0 * * *")
    public void processOverdueRequests() {
        LocalDate today = LocalDate.now();
        List<BorrowRequest> overdueList = borrowRequestRepository.findOverdueRequests(
                RequestStatus.APPROVED, today
        );

        if (overdueList.isEmpty()) {
            log.info("[OverdueScheduler] Không có request quá hạn.");
            return;
        }

        log.info("[OverdueScheduler] Bắt đầu xử lý {} request quá hạn.", overdueList.size());

        int processedCount = 0;
        int lockedCount = 0;

        for (BorrowRequest request : overdueList) {
            try {
                boolean wasLocked = overdueRequestProcessor.process(request, today);
                processedCount++;
                if (wasLocked) lockedCount++;
            } catch (Exception e) {
                log.error("[OverdueScheduler] Lỗi khi xử lý request id={}: {}", request.getId(), e.getMessage());
            }
        }

        log.info("[OverdueScheduler] Hoàn tất: {} request chuyển OVERDUE, {} user bị khóa.",
                processedCount, lockedCount);
    }
}