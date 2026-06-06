package com.borrowapp.notification.scheduler;

import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.service.EmailService;
import com.borrowapp.testutil.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailRetryScheduler")
class EmailRetrySchedulerTest {

    @Mock NotificationLogRepository logRepo;
    @Mock EmailService              emailService;

    @InjectMocks EmailRetryScheduler scheduler;

    @Test
    @DisplayName("Có 3 email FAILED chưa vượt MAX_RETRY → retryAsync gọi 3 lần")
    void retryFailedEmails_hasRetryable_callsRetryAsyncForEach() {
        NotificationLog log1 = TestFixtures.failedLog(1L);
        NotificationLog log2 = TestFixtures.failedLog(2L);
        NotificationLog log3 = TestFixtures.failedLog(3L);

        given(logRepo.findRetryable(eq(5), any(PageRequest.class)))
                .willReturn(List.of(log1, log2, log3));

        scheduler.retryFailedEmails();

        then(emailService).should().retryAsync(1L);
        then(emailService).should().retryAsync(2L);
        then(emailService).should().retryAsync(3L);
        then(emailService).should(times(3)).retryAsync(anyLong());
    }

    @Test
    @DisplayName("Không có email retryable → retryAsync không được gọi")
    void retryFailedEmails_noRetryable_doesNotCallRetryAsync() {
        given(logRepo.findRetryable(anyInt(), any())).willReturn(List.of());

        scheduler.retryFailedEmails();

        then(emailService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("retryAsync ném exception cho 1 email → không dừng, tiếp tục retry email khác")
    void retryFailedEmails_oneRetryThrows_continuesWithOthers() {
        NotificationLog log1 = TestFixtures.failedLog(1L);
        NotificationLog log2 = TestFixtures.failedLog(2L);

        given(logRepo.findRetryable(anyInt(), any()))
                .willReturn(List.of(log1, log2));

        // log1 throw, log2 phải vẫn được retry
        willThrow(new RuntimeException("queue full")).given(emailService).retryAsync(1L);
        willDoNothing().given(emailService).retryAsync(2L);

        // Không throw ra ngoài
        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> scheduler.retryFailedEmails());

        then(emailService).should().retryAsync(1L);
        then(emailService).should().retryAsync(2L);
    }

    @Test
    @DisplayName("Truy vấn repo với đúng MAX_RETRY=5 và BATCH_SIZE=20")
    void retryFailedEmails_queriesRepoWithCorrectParams() {
        given(logRepo.findRetryable(anyInt(), any())).willReturn(List.of());

        scheduler.retryFailedEmails();

        then(logRepo).should().findRetryable(
                eq(5),
                argThat(p -> p.getPageSize() == 20 && p.getPageNumber() == 0)
        );
    }
}
