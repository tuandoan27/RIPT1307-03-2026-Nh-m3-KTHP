package com.borrowapp.scheduler;

import com.borrowapp.common.constants.RequestStatus;
import com.borrowapp.notification.enums.NotificationType;
import com.borrowapp.notification.service.EmailService;
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
    private final EmailService emailService;

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

        log.info("[DueSoonScheduler] Bắt đầu xử lý {} request sắp đến hạn.", dueSoonList.size());

        int sentCount = 0;

        for (BorrowRequest request : dueSoonList) {
            try {
                sendDueSoonNotification(request);
                sentCount++;
            } catch (Exception e) {
                log.error("[DueSoonScheduler] Lỗi khi xử lý request id={}: {}",
                        request.getId(), e.getMessage());
            }
        }

        log.info("[DueSoonScheduler] Hoàn tất: {}/{} request đã xử lý.",
                sentCount, dueSoonList.size());
    }

    private void sendDueSoonNotification(BorrowRequest request) {
        String equipmentName = request.getEquipment().getName();
        String endDate = request.getEndDate().format(DATE_FORMAT);
        Long userId = request.getUser().getId();
        String userEmail = request.getUser().getEmail();

        String inAppMessage = String.format(
                "Yêu cầu mượn \"%s\" sẽ hết hạn vào ngày %s. Vui lòng trả đúng hạn.",
                equipmentName, endDate
        );

        notificationService.notifyOnly(
                userId,
                userEmail,
                NotificationType.REQUEST_DUE_SOON,
                "Sắp đến hạn trả thiết bị",
                inAppMessage,
                "/requests/" + request.getId()
        );

        emailService.sendDueSoonReminder(request.getUser(), request);

        log.info("[DueSoonScheduler] Đã trigger nhắc hạn → user id={} | request id={} | hạn trả={}.",
                userId, request.getId(), endDate);
    }
}