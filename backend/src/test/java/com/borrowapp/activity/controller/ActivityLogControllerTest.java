package com.borrowapp.activity.controller;

import com.borrowapp.activity.dto.ActivityLogFilterRequest;
import com.borrowapp.activity.dto.ActivityLogResponse;
import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @Import(SecurityConfig.class) → load @EnableMethodSecurity + csrf.disable()
 * vào WebMvcTest context để:
 *  - @PreAuthorize("hasRole('ADMIN')") hoạt động → USER trả 403
 *  - CSRF tắt → DELETE/POST không có csrf token trả 405 (thay vì 403)
 */
@WebMvcTest(ActivityLogController.class)
@DisplayName("ActivityLogController")
class ActivityLogControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ActivityLogService service;

    private final String BASE_URL = "/api/v1/activity-logs";

    // ─── GET /api/v1/activity-logs ────────────────────────────────────────────

    @Test
    @DisplayName("GET – ADMIN role – trả về 200 với danh sách log")
    @WithMockUser(roles = "ADMIN")
    void getLogs_adminRole_returns200WithLogs() throws Exception {
        // FIX: ActivityLogResponse không có userId/userName.
        //      Dùng performedBy (fullName của actor, hoặc "System").
        ActivityLogResponse log1 = ActivityLogResponse.builder()
                .id(1L)
                .performedBy("admin")
                .action(ActivityLogAction.APPROVE_REQUEST)
                .targetType("REQUEST")
                .targetId(100L)
                .createdAt(LocalDateTime.now())
                .build();

        Page<ActivityLogResponse> page = new PageImpl<>(List.of(log1));
        given(service.getLogs(any(ActivityLogFilterRequest.class))).willReturn(page);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("pageSize", "20")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].action").value("APPROVE_REQUEST"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.page").value(0));
    }

    @Test
    @DisplayName("GET – ADMIN role – page rỗng → trả về items=[]")
    @WithMockUser(roles = "ADMIN")
    void getLogs_noLogs_returnsEmptyItems() throws Exception {
        given(service.getLogs(any())).willReturn(Page.empty());

        mockMvc.perform(get(BASE_URL).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty())
                .andExpect(jsonPath("$.data.total").value(0));
    }

    @Test
    @DisplayName("GET – USER role – trả về 403 Forbidden")
    @WithMockUser(roles = "USER")
    void getLogs_userRole_returns403() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());

        then(service).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("GET – không auth – trả về 401 Unauthorized")
    void getLogs_noAuth_returns401() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET – filter by action param – service nhận đúng filter")
    @WithMockUser(roles = "ADMIN")
    void getLogs_withActionFilter_passedToService() throws Exception {
        given(service.getLogs(any())).willReturn(Page.empty());

        mockMvc.perform(get(BASE_URL)
                        .param("action", "APPROVE_REQUEST")
                        .param("targetType", "REQUEST")
                        .param("targetId", "42"))
                .andExpect(status().isOk());

        then(service).should().getLogs(
                argThat(f -> f.getAction() == ActivityLogAction.APPROVE_REQUEST
                          && "REQUEST".equals(f.getTargetType())
                          && Long.valueOf(42).equals(f.getTargetId()))
        );
    }

    @Test
    @DisplayName("DELETE – không có endpoint DELETE – trả về 405")
    @WithMockUser(roles = "ADMIN")
    void delete_notAllowed_returns405() throws Exception {
        // csrf.disable() trong SecurityConfig → không cần .with(csrf())
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("POST – không có endpoint POST – trả về 405")
    @WithMockUser(roles = "ADMIN")
    void post_notAllowed_returns405() throws Exception {
        // csrf.disable() trong SecurityConfig → không cần .with(csrf())
        mockMvc.perform(post(BASE_URL))
                .andExpect(status().isMethodNotAllowed());
    }
}