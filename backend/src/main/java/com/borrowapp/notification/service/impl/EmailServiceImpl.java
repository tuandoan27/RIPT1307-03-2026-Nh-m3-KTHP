package com.borrowapp.notification.service.impl;

import com.borrowapp.activity.util.LoggingHelper;
import com.borrowapp.common.constants.ActivityLogAction;
import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.enums.NotificationLogStatus;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.service.EmailService;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.user.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender             mailSender;
    private final NotificationLogRepository  logRepo;
    private final LoggingHelper              loggingHelper;

    /**
     * Fire-and-forget: gửi email trong thread pool riêng "emailTaskExecutor".
     * Tạo NotificationLog với status=FAILED trước khi gửi → đảm bảo
     * luôn có record để retry dù JVM crash giữa chừng.
     */
    @Async("emailTaskExecutor")
    @Override
    public void sendAsync(String to, String subject, String htmlBody) {
        // Tạo log bản ghi trước (status mặc định FAILED)
        NotificationLog nlog = logRepo.save(
                NotificationLog.builder()
                        .toEmail(to)
                        .subject(subject)
                        .body(htmlBody)
                        .status(NotificationLogStatus.FAILED)
                        .build()
        );
        doSend(nlog, to, subject, htmlBody);
    }

    /**
     * Retry một NotificationLog cụ thể – chạy bất đồng bộ.
     */
    @Async("emailTaskExecutor")
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void retryAsync(Long notificationLogId) {
        NotificationLog nlog = logRepo.findById(notificationLogId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NotificationLog not found: " + notificationLogId));

        nlog.markRetrying();
        logRepo.save(nlog);

        doSend(nlog, nlog.getToEmail(), nlog.getSubject(), nlog.getBody());

        loggingHelper.logSystem(ActivityLogAction.RETRY_EMAIL,
                "NOTIFICATION_LOG", notificationLogId,
                Map.of("to",         nlog.getToEmail(),
                       "subject",    nlog.getSubject(),
                       "retryCount", nlog.getRetryCount()));
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private void doSend(NotificationLog nlog,
                        String to, String subject, String htmlBody) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // html = true
            mailSender.send(mime);

            nlog.markSuccess();
            logRepo.save(nlog);

            log.info("[Email] Sent OK | to={} subject={}", to, subject);

        } catch (Exception ex) {
            nlog.incrementRetry(truncate(ex.getMessage(), 500));
            logRepo.save(nlog);
            log.error("[Email] FAILED | to={} subject={} retryCount={} err={}",
                    to, subject, nlog.getRetryCount(), ex.getMessage());
            // ❌ Không re-throw – fire and forget
        }
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    // ─── Template helpers – delegate to sendAsync via notificationService ─────
    // Note: these are delegated to the richer EmailServiceImpl in service package
    // when called from schedulers. This impl delegates to sendAsync for compatibility.

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    @Async("emailTaskExecutor")
    public void sendDueSoonReminder(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Nhắc nhở: Sắp đến hạn trả thiết bị";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Thiết bị <b>" + request.getEquipment().getName() + "</b> của bạn sẽ hết hạn vào <b>"
                + request.getEndDate().format(DATE_FMT) + "</b>. Vui lòng trả đúng hạn.</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendOverdueWarning(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Cảnh báo: Quá hạn trả thiết bị";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Thiết bị <b>" + request.getEquipment().getName() + "</b> đã QUÁ HẠN trả (hạn: "
                + request.getEndDate().format(DATE_FMT) + "). Tài khoản đã bị ghi điểm phạt.</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendRequestApproved(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Yêu cầu mượn đã được duyệt";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Yêu cầu mượn <b>" + request.getEquipment().getName() + "</b> đã được duyệt.<br/>"
                + "Thời gian: " + request.getStartDate().format(DATE_FMT)
                + " → " + request.getEndDate().format(DATE_FMT) + "</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    @Async("emailTaskExecutor")
    public void sendRequestRejected(User user, BorrowRequest request, String reason) {
        String subject = "[BorrowApp] Yêu cầu mượn bị từ chối";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Yêu cầu mượn <b>" + request.getEquipment().getName() + "</b> đã bị từ chối.<br/>"
                + "Lý do: " + (reason != null && !reason.isBlank() ? reason : "(không có)") + "</p>";
        sendAsync(user.getEmail(), subject, html);
    }
}
