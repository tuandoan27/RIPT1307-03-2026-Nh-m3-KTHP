// com/borrowapp/equipment/dto/EquipmentListResponse.java
package com.borrowapp.equipment.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class EquipmentListResponse {

    private List<EquipmentListItemResponse> items;
    private Long total;
    private Integer page;
    private Integer pageSize;
}