// com/borrowapp/user/dto/UserProfileResponse.java
package com.borrowapp.user.dto;

import com.borrowapp.common.constants.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {
    private String fullName;
    private String email;
    private Role role;
    private Integer penaltyPoint;
    private Boolean isLocked;
    private LocalDateTime createdAt;
}