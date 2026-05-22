// com/borrowapp/auth/service/AuthService.java
package com.borrowapp.auth.service;

import com.borrowapp.auth.dto.*;
import com.borrowapp.auth.util.JwtUtil;
import com.borrowapp.common.constants.Role;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        if (userRepository.existsByStudentCode(request.getStudentCode())) {
            throw new BadRequestException("Mã sinh viên đã được sử dụng");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .studentCode(request.getStudentCode())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email hoặc mật khẩu không đúng"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Email hoặc mật khẩu không đúng");
        }
        if (user.isLocked()) {
            throw new BadRequestException("Tài khoản đã bị khóa, vui lòng liên hệ admin");
        }
        if (user.isDeleted()) {
            throw new BadRequestException("Tài khoản không tồn tại");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return LoginResponse.builder()
                .token(token)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
        return UserResponse.fromEntity(user);
    }
}