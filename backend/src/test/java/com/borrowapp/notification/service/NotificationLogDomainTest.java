package com.borrowapp.notification.service;

import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.testutil.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests cho các domain method của NotificationLog entity.
 * Không cần Spring context – test thuần Java.
 */
@DisplayName("NotificationLog – domain methods")
class NotificationLogDomainTest {

    @Test
    @DisplayName("markSuccess() → status=SUCCESS, errorMessage=null")
    void markSuccess_setsSuccessStatusAndClearsError() {
        NotificationLog log = TestFixtures.failedLog(1L);
        assertThat(log.getStatus()).isEqualTo(NotificationLogStatus.FAILED);

        log.markSuccess();

        assertThat(log.getStatus()).isEqualTo(NotificationLogStatus.SUCCESS);
        assertThat(log.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("incrementRetry() → retryCount tăng, error được lưu, status=FAILED")
    void incrementRetry_incrementsCountAndSetsError() {
        NotificationLog log = TestFixtures.freshLog(1L);
        assertThat(log.getRetryCount()).isZero();

        log.incrementRetry("Connection timeout");

        assertThat(log.getRetryCount()).isEqualTo(1);
        assertThat(log.getErrorMessage()).isEqualTo("Connection timeout");
        assertThat(log.getStatus()).isEqualTo(NotificationLogStatus.FAILED);
    }

    @Test
    @DisplayName("incrementRetry() gọi nhiều lần → retryCount tăng dần")
    void incrementRetry_calledMultipleTimes_accumulatesCount() {
        NotificationLog log = TestFixtures.freshLog(1L);

        log.incrementRetry("err1");
        log.incrementRetry("err2");
        log.incrementRetry("err3");

        assertThat(log.getRetryCount()).isEqualTo(3);
        assertThat(log.getErrorMessage()).isEqualTo("err3"); // lưu lỗi mới nhất
    }

    @Test
    @DisplayName("markRetrying() → status=RETRYING")
    void markRetrying_setsRetryingStatus() {
        NotificationLog log = TestFixtures.failedLog(1L);

        log.markRetrying();

        assertThat(log.getStatus()).isEqualTo(NotificationLogStatus.RETRYING);
    }

    @Test
    @DisplayName("markSuccess() sau incrementRetry() → status về SUCCESS, error null")
    void markSuccess_afterRetry_resetsToSuccess() {
        NotificationLog log = TestFixtures.freshLog(1L);
        log.incrementRetry("fail");
        log.incrementRetry("fail again");

        assertThat(log.getRetryCount()).isEqualTo(2);

        log.markSuccess();

        assertThat(log.getStatus()).isEqualTo(NotificationLogStatus.SUCCESS);
        assertThat(log.getErrorMessage()).isNull();
        assertThat(log.getRetryCount()).isEqualTo(2); // retryCount KHÔNG reset
    }
}
