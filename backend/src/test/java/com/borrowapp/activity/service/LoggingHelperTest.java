package com.borrowapp.activity.service;

import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.activity.util.LoggingHelper;
import com.borrowapp.common.constants.ActivityLogAction;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoggingHelper")
class LoggingHelperTest {

    @Mock  ActivityLogService activityLogService;
    @Spy   ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks LoggingHelper loggingHelper;

    @Test
    @DisplayName("log() với Map detail → serialize thành JSON rồi gọi service")
    void log_mapDetail_serializesToJson() {
        Map<String, Object> detail = Map.of("device", "Laptop", "qty", 1);

        loggingHelper.log(1L, "admin",
                ActivityLogAction.APPROVE_REQUEST,
                "REQUEST", 100L, detail);

        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        then(activityLogService).should().log(
                eq(1L), eq("admin"),
                eq(ActivityLogAction.APPROVE_REQUEST),
                eq("REQUEST"), eq(100L),
                detailCaptor.capture()
        );

        String json = detailCaptor.getValue();
        assertThat(json).contains("Laptop");
        assertThat(json).contains("device");
    }

    @Test
    @DisplayName("log() với String detail → dùng nguyên không serialize lại")
    void log_stringDetail_passedAsIs() {
        String detail = "plain text detail";

        loggingHelper.log(1L, "admin",
                ActivityLogAction.LOGIN, "USER", 1L, detail);

        then(activityLogService).should().log(
                eq(1L), eq("admin"),
                eq(ActivityLogAction.LOGIN),
                eq("USER"), eq(1L),
                eq("plain text detail")
        );
    }

    @Test
    @DisplayName("log() với null detail → pass null xuống service")
    void log_nullDetail_passesNull() {
        loggingHelper.log(1L, "admin",
                ActivityLogAction.LOGOUT, "USER", 1L, null);

        then(activityLogService).should().log(
                eq(1L), eq("admin"),
                eq(ActivityLogAction.LOGOUT),
                eq("USER"), eq(1L),
                isNull()
        );
    }

    @Test
    @DisplayName("logSystem() → gọi activityLogService.logSystem() đúng params")
    void logSystem_delegatesCorrectly() {
        loggingHelper.logSystem(ActivityLogAction.MARK_OVERDUE,
                "REQUEST", 5L, Map.of("reason", "expired"));

        ArgumentCaptor<String> detailCaptor = ArgumentCaptor.forClass(String.class);
        then(activityLogService).should().logSystem(
                eq(ActivityLogAction.MARK_OVERDUE),
                eq("REQUEST"), eq(5L),
                detailCaptor.capture()
        );

        assertThat(detailCaptor.getValue()).contains("reason");
        assertThat(detailCaptor.getValue()).contains("expired");
    }

    @Test
    @DisplayName("log() với object không serializable → fallback toString(), không throw")
    void log_nonSerializableObject_fallsBackToString() {
        // Object override toString() nhưng ObjectMapper không serialize được
        Object weird = new Object() {
            @Override public String toString() { return "fallback-value"; }
        };

        assertThatNoException().isThrownBy(() ->
                loggingHelper.log(1L, "admin",
                        ActivityLogAction.CREATE_DEVICE,
                        "DEVICE", 1L, weird)
        );
    }
}
