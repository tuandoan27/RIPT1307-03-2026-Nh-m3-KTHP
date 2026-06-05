package com.borrowapp.notification.scheduler;

import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Tự động retry các email FAILED mỗi 15 phút.
 * Chỉ retry những email chưa vượt quá MAX_RETRY lần.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailRetryScheduler {

    /** Số lần retry tối đa – sau đó không tự động retry nữa, cần Admin xử lý thủ công */
    private static final int MAX_RETRY  = 5;

    /** Số email xử lý mỗi batch */
    private static final int BATCH_SIZE = 20;

    private final NotificationLogRepository logRepo;
    private final EmailService              emailService;

    /**
     * Chạy mỗi 15 phút, delay khởi động 1 phút sau khi app start.
     * fixedDelay đảm bảo thời gian tính từ lúc batch trước HOÀN THÀNH,
     * tránh overlap nếu batch chạy lâu hơn 15 phút.
     */
    @Scheduled(fixedDelay = 15 * 60 * 1000L, initialDelay = 60_000L)
    public void retryFailedEmails() {
        List<NotificationLog> retryable =
                logRepo.findRetryable(MAX_RETRY, PageRequest.of(0, BATCH_SIZE));

        if (retryable.isEmpty()) {
            log.debug("[EmailRetryScheduler] No retryable emails found.");
            return;
        }

        log.info("[EmailRetryScheduler] Found {} email(s) to retry (maxRetry={})",
                retryable.size(), MAX_RETRY);

        retryable.forEach(nlog -> {
            try {
                emailService.retryAsync(nlog.getId());
            } catch (Exception ex) {
                log.error("[EmailRetryScheduler] Cannot queue retry for logId={} err={}",
                        nlog.getId(), ex.getMessage());
            }
        });
    }
}
