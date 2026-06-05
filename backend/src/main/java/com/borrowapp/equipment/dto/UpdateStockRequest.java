package com.borrowapp.equipment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateStockRequest {

    @NotNull(message = "Số lượng mới không được để trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer newQuantity;

    private String reason;
}