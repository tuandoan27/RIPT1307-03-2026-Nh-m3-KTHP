package com.borrowapp.request.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RejectRequestBody {

    @NotBlank(message = "Lý do từ chối không được để trống")
    private String reason;
}