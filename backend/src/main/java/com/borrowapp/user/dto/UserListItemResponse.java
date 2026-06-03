package com.borrowapp.user.dto;

import com.borrowapp.common.constants.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserListItemResponse {
    private Long id;
    private String fullName;
    private String studentCode;
    private String email;
    private Role role;
    private int penaltyPoint;
    private boolean isLocked;
    private LocalDateTime createdAt;
}