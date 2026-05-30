package com.borrowapp.notification.service;

import com.borrowapp.activity.util.LoggingHelper;
import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.service.impl.EmailServiceImpl;
import com.borrowapp.testutil.TestFixtures;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceImplTest {

    @Mock JavaMailSender             mailSender;
    @Mock NotificationLogRepository  logRepo;
    @Mock LoggingHelper              loggingHelper;
    @Mock MimeMessage                mimeMessage;

    @InjectMocks EmailServiceImpl service;

    /**
     * Fix Nhóm E.1: dùng lenient() để stub không bị Mockito Strict báo
     * UnnecessaryStubbingException ở test retryAsync_logNotFound_throwsIllegalArgument
     * (test đó throw trước khi gọi createMimeMessage).
     */
    @BeforeEach
    void setUp() {
        lenient().when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ─── sendAsync() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendAsync() – fire and forget")
    class SendAsyncTests {

        @Test
        @DisplayName("Gửi thành công → NotificationLog status=SUCCESS")
        void sendAsync_success_logsSuccessStatus() {
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any(NotificationLog.class)))
                    .willReturn(freshLog)
                    .willReturn(freshLog);
            willDoNothing().given(mailSender).send(mimeMessage);

            service.sendAsync("user@example.com", "Subject", "<p>Body</p>");

            then(logRepo).should(times(2)).save(any(NotificationLog.class));

            ArgumentCaptor<NotificationLog> captor =
                    ArgumentCaptor.forClass(NotificationLog.class);
            then(logRepo).should(times(2)).save(captor.capture());

            NotificationLog secondSave = captor.getAllValues().get(1);
            assertThat(secondSave.getStatus()).isEqualTo(NotificationLogStatus.SUCCESS);
            assertThat(secondSave.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Gửi thất bại → NotificationLog status=FAILED, retryCount tăng, không throw")
        void sendAsync_mailFails_logsFailedAndDoesNotThrow() {
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any(NotificationLog.class))).willReturn(freshLog);
            willThrow(new RuntimeException("SMTP timeout"))
                    .given(mailSender).send(any(MimeMessage.class));

            assertThatNoException().isThrownBy(() ->
                    service.sendAsync("user@example.com", "Subj", "Body")
            );

            then(logRepo).should(atLeastOnce()).save(any());
        }

        @Test
        @DisplayName("NotificationLog được tạo trước khi gửi (status=FAILED mặc định)")
        void sendAsync_createsLogBeforeSend() {
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any())).willReturn(freshLog);
            willThrow(new RuntimeException("fail")).given(mailSender).send(mimeMessage);

            service.sendAsync("to@example.com", "Subject", "Body");

            then(logRepo).should(atLeast(1)).save(any(NotificationLog.class));
        }

        @Test
        @DisplayName("Error message dài hơn 500 ký tự được truncate")
        void sendAsync_longErrorMessage_truncatedTo500() {
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any())).willReturn(freshLog);

            String longError = "E".repeat(1000);
            willThrow(new RuntimeException(longError))
                    .given(mailSender).send(any(MimeMessage.class));

            service.sendAsync("to@x.com", "Sub", "Body");

            ArgumentCaptor<NotificationLog> captor =
                    ArgumentCaptor.forClass(NotificationLog.class);
            then(logRepo).should(times(2)).save(captor.capture());

            String savedError = captor.getAllValues().get(1).getErrorMessage();
            assertThat(savedError).isNotNull();
            assertThat(savedError.length()).isLessThanOrEqualTo(500);
        }
    }

    // ─── retryAsync() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("retryAsync() – retry email failed")
    class RetryAsyncTests {

        @Test
        @DisplayName("Retry thành công → status=SUCCESS, log activity RETRY_EMAIL")
        void retryAsync_success_updatesStatusAndLogsActivity() {
            NotificationLog failedLog = TestFixtures.failedLog(10L);
            given(logRepo.findById(10L)).willReturn(Optional.of(failedLog));
            given(logRepo.save(any())).willReturn(failedLog);
            willDoNothing().given(mailSender).send(mimeMessage);

            service.retryAsync(10L);

            then(logRepo).should(atLeast(2)).save(any());
            then(loggingHelper).should().logSystem(any(), eq("NOTIFICATION_LOG"), eq(10L), any());
        }

        @Test
        @DisplayName("Retry thất bại → retryCount tăng, không throw")
        void retryAsync_mailFails_incrementsRetryCount() {
            NotificationLog failedLog = TestFixtures.failedLog(10L);
            int initialRetryCount = failedLog.getRetryCount();

            given(logRepo.findById(10L)).willReturn(Optional.of(failedLog));
            given(logRepo.save(any())).willReturn(failedLog);
            willThrow(new RuntimeException("still failing"))
                    .given(mailSender).send(any(MimeMessage.class));

            assertThatNoException().isThrownBy(() -> service.retryAsync(10L));

            assertThat(failedLog.getRetryCount()).isGreaterThan(initialRetryCount);
            assertThat(failedLog.getStatus()).isEqualTo(NotificationLogStatus.FAILED);
        }

        @Test
        @DisplayName("NotificationLog không tồn tại → ném IllegalArgumentException")
        void retryAsync_logNotFound_throwsIllegalArgument() {
            given(logRepo.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.retryAsync(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("999");
        }

        /**
         * Fix Nhóm E.2: ArgumentCaptor chỉ lưu reference, không snapshot.
         * retryAsync sửa cùng 1 object qua nhiều bước:
         *   markRetrying() → save(nlog)   ← status = RETRYING tại thời điểm này
         *   doSend() → markSuccess() → save(nlog) ← status = SUCCESS tại thời điểm này
         * Captor.getAllValues() trả 2 reference giống nhau, cả 2 đều mang
         * trạng thái cuối (SUCCESS) → anyMatch(RETRYING) false.
         *
         * Fix: snapshot status NGAY trong willAnswer khi save() được gọi.
         */
        @Test
        @DisplayName("Trước khi gửi, log được đánh dấu RETRYING")
        void retryAsync_marksRetryingBeforeSend() {
            NotificationLog failedLog = TestFixtures.failedLog(10L);
            given(logRepo.findById(10L)).willReturn(Optional.of(failedLog));

            List<NotificationLogStatus> savedStatuses = new ArrayList<>();
            given(logRepo.save(any())).willAnswer(inv -> {
                NotificationLog arg = inv.getArgument(0);
                savedStatuses.add(arg.getStatus());
                return arg;
            });
            willDoNothing().given(mailSender).send(mimeMessage);

            service.retryAsync(10L);

            assertThat(savedStatuses).contains(NotificationLogStatus.RETRYING);
            assertThat(savedStatuses).contains(NotificationLogStatus.SUCCESS);
            assertThat(savedStatuses.indexOf(NotificationLogStatus.RETRYING))
                    .isLessThan(savedStatuses.indexOf(NotificationLogStatus.SUCCESS));
        }
    }
}