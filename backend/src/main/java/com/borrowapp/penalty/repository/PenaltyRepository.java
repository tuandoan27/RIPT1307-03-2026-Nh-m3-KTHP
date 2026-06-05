// com/borrowapp/penalty/repository/PenaltyRepository.java
package com.borrowapp.penalty.repository;

import com.borrowapp.penalty.entity.Penalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PenaltyRepository extends JpaRepository<Penalty, Long> {

    List<Penalty> findByUserIdOrderByCreatedAtDesc(Long userId);
}