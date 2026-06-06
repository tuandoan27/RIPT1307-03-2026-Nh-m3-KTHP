package com.borrowapp.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdjustPenaltyRequest {

    /** Số điểm thay đổi — dương để cộng, âm để trừ */
    @NotNull(message = "delta không được để trống")
    private Integer delta;

    @NotBlank(message = "Lý do không được để trống")
    private String reason;
}