// com/borrowapp/user/dto/PenaltyResponse.java
package com.borrowapp.user.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PenaltyResponse {
    private Integer points;
    private String reason;
    private LocalDateTime createdAt;
}