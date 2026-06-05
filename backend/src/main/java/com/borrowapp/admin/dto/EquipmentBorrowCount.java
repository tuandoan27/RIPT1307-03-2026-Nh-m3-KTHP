// com/borrowapp/admin/dto/EquipmentBorrowCount.java
package com.borrowapp.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentBorrowCount {
    private Long equipmentId;
    private String equipmentName;
    private Long count;
}