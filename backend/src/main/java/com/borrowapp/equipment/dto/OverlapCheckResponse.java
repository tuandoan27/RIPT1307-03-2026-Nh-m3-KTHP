package com.borrowapp.equipment.dto;

import lombok.Getter;

@Getter
public class OverlapCheckResponse {

    private final Long equipmentId;
    private final Long approvedCount;
    private final Integer totalQuantity;
    private final Boolean available;

    private OverlapCheckResponse(Long equipmentId, Long approvedCount, Integer totalQuantity) {
        this.equipmentId = equipmentId;
        this.approvedCount = approvedCount;
        this.totalQuantity = totalQuantity;
        this.available = approvedCount < totalQuantity;
    }

    public static OverlapCheckResponse of(Long equipmentId, Long approvedCount, Integer totalQuantity) {
        return new OverlapCheckResponse(equipmentId, approvedCount, totalQuantity);
    }
}