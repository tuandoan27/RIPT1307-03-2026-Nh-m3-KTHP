// com/borrowapp/request/service/BorrowRequestService.java
package com.borrowapp.request.service;

import com.borrowapp.activity.util.LoggingHelper;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.service.NotificationService;
import com.borrowapp.request.dto.BorrowRequestListItemResponse;
import com.borrowapp.common.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;

    // ── Integrations: Activity log + Async email/notification ──────────────
    private final LoggingHelper        loggingHelper;
    private final NotificationService  notificationService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        // ── Activity log ───────────────────────────────────────────────────
        User admin    = getCurrentUser();
        User borrower = request.getUser();
        logRequestAction(admin, ActivityLogAction.REQUEST_APPROVED, request, equipment, null);

        // ── Email + In-app notification cho người mượn (async, fire & forget) ──
        notificationService.sendAndNotify(
                borrower.getId(),
                borrower.getEmail(),
                NotificationType.REQUEST_APPROVED,
                "Yêu cầu mượn thiết bị đã được duyệt",
                "Yêu cầu mượn \"" + equipment.getName() + "\" của bạn đã được duyệt.",
                "/requests/" + request.getId(),
                "[BorrowApp] Yêu cầu mượn thiết bị đã được duyệt",
                buildApprovedEmailHtml(borrower, request, equipment)
        );
    }

    @Transactional
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

        // ── Activity log ───────────────────────────────────────────────────
        User admin     = getCurrentUser();
        User borrower  = request.getUser();
        Equipment eqp  = request.getEquipment();
        logRequestAction(admin, ActivityLogAction.REQUEST_REJECTED, request, eqp, body.getReason());

        // ── Email + In-app notification cho người mượn ──
        notificationService.sendAndNotify(
                borrower.getId(),
                borrower.getEmail(),
                NotificationType.REQUEST_REJECTED,
                "Yêu cầu mượn thiết bị bị từ chối",
                "Yêu cầu mượn \"" + eqp.getName() + "\" của bạn đã bị từ chối."
                        + (body.getReason() != null ? " Lý do: " + body.getReason() : ""),
                "/requests/" + request.getId(),
                "[BorrowApp] Yêu cầu mượn thiết bị bị từ chối",
                buildRejectedEmailHtml(borrower, request, eqp, body.getReason())
        );
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

        // ── Activity log (return không gửi email theo yêu cầu) ─────────────
        User admin = getCurrentUser();
        logRequestAction(admin, ActivityLogAction.REQUEST_RETURNED, request, equipment, null);
    }

    // ─── Helpers: lấy user hiện tại, ghi log, build email body ───────────────

    /**
     * Lấy User hiện tại từ SecurityContext (principal name = email).
     * Trả về null nếu không xác định được – để không phá luồng chính khi
     * scheduler/system gọi service.
     */
    private User getCurrentUser() {
        try {
            String email = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();
            return userRepository.findByEmail(email).orElse(null);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Ghi activity log cho một hành động trên BorrowRequest.
     * Detail là JSON gọn gồm borrower, equipment, status, từ/đến và lý do (nếu có).
     */
    private void logRequestAction(User actor,
                                  ActivityLogAction action,
                                  BorrowRequest request,
                                  Equipment equipment,
                                  String reason) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("requestId",   request.getId());
        detail.put("borrowerId",  request.getUser().getId());
        detail.put("borrower",    request.getUser().getEmail());
        detail.put("equipmentId", equipment.getId());
        detail.put("equipment",   equipment.getName());
        detail.put("startDate",   request.getStartDate().toString());
        detail.put("endDate",     request.getEndDate().toString());
        detail.put("status",      request.getStatus().name());
        if (reason != null && !reason.isBlank()) {
            detail.put("reason", reason);
        }

        if (actor != null) {
            loggingHelper.log(actor.getId(), actor.getFullName(),
                    action, "REQUEST", request.getId(), detail);
        } else {
            loggingHelper.logSystem(action, "REQUEST", request.getId(), detail);
        }
    }

    private String buildApprovedEmailHtml(User borrower, BorrowRequest request, Equipment equipment) {
        return ""
            + "<div style=\"font-family:Arial,sans-serif;color:#222\">"
            + "<h2 style=\"color:#2e7d32\">Yêu cầu mượn thiết bị đã được DUYỆT ✅</h2>"
            + "<p>Xin chào <b>" + safe(borrower.getFullName()) + "</b>,</p>"
            + "<p>Yêu cầu mượn thiết bị của bạn vừa được duyệt với các thông tin sau:</p>"
            + "<ul>"
            + "<li><b>Thiết bị:</b> " + safe(equipment.getName()) + "</li>"
            + "<li><b>Ngày bắt đầu:</b> " + request.getStartDate().format(DATE_FMT) + "</li>"
            + "<li><b>Ngày kết thúc:</b> " + request.getEndDate().format(DATE_FMT) + "</li>"
            + "<li><b>Mã yêu cầu:</b> #" + request.getId() + "</li>"
            + "</ul>"
            + "<p>Vui lòng đến nhận thiết bị đúng hạn và giữ gìn cẩn thận.</p>"
            + "<p style=\"color:#888;font-size:12px\">Email tự động từ hệ thống BorrowApp – vui lòng không trả lời.</p>"
            + "</div>";
    }

    private String buildRejectedEmailHtml(User borrower, BorrowRequest request,
                                          Equipment equipment, String reason) {
        return ""
            + "<div style=\"font-family:Arial,sans-serif;color:#222\">"
            + "<h2 style=\"color:#c62828\">Yêu cầu mượn thiết bị bị TỪ CHỐI ❌</h2>"
            + "<p>Xin chào <b>" + safe(borrower.getFullName()) + "</b>,</p>"
            + "<p>Rất tiếc, yêu cầu mượn thiết bị của bạn đã bị từ chối:</p>"
            + "<ul>"
            + "<li><b>Thiết bị:</b> " + safe(equipment.getName()) + "</li>"
            + "<li><b>Ngày bắt đầu:</b> " + request.getStartDate().format(DATE_FMT) + "</li>"
            + "<li><b>Ngày kết thúc:</b> " + request.getEndDate().format(DATE_FMT) + "</li>"
            + "<li><b>Mã yêu cầu:</b> #" + request.getId() + "</li>"
            + (reason != null && !reason.isBlank()
                    ? "<li><b>Lý do:</b> " + safe(reason) + "</li>"
                    : "")
            + "</ul>"
            + "<p>Nếu cần thêm thông tin, vui lòng liên hệ quản trị viên.</p>"
            + "<p style=\"color:#888;font-size:12px\">Email tự động từ hệ thống BorrowApp – vui lòng không trả lời.</p>"
            + "</div>";
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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