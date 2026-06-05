package com.borrowapp.notification.service;

import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.dto.NotificationResponse;
import com.borrowapp.notification.entity.Notification;
import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.enums.NotificationStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.mapper.NotificationMapper;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.repository.NotificationRepository;
import com.borrowapp.notification.service.impl.NotificationServiceImpl;
import com.borrowapp.testutil.TestFixtures;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService")
class NotificationServiceImplTest {

    @Mock NotificationRepository    notifRepo;
    @Mock NotificationLogRepository logRepo;
    @Mock EmailService              emailService;
    @Mock NotificationMapper        mapper;

    @InjectMocks NotificationServiceImpl service;

    // ─── sendAndNotify() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendAndNotify() – in-app + async email")
    class SendAndNotifyTests {

        @Test
        @DisplayName("Lưu Notification in-app VÀ gọi emailService.sendAsync()")
        void sendAndNotify_savesNotifAndSendsEmail() {
            given(notifRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.sendAndNotify(
                    1L, "user@example.com",
                    NotificationType.REQUEST_APPROVED,
                    "Title", "Message", "/link",
                    "Email Subject", "<p>Html</p>"
            );

            // Lưu notification in-app
            ArgumentCaptor<Notification> notifCaptor =
                    ArgumentCaptor.forClass(Notification.class);
            then(notifRepo).should().save(notifCaptor.capture());
            Notification saved = notifCaptor.getValue();
            assertThat(saved.getRecipientId()).isEqualTo(1L);
            assertThat(saved.getType()).isEqualTo(NotificationType.REQUEST_APPROVED);
            assertThat(saved.getStatus()).isEqualTo(NotificationStatus.UNREAD);

            // Gọi email async
            then(emailService).should().sendAsync(
                    "user@example.com", "Email Subject", "<p>Html</p>");
        }
    }

    // ─── notifyOnly() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("notifyOnly() – chỉ in-app, không gửi email")
    class NotifyOnlyTests {

        @Test
        @DisplayName("Lưu Notification in-app, KHÔNG gọi emailService")
        void notifyOnly_savesNotifWithoutEmail() {
            given(notifRepo.save(any())).willAnswer(inv -> inv.getArgument(0));

            service.notifyOnly(1L, "user@example.com",
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    "Title", "Message", "/link");

            then(notifRepo).should().save(any(Notification.class));
            then(emailService).shouldHaveNoInteractions();
        }
    }

    // ─── getBell() ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getBell() – phân trang + unread count")
    class GetBellTests {

        @Test
        @DisplayName("Trả về đúng unreadCount, items, total, page, pageSize")
        void getBell_returnsCorrectBellData() {
            Notification n1 = TestFixtures.unreadNotification(1L, 10L);
            Notification n2 = TestFixtures.unreadNotification(2L, 10L);
            Page<Notification> mockPage = new PageImpl<>(List.of(n1, n2));

            NotificationResponse r1 = NotificationResponse.builder()
                    .id(1L).status(NotificationStatus.UNREAD).build();
            NotificationResponse r2 = NotificationResponse.builder()
                    .id(2L).status(NotificationStatus.UNREAD).build();

            given(notifRepo.findByRecipientIdOrderByCreatedAtDesc(eq(10L), any()))
                    .willReturn(mockPage);
            given(notifRepo.countByRecipientIdAndStatus(10L, NotificationStatus.UNREAD))
                    .willReturn(2L);
            given(mapper.toResponse(n1)).willReturn(r1);
            given(mapper.toResponse(n2)).willReturn(r2);

            NotificationBellResponse bell = service.getBell(10L, 0, 10);

            assertThat(bell.getUnreadCount()).isEqualTo(2);
            assertThat(bell.getItems()).hasSize(2);
            assertThat(bell.getTotal()).isEqualTo(2);
            assertThat(bell.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("pageSize > 50 bị clamp về 50")
        void getBell_pageSizeOver50_clampedTo50() {
            given(notifRepo.findByRecipientIdOrderByCreatedAtDesc(any(), argThat(p -> p.getPageSize() == 50)))
                    .willReturn(Page.empty());
            given(notifRepo.countByRecipientIdAndStatus(any(), any())).willReturn(0L);

            service.getBell(1L, 0, 9999);

            then(notifRepo).should().findByRecipientIdOrderByCreatedAtDesc(
                    any(), argThat(p -> p.getPageSize() == 50));
        }

        @Test
        @DisplayName("Không có notification → unreadCount=0, items=[]")
        void getBell_noNotifications_returnsZeroUnread() {
            given(notifRepo.findByRecipientIdOrderByCreatedAtDesc(any(), any()))
                    .willReturn(Page.empty());
            given(notifRepo.countByRecipientIdAndStatus(any(), any())).willReturn(0L);

            NotificationBellResponse bell = service.getBell(1L, 0, 10);

            assertThat(bell.getUnreadCount()).isZero();
            assertThat(bell.getItems()).isEmpty();
        }
    }

    // ─── markAsRead() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("markAsRead() – đánh dấu đã đọc")
    class MarkAsReadTests {

        @Test
        @DisplayName("UNREAD notification → status đổi thành READ")
        void markAsRead_unread_updatesStatusToRead() {
            Notification notif = TestFixtures.unreadNotification(1L, 5L);
            given(notifRepo.findById(1L)).willReturn(Optional.of(notif));
            given(notifRepo.save(any())).willReturn(notif);

            service.markAsRead(5L, 1L);

            assertThat(notif.getStatus()).isEqualTo(NotificationStatus.READ);
            assertThat(notif.getReadAt()).isNotNull();
            then(notifRepo).should().save(notif);
        }

        @Test
        @DisplayName("Notification đã READ → không save lại (idempotent)")
        void markAsRead_alreadyRead_doesNotSaveAgain() {
            Notification readNotif = TestFixtures.readNotification(1L, 5L);
            given(notifRepo.findById(1L)).willReturn(Optional.of(readNotif));

            service.markAsRead(5L, 1L);

            then(notifRepo).should(never()).save(any());
        }

        @Test
        @DisplayName("Notification không tồn tại → ném EntityNotFoundException")
        void markAsRead_notFound_throwsEntityNotFound() {
            given(notifRepo.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.markAsRead(5L, 999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Notification thuộc user khác → ném SecurityException")
        void markAsRead_otherUser_throwsSecurityException() {
            Notification notif = TestFixtures.unreadNotification(1L, 5L); // owner = 5
            given(notifRepo.findById(1L)).willReturn(Optional.of(notif));

            assertThatThrownBy(() -> service.markAsRead(99L, 1L)) // caller = 99
                    .isInstanceOf(SecurityException.class);

            then(notifRepo).should(never()).save(any());
        }
    }

    // ─── markAllAsRead() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("markAllAsRead() – đánh dấu tất cả")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Gọi repo.markAllAsRead() với đúng userId")
        void markAllAsRead_callsRepoWithCorrectUserId() {
            given(notifRepo.markAllAsRead(10L)).willReturn(3);

            service.markAllAsRead(10L);

            then(notifRepo).should().markAllAsRead(10L);
        }

        @Test
        @DisplayName("Không có notification UNREAD → không throw")
        void markAllAsRead_nothingToUpdate_doesNotThrow() {
            given(notifRepo.markAllAsRead(any())).willReturn(0);

            assertThatNoException().isThrownBy(() -> service.markAllAsRead(1L));
        }
    }

    // ─── getFailedLogs() ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("getFailedLogs() – lấy email thất bại")
    class GetFailedLogsTests {

        @Test
        @DisplayName("Trả về Page<NotificationLogResponse> đúng status FAILED")
        void getFailedLogs_returnsFailedPage() {
            NotificationLog failed = TestFixtures.failedLog(1L);
            NotificationLogResponse resp = NotificationLogResponse.builder()
                    .id(1L)
                    .status(NotificationLogStatus.FAILED)
                    .retryCount(1)
                    .build();

            given(logRepo.findByStatusOrderByCreatedAtDesc(eq(NotificationLogStatus.FAILED), any()))
                    .willReturn(new PageImpl<>(List.of(failed)));
            given(mapper.toLogResponse(failed)).willReturn(resp);

            Page<NotificationLogResponse> result = service.getFailedLogs(0, 20);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus())
                    .isEqualTo(NotificationLogStatus.FAILED);
        }

        @Test
        @DisplayName("pageSize > 100 bị clamp về 100")
        void getFailedLogs_pageSizeOver100_clampedTo100() {
            given(logRepo.findByStatusOrderByCreatedAtDesc(any(),
                    argThat(p -> p.getPageSize() == 100)))
                    .willReturn(Page.empty());

            service.getFailedLogs(0, 9999);

            then(logRepo).should().findByStatusOrderByCreatedAtDesc(any(),
                    argThat(p -> p.getPageSize() == 100));
        }
    }

    // ─── retryEmail() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("retryEmail() – queue retry")
    class RetryEmailTests {

        @Test
        @DisplayName("Tồn tại log → gọi emailService.retryAsync(id)")
        void retryEmail_exists_callsRetryAsync() {
            given(logRepo.findById(10L))
                    .willReturn(Optional.of(TestFixtures.failedLog(10L)));

            service.retryEmail(10L);

            then(emailService).should().retryAsync(10L);
        }

        @Test
        @DisplayName("Log không tồn tại → ném EntityNotFoundException, KHÔNG gọi retryAsync")
        void retryEmail_notFound_throwsEntityNotFound() {
            given(logRepo.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> service.retryEmail(999L))
                    .isInstanceOf(EntityNotFoundException.class);

            then(emailService).shouldHaveNoInteractions();
        }
    }
}
