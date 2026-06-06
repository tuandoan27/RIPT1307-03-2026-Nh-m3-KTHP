package com.borrowapp.activity.service;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.entity.ActivityLog;
import com.borrowapp.activity.mapper.ActivityLogMapper;
import com.borrowapp.activity.repository.ActivityLogRepository;
import com.borrowapp.activity.service.impl.ActivityLogServiceImpl;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.testutil.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivityLogService")
class ActivityLogServiceImplTest {

    @Mock ActivityLogRepository repo;
    @Mock ActivityLogMapper      mapper;

    @InjectMocks ActivityLogServiceImpl service;

    // ─── log() ───────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("log() – ghi log với actor")
    class LogWithActorTests {

        @Test
        @DisplayName("Lưu ActivityLog đúng field khi actor hợp lệ")
        void log_validActor_savesCorrectFields() {
            // given
            given(repo.save(any(ActivityLog.class)))
                    .willAnswer(inv -> inv.getArgument(0));

            // when
            service.log(1L, "admin",
                    ActivityLogAction.APPROVE_REQUEST,
                    "REQUEST", 100L,
                    "{\"device\":\"Laptop\"}");

            // then – capture argument passed to save()
            ArgumentCaptor<ActivityLog> captor =
                    ArgumentCaptor.forClass(ActivityLog.class);
            then(repo).should().save(captor.capture());

            ActivityLog saved = captor.getValue();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getUserName()).isEqualTo("admin");
            assertThat(saved.getAction()).isEqualTo(ActivityLogAction.APPROVE_REQUEST);
            assertThat(saved.getTargetType()).isEqualTo("REQUEST");
            assertThat(saved.getTargetId()).isEqualTo(100L);
            assertThat(saved.getDetail()).contains("Laptop");
        }

        @Test
        @DisplayName("Actor là null (system action) – vẫn lưu thành công")
        void log_nullActor_savesWithNullUserId() {
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.log(null, "SYSTEM",
                    ActivityLogAction.MARK_OVERDUE,
                    "REQUEST", 200L, "auto");

            ArgumentCaptor<ActivityLog> captor =
                    ArgumentCaptor.forClass(ActivityLog.class);
            then(repo).should().save(captor.capture());

            assertThat(captor.getValue().getUserId()).isNull();
            assertThat(captor.getValue().getUserName()).isEqualTo("SYSTEM");
        }

        @Test
        @DisplayName("Nếu repo.save() ném exception → KHÔNG propagate ra ngoài")
        void log_repoThrows_doesNotPropagate() {
            given(repo.save(any())).willThrow(new RuntimeException("DB down"));

            // ❌ Không được throw – fire and forget style
            assertThatNoException().isThrownBy(() ->
                    service.log(1L, "admin",
                            ActivityLogAction.CREATE_DEVICE,
                            "DEVICE", 1L, "detail")
            );
        }

        @Test
        @DisplayName("detail null – vẫn lưu thành công")
        void log_nullDetail_savesFine() {
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            assertThatNoException().isThrownBy(() ->
                    service.log(1L, "admin",
                            ActivityLogAction.LOGIN,
                            "USER", 1L, null)
            );
        }
    }

    // ─── logSystem() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("logSystem() – system action không có actor")
    class LogSystemTests {

        @Test
        @DisplayName("logSystem() gọi nội bộ log() với userId=null, userName=SYSTEM")
        void logSystem_delegatesWithNullUserId() {
            given(repo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.logSystem(ActivityLogAction.MARK_OVERDUE,
                    "REQUEST", 5L, "detail");

            ArgumentCaptor<ActivityLog> captor =
                    ArgumentCaptor.forClass(ActivityLog.class);
            then(repo).should().save(captor.capture());

            assertThat(captor.getValue().getUserId()).isNull();
            assertThat(captor.getValue().getUserName()).isEqualTo("SYSTEM");
            assertThat(captor.getValue().getAction())
                    .isEqualTo(ActivityLogAction.MARK_OVERDUE);
        }

        @Test
        @DisplayName("logSystem() exception không propagate")
        void logSystem_exceptionDoesNotPropagate() {
            given(repo.save(any())).willThrow(new RuntimeException("fail"));

            assertThatNoException().isThrownBy(() ->
                    service.logSystem(ActivityLogAction.EXPORT_DATA,
                            "SYSTEM", null, null)
            );
        }
    }

    // ─── getLogs() ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getLogs() – phân trang + filter")
    class GetLogsTests {

        private ActivityLogFilterRequest filterOf(int page, int pageSize) {
            ActivityLogFilterRequest f = new ActivityLogFilterRequest();
            f.setPage(page);
            f.setPageSize(pageSize);
            return f;
        }

        @Test
        @DisplayName("Trả về đúng Page khi có kết quả")
        void getLogs_withResults_returnsMappedPage() {
            ActivityLog log1 = TestFixtures.activityLog(1L, ActivityLogAction.APPROVE_REQUEST);
            ActivityLog log2 = TestFixtures.activityLog(2L, ActivityLogAction.REJECT_REQUEST);

            ActivityLogResponse resp1 = ActivityLogResponse.builder()
                    .id(1L).action(ActivityLogAction.APPROVE_REQUEST).build();
            ActivityLogResponse resp2 = ActivityLogResponse.builder()
                    .id(2L).action(ActivityLogAction.REJECT_REQUEST).build();

            Page<ActivityLog> mockPage = new PageImpl<>(List.of(log1, log2));
            given(repo.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(mockPage);
            given(mapper.toResponse(log1)).willReturn(resp1);
            given(mapper.toResponse(log2)).willReturn(resp2);

            Page<ActivityLogResponse> result = service.getLogs(filterOf(0, 20));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getId()).isEqualTo(1L);
            assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Trả về Page rỗng khi không có log")
        void getLogs_empty_returnsEmptyPage() {
            given(repo.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(Page.empty());

            Page<ActivityLogResponse> result = service.getLogs(filterOf(0, 20));

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("page âm được normalize về 0")
        void getLogs_negativePage_normalizesToZero() {
            given(repo.findWithFilters(any(), any(), any(), any(), any(), any(),
                    argThat(p -> p.getPageNumber() == 0)))
                    .willReturn(Page.empty());

            ActivityLogFilterRequest f = filterOf(-5, 10);
            service.getLogs(f);

            // Verify PageRequest được tạo với page=0
            then(repo).should().findWithFilters(
                    any(), any(), any(), any(), any(), any(),
                    argThat(p -> p.getPageNumber() == 0)
            );
        }

        @Test
        @DisplayName("pageSize vượt max 100 bị clamp về 100")
        void getLogs_pageSizeOver100_clampedTo100() {
            given(repo.findWithFilters(any(), any(), any(), any(), any(), any(),
                    argThat(p -> p.getPageSize() == 100)))
                    .willReturn(Page.empty());

            ActivityLogFilterRequest f = filterOf(0, 9999);
            service.getLogs(f);

            then(repo).should().findWithFilters(
                    any(), any(), any(), any(), any(), any(),
                    argThat(p -> p.getPageSize() == 100)
            );
        }

        @Test
        @DisplayName("Filter có action + targetType được pass đúng xuống repo")
        void getLogs_withFilters_passesCorrectParamsToRepo() {
            ActivityLogFilterRequest f = new ActivityLogFilterRequest();
            f.setAction(ActivityLogAction.APPROVE_REQUEST);
            f.setTargetType("REQUEST");
            f.setTargetId(42L);
            f.setPage(0);
            f.setPageSize(10);

            given(repo.findWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .willReturn(Page.empty());

            service.getLogs(f);

            then(repo).should().findWithFilters(
                    isNull(),                                    // userId
                    eq(ActivityLogAction.APPROVE_REQUEST),       // action
                    eq("REQUEST"),                               // targetType
                    eq(42L),                                     // targetId
                    isNull(), isNull(),                          // from, to
                    any(PageRequest.class)
            );
        }
    }
}
