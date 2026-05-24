package com.borrowapp.notification.controller;

import com.borrowapp.notification.dto.NotificationLogResponse;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationAdminController.class)
@DisplayName("NotificationAdminController")
class NotificationAdminControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  NotificationService service;

    private static final String BASE = "/api/v1/admin/notifications";

    // ─── GET /failed-emails ───────────────────────────────────────────────────

    @Test
    @DisplayName("GET /failed-emails – ADMIN → 200 với danh sách log")
    @WithMockUser(roles = "ADMIN")
    void getFailedEmails_adminRole_returns200() throws Exception {
        NotificationLogResponse log1 = NotificationLogResponse.builder()
                .id(1L)
                .toEmail("user@example.com")
                .subject("Test Subject")
                .status(NotificationLogStatus.FAILED)
                .retryCount(2)
                .errorMessage("Connection refused")
                .createdAt(LocalDateTime.now())
                .build();

        given(service.getFailedLogs(anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of(log1)));

        mockMvc.perform(get(BASE + "/failed-emails")
                        .param("page", "0")
                        .param("pageSize", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].status").value("FAILED"))
                .andExpect(jsonPath("$.data.items[0].retryCount").value(2))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /failed-emails – USER role → 403")
    @WithMockUser(roles = "USER")
    void getFailedEmails_userRole_returns403() throws Exception {
        mockMvc.perform(get(BASE + "/failed-emails"))
                .andExpect(status().isForbidden());

        then(service).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GET /failed-emails – không auth → 401")
    void getFailedEmails_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/failed-emails"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /failed-emails – không có failed log → items=[]")
    @WithMockUser(roles = "ADMIN")
    void getFailedEmails_empty_returnsEmptyItems() throws Exception {
        given(service.getFailedLogs(anyInt(), anyInt()))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get(BASE + "/failed-emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    // ─── POST /retry-email/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /retry-email/{id} – ADMIN, log tồn tại → 200 queued")
    @WithMockUser(roles = "ADMIN")
    void retryEmail_exists_returns200Queued() throws Exception {
        willDoNothing().given(service).retryEmail(10L);

        mockMvc.perform(post(BASE + "/retry-email/10").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email retry queued successfully"));

        then(service).should().retryEmail(10L);
    }

    @Test
    @DisplayName("POST /retry-email/{id} – log không tồn tại → 404")
    @WithMockUser(roles = "ADMIN")
    void retryEmail_notFound_returns404() throws Exception {
        willThrow(new EntityNotFoundException("NotificationLog not found: 999"))
                .given(service).retryEmail(999L);

        mockMvc.perform(post(BASE + "/retry-email/999").with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /retry-email/{id} – USER role → 403")
    @WithMockUser(roles = "USER")
    void retryEmail_userRole_returns403() throws Exception {
        mockMvc.perform(post(BASE + "/retry-email/10").with(csrf()))
                .andExpect(status().isForbidden());

        then(service).shouldHaveNoInteractions();
    }

    // ─── POST /retry-all-failed ───────────────────────────────────────────────

    @Test
    @DisplayName("POST /retry-all-failed – ADMIN, có 2 failed → 200 với queued=2")
    @WithMockUser(roles = "ADMIN")
    void retryAllFailed_hasFailed_returnsQueued() throws Exception {
        NotificationLogResponse r1 = NotificationLogResponse.builder().id(1L)
                .status(NotificationLogStatus.FAILED).build();
        NotificationLogResponse r2 = NotificationLogResponse.builder().id(2L)
                .status(NotificationLogStatus.FAILED).build();

        given(service.getFailedLogs(0, 100))
                .willReturn(new PageImpl<>(List.of(r1, r2)));
        willDoNothing().given(service).retryEmail(anyLong());

        mockMvc.perform(post(BASE + "/retry-all-failed").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.queued").value(2));

        then(service).should().retryEmail(1L);
        then(service).should().retryEmail(2L);
    }

    @Test
    @DisplayName("POST /retry-all-failed – không có failed → queued=0")
    @WithMockUser(roles = "ADMIN")
    void retryAllFailed_noFailed_returnsZeroQueued() throws Exception {
        given(service.getFailedLogs(0, 100))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(post(BASE + "/retry-all-failed").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.queued").value(0));

        then(service).should(never()).retryEmail(anyLong());
    }

    @Test
    @DisplayName("POST /retry-all-failed – USER role → 403")
    @WithMockUser(roles = "USER")
    void retryAllFailed_userRole_returns403() throws Exception {
        mockMvc.perform(post(BASE + "/retry-all-failed").with(csrf()))
                .andExpect(status().isForbidden());
    }
}
