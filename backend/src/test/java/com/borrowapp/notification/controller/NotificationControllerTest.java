package com.borrowapp.notification.controller;

import com.borrowapp.auth.util.JwtUtil;
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

import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import java.util.Optional;

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
    @MockBean  JwtUtil jwtUtil;
    @MockBean  UserRepository userRepository;

    private static final String BASE = "/api/notifications";

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

    // ─── GET /api/notifications ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/notifications – đã xác thực → 200 với unreadCount và items")
    void getBell_authenticated_returns200WithData() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));

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

        mockMvc.perform(get(BASE)
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
    @DisplayName("GET /api/notifications – chưa xác thực → 401")
    void getBell_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/notifications – không có notification → unreadCount=0, items=[]")
    void getBell_empty_returnsZeroUnreadEmptyItems() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));

        NotificationBellResponse emptyBell = NotificationBellResponse.builder()
                .unreadCount(0).items(List.of()).total(0).page(0).pageSize(10)
                .build();

        given(service.getBell(any(), anyInt(), anyInt())).willReturn(emptyBell);

        mockMvc.perform(get(BASE)
                        .with(user(userPrincipal()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(0))
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    // ─── PUT /{id}/read ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /{id}/read – thành công → 200 'Đã đánh dấu thông báo là đã đọc'")
    void markAsRead_success_returns200() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));
        willDoNothing().given(service).markAsRead(eq(42L), eq(1L));

        mockMvc.perform(put(BASE + "/1/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Đã đánh dấu thông báo là đã đọc"));
    }

    @Test
    @DisplayName("PUT /{id}/read – không có notification → 404")
    void markAsRead_notFound_returns404() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));
        willThrow(new com.borrowapp.common.exception.ResourceNotFoundException("Notification not found"))
                .given(service).markAsRead(any(), eq(999L));

        mockMvc.perform(put(BASE + "/999/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /{id}/read – sai owner → 403")
    void markAsRead_wrongOwner_returns403() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));
        willThrow(new com.borrowapp.common.exception.ForbiddenException("Access denied"))
                .given(service).markAsRead(any(), eq(1L));

        mockMvc.perform(put(BASE + "/1/read")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /{id}/read – chưa xác thực → 401")
    void markAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put(BASE + "/1/read").with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ─── PUT /read-all ─────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT /read-all – thành công → 200")
    void markAllAsRead_success_returns200() throws Exception {
        User u = new User(); u.setId(42L);
        given(userRepository.findByEmail(any())).willReturn(Optional.of(u));
        willDoNothing().given(service).markAllAsRead(eq(42L));

        mockMvc.perform(put(BASE + "/read-all")
                        .with(user(userPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("Đã đánh dấu tất cả thông báo là đã đọc"));
    }

    @Test
    @DisplayName("PUT /read-all – chưa xác thực → 401")
    void markAllAsRead_unauthenticated_returns401() throws Exception {
        mockMvc.perform(put(BASE + "/read-all").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}