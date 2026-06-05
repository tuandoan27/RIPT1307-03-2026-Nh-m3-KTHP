// com/borrowapp/admin/dto/MonthlyBorrowCount.java
package com.borrowapp.admin.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyBorrowCount {
    private Integer month;
    private Long count;
}