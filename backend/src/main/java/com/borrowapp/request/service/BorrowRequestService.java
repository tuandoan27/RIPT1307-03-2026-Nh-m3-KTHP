// com/borrowapp/request/service/BorrowRequestService.java
package com.borrowapp.request.service;

import com.borrowapp.activity.util.LoggingHelper;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.common.constants.Role;
import com.borrowapp.common.exception.BadRequestException;
import com.borrowapp.common.exception.ForbiddenException;
import com.borrowapp.common.exception.ResourceNotFoundException;
import com.borrowapp.common.response.PageResponse;
import com.borrowapp.common.utils.TransitionValidator;
import com.borrowapp.equipment.entity.Equipment;
import com.borrowapp.equipment.repository.EquipmentRepository;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.service.NotificationService;
import com.borrowapp.request.dto.BorrowRequestDetailResponse;
import com.borrowapp.request.dto.BorrowRequestListItemResponse;
import com.borrowapp.request.dto.BorrowRequestResponse;
import com.borrowapp.request.dto.CreateBorrowRequestRequest;
import com.borrowapp.request.dto.RejectRequestBody;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import com.borrowapp.user.entity.User;
import com.borrowapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository     equipmentRepository;
    private final UserRepository          userRepository;
    private final NotificationService     notificationService;
    private final LoggingHelper           loggingHelper;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ══════════════════════════════════════════════════════════════════════
    // CREATE
    // ══════════════════════════════════════════════════════════════════════

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
            throw new BadRequestException("Thiết bị đã hết số lượng trong khoảng thời gian này");
        }

        BorrowRequest borrowRequest = BorrowRequest.builder()
                .user(user)
                .equipment(equipment)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .build();

        BorrowRequest saved = borrowRequestRepository.save(borrowRequest);

        try {
        notificationService.notifyOnly(
            user.getId(),
            user.getEmail(),
            NotificationType.REQUEST_CREATED,
            "Gửi yêu cầu thành công",
            "Yêu cầu mượn thiết bị \"" + equipment.getName() + "\" đã được gửi, vui lòng chờ admin duyệt.",
            "/requests/" + saved.getId()
        );
        } catch (Exception ex) {
         log.error("[CreateRequest] Notify failed | requestId={} err={}", saved.getId(), ex.getMessage());
        }

        try {
        loggingHelper.log(
            user.getId(),
            user.getFullName(),
            ActivityLogAction.CREATE_REQUEST,
            "REQUEST", saved.getId(),
            "Sinh viên tạo yêu cầu mượn thiết bị: " + equipment.getName()
        );
        } catch (Exception ex) {
                log.error("[CreateRequest] Log failed | requestId={} err={}", saved.getId(), ex.getMessage());
        }

        return BorrowRequestResponse.fromEntity(saved);
        }

    @Transactional
    public void approveRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.APPROVED)) {
            throw new BadRequestException(
                    "Không thể duyệt yêu cầu ở trạng thái " + request.getStatus());
        }

        Equipment equipment = equipmentRepository
                .findByIdAndIsDeletedFalse(request.getEquipment().getId())
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

        User actor   = currentActor();
        User borrower = request.getUser();

        try {
            notificationService.sendAndNotify(
                    borrower.getId(),
                    borrower.getEmail(),
                    NotificationType.REQUEST_APPROVED,
                    "Yêu cầu mượn thiết bị đã được duyệt",
                    String.format("Yêu cầu mượn \"%s\" của bạn (từ %s đến %s) đã được duyệt.",
                            equipment.getName(),
                            request.getStartDate().format(DATE_FMT),
                            request.getEndDate().format(DATE_FMT)),
                    "/requests/" + request.getId(),
                    "[BorrowApp] Yêu cầu mượn đã được duyệt",
                    buildApprovedEmailHtml(borrower, equipment, request)
            );
        } catch (Exception ex) {
            log.error("[ApproveRequest] Notify failed | requestId={} err={}", requestId, ex.getMessage());
        }

        try {
            loggingHelper.log(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getFullName() : "SYSTEM",
                    ActivityLogAction.APPROVE_REQUEST,
                    "REQUEST", request.getId(),
                    buildRequestDetail(request, equipment, borrower, null)
            );
        } catch (Exception ex) {
            log.error("[ApproveRequest] Log failed | requestId={} err={}", requestId, ex.getMessage());
        }
    }


    @Transactional
    public void rejectRequest(Long requestId, RejectRequestBody body) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.REJECTED)) {
            throw new BadRequestException(
                    "Không thể từ chối yêu cầu ở trạng thái " + request.getStatus());
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setReason(body.getReason());
        borrowRequestRepository.save(request);

        User actor     = currentActor();
        User borrower  = request.getUser();
        Equipment equipment = request.getEquipment();
        String reason  = body.getReason();

        try {
            notificationService.sendAndNotify(
                    borrower.getId(),
                    borrower.getEmail(),
                    NotificationType.REQUEST_REJECTED,
                    "Yêu cầu mượn thiết bị đã bị từ chối",
                    String.format("Yêu cầu mượn \"%s\" của bạn đã bị từ chối. Lý do: %s",
                            equipment.getName(),
                            reason != null && !reason.isBlank() ? reason : "(không có)"),
                    "/requests/" + request.getId(),
                    "[BorrowApp] Yêu cầu mượn bị từ chối",
                    buildRejectedEmailHtml(borrower, equipment, request, reason)
            );
        } catch (Exception ex) {
            log.error("[RejectRequest] Notify failed | requestId={} err={}", requestId, ex.getMessage());
        }

        try {
            loggingHelper.log(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getFullName() : "SYSTEM",
                    ActivityLogAction.REJECT_REQUEST,
                    "REQUEST", request.getId(),
                    buildRequestDetail(request, equipment, borrower, reason)
            );
        } catch (Exception ex) {
            log.error("[RejectRequest] Log failed | requestId={} err={}", requestId, ex.getMessage());
        }
    }


    @Transactional
    public void returnRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy yêu cầu mượn"));

        if (!TransitionValidator.isValidTransition(request.getStatus(), RequestStatus.RETURNED)) {
            throw new BadRequestException(
                    "Không thể xác nhận trả thiết bị ở trạng thái " + request.getStatus());
        }

        Equipment equipment = equipmentRepository
                .findByIdAndIsDeletedFalse(request.getEquipment().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Thiết bị không tồn tại hoặc đã bị xóa"));

        request.setStatus(RequestStatus.RETURNED);
        equipment.setAvailableQuantity(equipment.getAvailableQuantity() + 1);

        borrowRequestRepository.save(request);
        equipmentRepository.save(equipment);

        User actor   = currentActor();
        User borrower = request.getUser();
        try {
            loggingHelper.log(
                    actor != null ? actor.getId() : null,
                    actor != null ? actor.getFullName() : "SYSTEM",
                    ActivityLogAction.RETURN_REQUEST,
                    "REQUEST", request.getId(),
                    buildRequestDetail(request, equipment, borrower, null)
            );
        } catch (Exception ex) {
            log.error("[ReturnRequest] Log failed | requestId={} err={}", requestId, ex.getMessage());
        }
    }

    public PageResponse<BorrowRequestListItemResponse> getMyRequests(
            int page, int pageSize, RequestStatus status) {

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

    private User currentActor() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || auth.getName() == null) return null;
            return userRepository.findByEmail(auth.getName()).orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private Map<String, Object> buildRequestDetail(BorrowRequest request,
                                                   Equipment equipment,
                                                   User borrower,
                                                   String reason) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("requestId",     request.getId());
        m.put("status",        request.getStatus());
        m.put("borrowerId",    borrower.getId());
        m.put("borrowerEmail", borrower.getEmail());
        m.put("borrowerName",  borrower.getFullName());
        m.put("equipmentId",   equipment.getId());
        m.put("equipmentName", equipment.getName());
        m.put("startDate",     request.getStartDate());
        m.put("endDate",       request.getEndDate());
        if (reason != null) m.put("reason", reason);
        return m;
    }

    private String buildApprovedEmailHtml(User borrower, Equipment equipment, BorrowRequest request) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:14px;color:#222\">"
             + "<h2 style=\"color:#2e7d32\">Yêu cầu mượn đã được duyệt</h2>"
             + "<p>Chào <b>" + esc(borrower.getFullName()) + "</b>,</p>"
             + "<p>Yêu cầu mượn thiết bị của bạn đã được duyệt với thông tin sau:</p>"
             + "<ul>"
             + "<li><b>Thiết bị:</b> " + esc(equipment.getName()) + "</li>"
             + "<li><b>Thời gian:</b> " + request.getStartDate().format(DATE_FMT)
             + " → " + request.getEndDate().format(DATE_FMT) + "</li>"
             + "</ul>"
             + "<p>Vui lòng đến nhận thiết bị đúng thời gian. Cảm ơn bạn!</p>"
             + "<hr/><p style=\"font-size:12px;color:#888\">Email tự động từ hệ thống BorrowApp.</p>"
             + "</div>";
    }

    private String buildRejectedEmailHtml(User borrower, Equipment equipment,
                                          BorrowRequest request, String reason) {
        return "<div style=\"font-family:Arial,sans-serif;font-size:14px;color:#222\">"
             + "<h2 style=\"color:#c62828\">Yêu cầu mượn bị từ chối</h2>"
             + "<p>Chào <b>" + esc(borrower.getFullName()) + "</b>,</p>"
             + "<p>Rất tiếc, yêu cầu mượn của bạn đã bị từ chối:</p>"
             + "<ul>"
             + "<li><b>Thiết bị:</b> " + esc(equipment.getName()) + "</li>"
             + "<li><b>Thời gian:</b> " + request.getStartDate().format(DATE_FMT)
             + " → " + request.getEndDate().format(DATE_FMT) + "</li>"
             + "<li><b>Lý do:</b> " + esc(reason != null && !reason.isBlank() ? reason : "(không có)") + "</li>"
             + "</ul>"
             + "<p>Nếu cần hỗ trợ, vui lòng liên hệ quản trị viên.</p>"
             + "<hr/><p style=\"font-size:12px;color:#888\">Email tự động từ hệ thống BorrowApp.</p>"
             + "</div>";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}