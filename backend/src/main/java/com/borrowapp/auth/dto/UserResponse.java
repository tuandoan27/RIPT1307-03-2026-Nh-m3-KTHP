// com/borrowapp/auth/dto/UserResponse.java
package com.borrowapp.auth.dto;

import com.borrowapp.common.constants.Role;
import com.borrowapp.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String studentCode;
    private String email;
    private Role role;
    private int penaltyPoint;
    private boolean isLocked;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .studentCode(user.getStudentCode())
                .email(user.getEmail())
                .role(user.getRole())
                .penaltyPoint(user.getPenaltyPoint())
                .isLocked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}