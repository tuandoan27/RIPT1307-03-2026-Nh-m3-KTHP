// com/borrowapp/request/dto/CreateBorrowRequestRequest.java
package com.borrowapp.request.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateBorrowRequestRequest {

    @NotNull(message = "Thiết bị không được để trống")
    private Long equipmentId;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    private String note;
}