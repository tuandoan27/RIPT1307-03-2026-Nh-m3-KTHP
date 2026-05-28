// com/borrowapp/request/service/BorrowRequestService.java
package com.borrowapp.request.service;

import com.borrowapp.request.dto.BorrowRequestListItemResponse;
import com.borrowapp.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.List;
import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.utils.TransitionValidator;
import com.borrowapp.common.constants.Role;
import com.borrowapp.common.exception.ForbiddenException;
import com.borrowapp.request.dto.BorrowRequestDetailResponse;
import com.borrowapp.equipment.entity.Equipment;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.request.dto.BorrowRequestResponse;
import com.borrowapp.request.dto.CreateBorrowRequestRequest;
import com.borrowapp.request.dto.RejectRequestBody;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public BorrowRequestResponse createRequest(CreateBorrowRequestRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (user.isLocked()) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin");
        }

        Equipment equipment = equipmentRepository
                .findByIdAndIsDeletedFalse(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị với id: " + request.getEquipmentId()));

        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new BadRequestException("Ngày bắt đầu không được là ngày trong quá khứ");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        long overlappingCount = borrowRequestRepository
                .countByEquipmentIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        equipment.getId(),
                        RequestStatus.APPROVED,
                        request.getEndDate(),
                        request.getStartDate()
                );

        if (overlappingCount >= equipment.getTotalQuantity()) {
            throw new BadRequestException(
                    "Thiết bị đã hết số lượng trong khoảng thời gian này");
        }

        BorrowRequest borrowRequest = BorrowRequest.builder()
                .user(user)
                .equipment(equipment)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .build();

        return BorrowRequestResponse.fromEntity(borrowRequestRepository.save(borrowRequest));
    }

    @Transactional
    public void approveRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.APPROVED)) {
            throw new BadRequestException(
                    "Không thể duyệt yêu cầu ở trạng thái " + request.getStatus()
            );
        }

        Equipment equipment = equipmentRepository.findByIdAndIsDeletedFalse(request.getEquipment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị không tồn tại hoặc đã bị xóa"));

        Long approvedOverlap = borrowRequestRepository.countApprovedOverlap(
                equipment.getId(),
                RequestStatus.APPROVED,
                request.getStartDate(),
                request.getEndDate()
        );

        if (approvedOverlap >= equipment.getTotalQuantity()) {
            throw new BadRequestException("Thiết bị đã được mượn hết trong khoảng thời gian này");
        }

        if (equipment.getAvailableQuantity() <= 0) {
            throw new BadRequestException("Thiết bị không còn khả dụng");
        }

        request.setStatus(RequestStatus.APPROVED);
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() - 1);

        borrowRequestRepository.save(request);
        equipmentRepository.save(equipment);
    }

    public void rejectRequest(Long requestId, RejectRequestBody body) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.REJECTED)) {
            throw new BadRequestException(
                    "Không thể từ chối yêu cầu ở trạng thái " + request.getStatus()
            );
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setReason(body.getReason());
        borrowRequestRepository.save(request);
    }

    @Transactional
    public void returnRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.RETURNED)) {
            throw new BadRequestException(
                    "Không thể xác nhận trả thiết bị ở trạng thái " + request.getStatus()
            );
        }

        Equipment equipment = equipmentRepository.findByIdAndIsDeletedFalse(request.getEquipment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị không tồn tại hoặc đã bị xóa"));

        request.setStatus(RequestStatus.RETURNED);
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + 1);

        borrowRequestRepository.save(request);
        equipmentRepository.save(equipment);
    }
    public PageResponse<BorrowRequestListItemResponse> getMyRequests(int page, int pageSize, RequestStatus status) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
 
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
 
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
 
        Page<BorrowRequest> resultPage = (status != null)
                ? borrowRequestRepository.findByUserIdAndStatus(user.getId(), status, pageable)
                : borrowRequestRepository.findByUserId(user.getId(), pageable);
 
        List<BorrowRequestListItemResponse> items = resultPage.getContent().stream()
                .map(BorrowRequestListItemResponse::fromEntity)
                .toList();

        return PageResponse.<BorrowRequestListItemResponse>builder()
                .items(items)
                .total(resultPage.getTotalElements())
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    public BorrowRequestDetailResponse getRequestById(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (currentUser.getRole() == Role.STUDENT
                && !request.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Bạn không có quyền xem yêu cầu này");
        }

        return BorrowRequestDetailResponse.fromEntity(request);
    }
    public PageResponse<BorrowRequestListItemResponse> getAllRequests(
        int page, int pageSize, RequestStatus status, String keyword) {

    String normalizedKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

    Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

    Page<BorrowRequest> resultPage = borrowRequestRepository
            .findAllWithFilter(status, normalizedKeyword, pageable);

    List<BorrowRequestListItemResponse> items = resultPage.getContent().stream()
            .map(BorrowRequestListItemResponse::fromEntity)
            .toList();

    return PageResponse.<BorrowRequestListItemResponse>builder()
            .items(items)
            .total(resultPage.getTotalElements())
            .page(page)
            .pageSize(pageSize)
            .build();
    }
}