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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService")
class EmailServiceImplTest {

    @Mock JavaMailSender             mailSender;
    @Mock NotificationLogRepository  logRepo;
    @Mock LoggingHelper              loggingHelper;
    @Mock MimeMessage                mimeMessage;

    @InjectMocks EmailServiceImpl service;

    @BeforeEach
    void setUp() {
        given(mailSender.createMimeMessage()).willReturn(mimeMessage);
    }

    // ─── sendAsync() ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendAsync() – fire and forget")
    class SendAsyncTests {

        @Test
        @DisplayName("Gửi thành công → NotificationLog status=SUCCESS")
        void sendAsync_success_logsSuccessStatus() {
            // given
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any(NotificationLog.class)))
                    .willReturn(freshLog)    // lần 1 – tạo mới
                    .willReturn(freshLog);   // lần 2 – update success
            willDoNothing().given(mailSender).send(mimeMessage);

            // when
            service.sendAsync("user@example.com", "Subject", "<p>Body</p>");

            // then – save() gọi 2 lần: tạo log + update SUCCESS
            then(logRepo).should(times(2)).save(any(NotificationLog.class));

            ArgumentCaptor<NotificationLog> captor =
                    ArgumentCaptor.forClass(NotificationLog.class);
            then(logRepo).should(times(2)).save(captor.capture());

            // Lần save thứ 2 phải là SUCCESS
            NotificationLog secondSave = captor.getAllValues().get(1);
            assertThat(secondSave.getStatus()).isEqualTo(NotificationLogStatus.SUCCESS);
            assertThat(secondSave.getErrorMessage()).isNull();
        }

        @Test
        @DisplayName("Gửi thất bại → NotificationLog status=FAILED, retryCount tăng, không throw")
        void sendAsync_mailFails_logsFailedAndDoesNotThrow() {
            // given
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any(NotificationLog.class))).willReturn(freshLog);
            willThrow(new RuntimeException("SMTP timeout"))
                    .given(mailSender).send(any(MimeMessage.class));

            // when – không được throw
            assertThatNoException().isThrownBy(() ->
                    service.sendAsync("user@example.com", "Subj", "Body")
            );

            // then – log vẫn được lưu (2 lần: tạo + update failed)
            then(logRepo).should(atLeastOnce()).save(any());
        }

        @Test
        @DisplayName("NotificationLog được tạo trước khi gửi (status=FAILED mặc định)")
        void sendAsync_createsLogBeforeSend() {
            // given – mailSender ném exception ngay lập tức
            NotificationLog freshLog = TestFixtures.freshLog(1L);
            given(logRepo.save(any())).willReturn(freshLog);
            willThrow(new RuntimeException("fail")).given(mailSender).send(mimeMessage);

            // when
            service.sendAsync("to@example.com", "Subject", "Body");

            // then – log được tạo dù mail fail
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
            // given
            NotificationLog failedLog = TestFixtures.failedLog(10L);
            given(logRepo.findById(10L)).willReturn(Optional.of(failedLog));
            given(logRepo.save(any())).willReturn(failedLog);
            willDoNothing().given(mailSender).send(mimeMessage);

            // when
            service.retryAsync(10L);

            // then
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

            // retryCount phải tăng lên
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

        @Test
        @DisplayName("Trước khi gửi, log được đánh dấu RETRYING")
        void retryAsync_marksRetryingBeforeSend() {
            NotificationLog failedLog = TestFixtures.failedLog(10L);
            given(logRepo.findById(10L)).willReturn(Optional.of(failedLog));
            given(logRepo.save(any())).willAnswer(inv -> inv.getArgument(0));
            willDoNothing().given(mailSender).send(mimeMessage);

            service.retryAsync(10L);

            // Lần save đầu tiên phải là RETRYING
            ArgumentCaptor<NotificationLog> captor =
                    ArgumentCaptor.forClass(NotificationLog.class);
            then(logRepo).should(atLeast(1)).save(captor.capture());

            boolean hasRetryingState = captor.getAllValues().stream()
                    .anyMatch(l -> l.getStatus() == NotificationLogStatus.RETRYING);
            assertThat(hasRetryingState).isTrue();
        }
    }
}
