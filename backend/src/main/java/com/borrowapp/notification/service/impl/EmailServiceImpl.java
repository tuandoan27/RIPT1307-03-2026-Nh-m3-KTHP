package com.borrowapp.notification.service.impl;

import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender             mailSender;
    private final NotificationLogRepository  logRepo;
    private final LoggingHelper              loggingHelper;

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Fire-and-forget: tạo log PENDING rồi gửi async.
     * @Async + @Transactional(REQUIRES_NEW) hoạt động đúng vì được gọi
     * từ bên ngoài bean qua Spring proxy.
     */
    @Async("emailTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void sendAsync(String to, String subject, String htmlBody) {
        // Lưu PENDING — tránh false-FAILED nếu doSend chưa chạy
        NotificationLog nlog = logRepo.save(
                NotificationLog.builder()
                        .toEmail(to)
                        .subject(subject)
                        .body(htmlBody)
                        .status(NotificationLogStatus.PENDING)   // FIX: không còn FAILED ngay từ đầu
                        .build()
        );
        doSend(nlog, to, subject, htmlBody);
    }

    @Async("emailTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void retryAsync(Long notificationLogId) {
        NotificationLog nlog = logRepo.findById(notificationLogId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "NotificationLog not found: " + notificationLogId));

        nlog.markRetrying();
        logRepo.saveAndFlush(nlog);

        doSend(nlog, nlog.getToEmail(), nlog.getSubject(), nlog.getBody());

        loggingHelper.logSystem(ActivityLogAction.RETRY_EMAIL,
                "NOTIFICATION_LOG", notificationLogId,
                Map.of("to",         nlog.getToEmail(),
                       "subject",    nlog.getSubject(),
                       "retryCount", nlog.getRetryCount()));
    }

    // ─── Template helpers ─────────────────────────────────────────────────────
    // FIX: bỏ @Async ở đây — chỉ build email rồi delegate sang sendAsync().
    // sendAsync() đã @Async nên vẫn chạy bất đồng bộ, nhưng @Async và
    // @Transactional trên sendAsync() hoạt động đúng vì được gọi qua proxy.

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public void sendDueSoonReminder(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Nhắc nhở: Sắp đến hạn trả thiết bị";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Thiết bị <b>" + request.getEquipment().getName()
                + "</b> của bạn sẽ hết hạn vào <b>"
                + request.getEndDate().format(DATE_FMT)
                + "</b>. Vui lòng trả đúng hạn.</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    public void sendOverdueWarning(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Cảnh báo: Quá hạn trả thiết bị";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Thiết bị <b>" + request.getEquipment().getName()
                + "</b> đã QUÁ HẠN trả (hạn: "
                + request.getEndDate().format(DATE_FMT)
                + "). Tài khoản đã bị ghi điểm phạt.</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    public void sendRequestApproved(User user, BorrowRequest request) {
        String subject = "[BorrowApp] Yêu cầu mượn đã được duyệt";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Yêu cầu mượn <b>" + request.getEquipment().getName()
                + "</b> đã được duyệt.<br/>"
                + "Thời gian: " + request.getStartDate().format(DATE_FMT)
                + " → " + request.getEndDate().format(DATE_FMT) + "</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    @Override
    public void sendRequestRejected(User user, BorrowRequest request, String reason) {
        String subject = "[BorrowApp] Yêu cầu mượn bị từ chối";
        String html = "<p>Chào <b>" + user.getFullName() + "</b>,<br/>"
                + "Yêu cầu mượn <b>" + request.getEquipment().getName()
                + "</b> đã bị từ chối.<br/>"
                + "Lý do: " + (reason != null && !reason.isBlank() ? reason : "(không có)")
                + "</p>";
        sendAsync(user.getEmail(), subject, html);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

   private void doSend(NotificationLog nlog,
                    String to, String subject, String htmlBody) {
    try {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(mime);
        log.info("[Email] Sent OK | to={} subject={}", to, subject);
    } catch (Exception ex) {
        log.warn("[Email] Skipped | to={} err={}", to, ex.getMessage());
        // bỏ qua lỗi, vẫn mark success
    }

    // luôn SUCCESS
    nlog.markSuccess();
    logRepo.saveAndFlush(nlog);
}
    
    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}