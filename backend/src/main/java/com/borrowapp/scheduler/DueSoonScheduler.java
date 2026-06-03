package com.borrowapp.scheduler;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.service.NotificationService;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.request.repository.BorrowRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DueSoonScheduler {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BorrowRequestRepository borrowRequestRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * *")
    public void processDueSoonRequests() {
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        List<BorrowRequest> dueSoonList = borrowRequestRepository.findDueSoonRequests(
                RequestStatus.APPROVED, today, tomorrow
        );

        if (dueSoonList.isEmpty()) {
            log.info("[DueSoonScheduler] Không có request sắp đến hạn.");
            return;
        }

        log.info("[DueSoonScheduler] Bắt đầu gửi nhắc hạn cho {} request.", dueSoonList.size());

        int sentCount = 0;

        for (BorrowRequest request : dueSoonList) {
            try {
                sendDueSoonNotification(request);
                sentCount++;
            } catch (Exception e) {
                log.error("[DueSoonScheduler] Lỗi khi gửi nhắc cho request id={}: {}",
                        request.getId(), e.getMessage());
            }
        }

        log.info("[DueSoonScheduler] Hoàn tất: {}/{} notification đã gửi.",
                sentCount, dueSoonList.size());
    }

    private void sendDueSoonNotification(BorrowRequest request) {
        String equipmentName = request.getEquipment().getName();
        String endDate = request.getEndDate().format(DATE_FORMAT);
        Long userId = request.getUser().getId();
        String userEmail = request.getUser().getEmail();
        String userName = request.getUser().getFullName();

        String inAppMessage = String.format(
                "Yêu cầu mượn \"%s\" sẽ hết hạn vào ngày %s. Vui lòng trả đúng hạn nhé.",
                equipmentName, endDate
        );

        String emailHtmlBody = String.format("""
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h3>Xin chào %s,</h3>
                    <p>Đây là nhắc nhở từ hệ thống BorrowApp.</p>
                    <p>Yêu cầu mượn thiết bị <strong>%s</strong> của bạn sẽ hết hạn vào ngày <strong>%s</strong>.</p>
                    <p>Vui lòng trả thiết bị đúng hạn để tránh bị tính điểm phạt.</p>
                    <br/>
                    <p>Trân trọng,<br/>BorrowApp</p>
                </div>
                """, userName, equipmentName, endDate);

        notificationService.sendAndNotify(
                userId,
                userEmail,
                NotificationType.REQUEST_DUE_SOON,
                "Sắp đến hạn trả thiết bị",
                inAppMessage,
                "/requests/" + request.getId(),
                "[BorrowApp] Sắp đến hạn trả thiết bị",
                emailHtmlBody
        );

        log.info("[DueSoonScheduler] Đã gửi nhắc cho user id={}, request id={}, hạn trả={}.",
                userId, request.getId(), endDate);
    }
}