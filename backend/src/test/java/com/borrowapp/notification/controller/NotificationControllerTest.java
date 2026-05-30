package com.borrowapp.notification.controller;

import com.borrowapp.notification.dto.NotificationBellResponse;
import com.borrowapp.notification.dto.NotificationResponse;
import com.borrowapp.notification.enums.NotificationStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Self-contained test: principal class TestPrincipal được khai báo ngay trong
 * file này (public static), KHÔNG cần tạo CustomUserPrincipal ở main code.
 *
 * Khi bạn tích hợp module User/JWT thật → thay TestPrincipal bằng class
 * UserDetails của module đó (miễn là có getId() trả về Long).
 */
@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController")
class NotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  NotificationService service;

    private static final String BASE = "/api/v1/notifications";

    // ─── Principal nội bộ chỉ dùng cho test ──────────────────────────────────
    /**
     * @AuthenticationPrincipal(expression = "id") sẽ đọc field này qua SpEL.
     * Phải là public class + có getId() public để SpEL resolve được.
     */
    public static class TestPrincipal implements UserDetails {
        private final Long id;
        private final String username;
        private final List<String> roles;

        public TestPrincipal(Long id, String username, List<String> roles) {
            this.id = id;
            this.username = username;
            this.roles = roles;
        }

        public Long getId() { return id; }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return roles.stream()
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();
        }
        @Override public String  getPassword()              { return ""; }
        @Override public String  getUsername()              { return username; }
        @Override public boolean isAccountNonExpired()      { return true; }
        @Override public boolean isAccountNonLocked()       { return true; }
        @Override public boolean isCredentialsNonExpired()  { return true; }
        @Override public boolean isEnabled()                { return true; }
    }

    /** Principal helper – user ID 42, role USER */
    private static TestPrincipal userPrincipal() {
        return new TestPrincipal(42L, "user", List.of("USER"));
    }

    // ─── GET /bell ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /bell – đã xác thực → 200 với unreadCount và items")
    void getBell_authenticated_returns200WithData() throws Exception {
        NotificationResponse item = NotificationResponse.builder()
                .id(1L)
                .type(NotificationType.REQUEST_APPROVED)
                .title("Yêu cầu được duyệt")
                .message("Laptop đã được duyệt.")
                .status(NotificationStatus.UNREAD)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationBellResponse bell = NotificationBellResponse.builder()
                .unreadCount(1).items(List.of(item)).total(1).page(0).pageSize(10)
                .build();

        given(service.getBell(eq(42L), anyInt(), anyInt())).willReturn(bell);

        mockMvc.perform(get(BASE + "/bell")
                        .with(user(userPrincipal()))
                        .param("page", "0")
                        .param("pageSize", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unreadCount").value(1))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].id").value(1))
                .andExpect(jsonPath("$.data.items[0].type").value("REQUEST_APPROVED"))
                .andExpect(jsonPath("$.data.total").value(1));
    }

    @Test
    @DisplayName("GET /bell – chưa xác thực → 401")
    void getBell_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE + "/bell"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /bell – không có notification → unreadCount=0, items=[]")
    void getBell_empty_returnsZeroUnreadEmptyItems() throws Exception {
        NotificationBellResponse emptyBell = NotificationBellResponse.builder()
                .unreadCount(0).items(List.of()).total(0).page(0).pageSize(10)
                .build();

        given(service.getBell(any(), anyInt(), anyInt())).willReturn(emptyBell);

        mockMvc.perform(get(BASE + "/bell")
                        .with(user(userPrincipal()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    // ─── PATCH /{id}/read ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /{id}/read – thành công → 200 'Marked as read'")
    void markAsRead_success_returns200() throws Exception {
        willDoNothing().given(service).markAsRead(eq(42L), eq(1L));

        mockMvc.perform(patch(BASE + "/1/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Marked as read"));
    }

    @Test
    @DisplayName("PATCH /{id}/read – không có notification → 404")
    void markAsRead_notFound_returns404() throws Exception {
        willThrow(new EntityNotFoundException("Notification not found: 999"))
                .given(service).markAsRead(any(), eq(999L));

        mockMvc.perform(patch(BASE + "/999/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /{id}/read – sai owner → 403")
    void markAsRead_wrongOwner_returns403() throws Exception {
        willThrow(new SecurityException("Access denied"))
                .given(service).markAsRead(any(), eq(1L));

        mockMvc.perform(patch(BASE + "/1/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /{id}/read – chưa xác thực → 401")
    void markAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch(BASE + "/1/read").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ─── PATCH /read-all ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PATCH /read-all – thành công → 200")
    void markAllAsRead_success_returns200() throws Exception {
        willDoNothing().given(service).markAllAsRead(eq(42L));

        mockMvc.perform(patch(BASE + "/read-all")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("All notifications marked as read"));
    }

    @Test
    @DisplayName("PATCH /read-all – chưa xác thực → 401")
    void markAllAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(patch(BASE + "/read-all").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}