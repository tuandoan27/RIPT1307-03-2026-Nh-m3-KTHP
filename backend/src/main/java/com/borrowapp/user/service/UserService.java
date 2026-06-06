package com.borrowapp.user.service;

import com.borrowapp.activity.service.ActivityLogService;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.constants.Role;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.penalty.entity.Penalty;
import com.borrowapp.penalty.entity.PenaltyType;
import com.borrowapp.penalty.repository.PenaltyRepository;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.dto.*;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import com.borrowapp.user.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository          userRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final PasswordEncoder         passwordEncoder;
    private final PenaltyRepository       penaltyRepository;
    private final ActivityLogService      activityLogService;

    @Transactional(readOnly = true)
    public PageResponse<UserListItemResponse> getUsers(
            String search, Role role, Boolean isLocked, int page, int pageSize) {
        Pageable pageable = PageRequest.of(
                page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<User> spec = UserSpecification.filter(search, role, isLocked);
        Page<User> result = userRepository.findAll(spec, pageable);
        return PageResponse.<UserListItemResponse>builder()
                .items(result.getContent().stream().map(this::toListItemResponse).toList())
                .total(result.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserDetail(Long id) {
        User user = findUserById(id);
        Pageable sortByCreatedAtDesc = PageRequest.of(
                0, Integer.MAX_VALUE, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<UserRequestHistoryItem> requests = borrowRequestRepository
                .findByUserId(id, sortByCreatedAtDesc)
                .getContent()
                .stream()
                .map(r -> UserRequestHistoryItem.builder()
                        .id(r.getId())
                        .equipmentName(r.getEquipment().getName())
                        .startDate(r.getStartDate())
                        .endDate(r.getEndDate())
                        .status(r.getStatus())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();
        return UserDetailResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .studentCode(user.getStudentCode())
                .email(user.getEmail())
                .role(user.getRole())
                .penaltyPoint(user.getPenaltyPoint())
                .isLocked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .requests(requests)
                .build();
    }

    @Transactional
    public void lockUser(Long id) {
        User user = findUserById(id);
        if (user.isLocked()) throw new BadRequestException("Tài khoản này đã bị khóa.");
        user.setLocked(true);
        userRepository.save(user);
    }

    @Transactional
    public void unlockUser(Long id) {
        User user = findUserById(id);
        if (!user.isLocked()) throw new BadRequestException("Tài khoản này chưa bị khóa.");
        user.setLocked(false);
        userRepository.save(user);
    }

    @Transactional
    public void resetPenalty(Long id) {
        User user = findUserById(id);
        user.setPenaltyPoint(0);
        if (user.isLocked()) user.setLocked(false);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile() {
        User user = getCurrentUser();
        return UserProfileResponse.builder()
                .fullName(user.getFullName())
                .studentCode(user.getStudentCode())
                .email(user.getEmail())
                .role(user.getRole())
                .penaltyPoint(user.getPenaltyPoint())
                .isLocked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadRequestException("Mật khẩu cũ không đúng");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<PenaltyResponse> getMyPenalties() {
        User user = getCurrentUser();
        return penaltyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(p -> PenaltyResponse.builder()
                        .points(p.getPoints())
                        .reason(p.getReason())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public UserDetailResponse adjustPenalty(Long userId, AdjustPenaltyRequest request,
                                             Long adminId, String adminName) {
        User user = findUserById(userId);

        user.setPenaltyPoint(user.getPenaltyPoint() + request.getDelta());
        checkAndLockIfThresholdReached(user);
        userRepository.save(user);

        penaltyRepository.save(Penalty.builder()
                .user(user)
                .points(request.getDelta())
                .reason(request.getReason())
                .type(PenaltyType.MANUAL_ADJUSTMENT)
                .build());

        activityLogService.log(
                adminId, adminName,
                ActivityLogAction.ADJUST_PENALTY, "USER", userId,
                String.format("delta=%d, reason=%s", request.getDelta(), request.getReason())
        );

        return getUserDetail(userId);
    }

    public boolean checkAndLockIfThresholdReached(User user) {
        if (!user.isLocked() && user.getPenaltyPoint() >= 10) {
            user.setLocked(true);
            return true;
        }
        return false;
    }

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy user với id: " + id));
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
    }

    private UserListItemResponse toListItemResponse(User user) {
        return UserListItemResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .studentCode(user.getStudentCode())
                .email(user.getEmail())
                .role(user.getRole())
                .penaltyPoint(user.getPenaltyPoint())
                .isLocked(user.isLocked())
                .createdAt(user.getCreatedAt())
                .build();
    }
}