// com/borrowapp/equipment/dto/EquipmentDetailResponse.java
package com.borrowapp.equipment.dto;

import com.borrowapp.equipment.entity.Equipment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EquipmentDetailResponse {

    private Long id;
    private String name;
    private String description;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private String imageUrl;
    private LocalDateTime createdAt;

    public static EquipmentDetailResponse fromEntity(Equipment equipment) {
        return EquipmentDetailResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .totalQuantity(equipment.getTotalQuantity())
                .availableQuantity(equipment.getAvailableQuantity())
                .imageUrl(equipment.getImageUrl())
                .createdAt(equipment.getCreatedAt())
                .build();
    }
}