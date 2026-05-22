// com/borrowapp/request/service/BorrowRequestService.java
package com.borrowapp.request.service;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.equipment.entity.Equipment;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.request.dto.BorrowRequestResponse;
import com.borrowapp.request.dto.CreateBorrowRequestRequest;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    public BorrowRequestResponse createRequest(CreateBorrowRequestRequest request) {

        // 1. Lấy user hiện tại từ token
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        // 2. Validate tài khoản không bị khóa
        if (user.isLocked()) {
            throw new BadRequestException("Tài khoản của bạn đã bị khóa, vui lòng liên hệ admin");
        }

        // 3. Validate thiết bị tồn tại và chưa bị xóa
        Equipment equipment = equipmentRepository
                .findByIdAndIsDeletedFalse(request.getEquipmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thiết bị với id: " + request.getEquipmentId()));

        // 4. Validate ngày không phải quá khứ
        LocalDate today = LocalDate.now();
        if (request.getStartDate().isBefore(today)) {
            throw new BadRequestException("Ngày bắt đầu không được là ngày trong quá khứ");
        }

        // 5. Validate startDate <= endDate
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BadRequestException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        // 6. Validate overlap
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

        // 7. Tạo và lưu request
        BorrowRequest borrowRequest = BorrowRequest.builder()
                .user(user)
                .equipment(equipment)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .build();

        return BorrowRequestResponse.fromEntity(borrowRequestRepository.save(borrowRequest));
    }
}