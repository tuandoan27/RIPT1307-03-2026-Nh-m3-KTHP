// com/borrowapp/equipment/entity/Equipment.java
package com.borrowapp.equipment.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "equipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Min(value = 0, message = "Tổng số lượng không được âm")
    @Column(nullable = false)
    private Integer totalQuantity;

    @Min(value = 0, message = "Số lượng khả dụng không được âm")
    @Column(nullable = false)
    private Integer availableQuantity;

    @Column
    private String imageUrl;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}