package com.borrowapp.user.service;

import com.borrowapp.common.constants.Role;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.dto.UserDetailResponse;
import com.borrowapp.user.dto.UserListItemResponse;
import com.borrowapp.user.dto.UserRequestHistoryItem;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import com.borrowapp.user.repository.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BorrowRequestRepository borrowRequestRepository;


    @Transactional(readOnly = true)
    public PageResponse<UserListItemResponse> getUsers(
            String search,
            Role role,
            Boolean isLocked,
            int page,
            int pageSize
    ) {
        Pageable pageable = PageRequest.of(
                page - 1,
                pageSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

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
                0, Integer.MAX_VALUE,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
 
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

        if (user.isLocked()) {
            throw new BadRequestException("Tài khoản này đã bị khóa.");
        }

        user.setLocked(true);
        userRepository.save(user);
    }


    @Transactional
    public void unlockUser(Long id) {
        User user = findUserById(id);

        if (!user.isLocked()) {
            throw new BadRequestException("Tài khoản này chưa bị khóa.");
        }

        user.setLocked(false);
        userRepository.save(user);
    }


    @Transactional
    public void resetPenalty(Long id) {
        User user = findUserById(id);

        user.setPenaltyPoint(0);

        if (user.isLocked()) {
            user.setLocked(false);
        }

        userRepository.save(user);
    }

    // ─── Helpers 

    private User findUserById(Long id) {
        return userRepository.findById(id)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với id: " + id));
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