// com/borrowapp/equipment/repository/EquipmentRepository.java
package com.borrowapp.equipment.repository;

import com.borrowapp.equipment.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findAllByIsDeletedFalse();

    Optional<Equipment> findByIdAndIsDeletedFalse(Long id);

    // Dùng khi không có keyword
    Page<Equipment> findByIsDeletedFalse(Pageable pageable);

    // Dùng khi có keyword — ignore case
    Page<Equipment> findByIsDeletedFalseAndNameContainingIgnoreCase(String keyword, Pageable pageable);

    Long countByIsDeletedFalse();
}