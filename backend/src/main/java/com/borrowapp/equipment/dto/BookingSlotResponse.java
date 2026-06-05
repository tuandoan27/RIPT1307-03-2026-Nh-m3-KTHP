package com.borrowapp.equipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class BookingSlotResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long quantity;
}