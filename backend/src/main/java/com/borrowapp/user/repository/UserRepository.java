// com/borrowapp/user/repository/UserRepository.java
package com.borrowapp.user.repository;

import com.borrowapp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByStudentCode(String studentCode);
}