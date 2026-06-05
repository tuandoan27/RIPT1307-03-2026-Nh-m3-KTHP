package com.borrowapp.notification.service;

import com.borrowapp.notification.entity.NotificationLog;
import com.borrowapp.notification.repository.NotificationLogRepository;
import com.borrowapp.notification.service.EmailService;
import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.user.entity.User;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final JavaMailSender mailSender;
    private final NotificationLogRepository logRepository;

    @Value("${spring.mail.username}")
    private String senderEmail;

    // ─── Low-level: HTML email + DB log ──────────────────────────────────────

    /**
     * Gửi HTML email async, ghi kết quả vào NotificationLog.
     * Status mặc định là FAILED (pessimistic default) — markSuccess() nếu gửi được.
     */
    @Async
    @Override
    public void sendAsync(String to, String subject, String htmlBody) {
        NotificationLog notifLog = NotificationLog.builder()
                .toEmail(to)
                .subject(subject)
                .body(htmlBody)
                .build();

        try {
            sendHtml(to, subject, htmlBody);
            notifLog.markSuccess();
            log.info("[Email] sendAsync OK → to={} | subject={}", to, subject);
        } catch (Exception e) {
            notifLog.setErrorMessage(e.getMessage()); // retryCount giữ nguyên 0 (chưa retry)
            log.error("[Email] sendAsync FAILED → to={} | subject={} | error={}", to, subject, e.getMessage());
        } finally {
            logRepository.save(notifLog);
        }
    }

    /**
     * Gửi lại email FAILED theo notificationLogId.
     * Flow: FAILED → RETRYING → SUCCESS | FAILED (retryCount++)
     */
    @Async
    @Override
    public void retryAsync(Long notificationLogId) {
        NotificationLog notifLog = logRepository.findById(notificationLogId).orElse(null);
        if (notifLog == null) {
            log.warn("[Email] retryAsync: NotificationLog id={} không tồn tại, bỏ qua.", notificationLogId);
            return;
        }

        notifLog.markRetrying();
        logRepository.save(notifLog); // persist RETRYING trước khi gửi

        try {
            sendHtml(notifLog.getToEmail(), notifLog.getSubject(), notifLog.getBody());
            notifLog.markSuccess();
            log.info("[Email] retryAsync OK → to={} | logId={}", notifLog.getToEmail(), notificationLogId);
        } catch (Exception e) {
            notifLog.incrementRetry(e.getMessage()); // retryCount++, status=FAILED
            log.error("[Email] retryAsync FAILED → to={} | logId={} | retryCount={} | error={}",
                    notifLog.getToEmail(), notificationLogId, notifLog.getRetryCount(), e.getMessage());
        } finally {
            logRepository.save(notifLog); // persist trạng thái cuối
        }
    }

    // ─── High-level domain methods (called from schedulers) ──────────────────

    @Async
    @Override
    public void sendDueSoonReminder(User user, BorrowRequest request) {
        sendPlainText(
                user.getEmail(),
                "[BorrowApp] Nhắc nhở: Sắp đến hạn trả thiết bị",
                buildDueSoonBody(
                        user.getFullName(),
                        request.getEquipment().getName(),
                        request.getEndDate()
                )
        );
    }

    @Async
    @Override
    public void sendOverdueWarning(User user, BorrowRequest request) {
        long daysOverdue = ChronoUnit.DAYS.between(request.getEndDate(), LocalDate.now());
        sendPlainText(
                user.getEmail(),
                "[BorrowApp] Cảnh báo: Quá hạn trả thiết bị",
                buildOverdueBody(
                        user.getFullName(),
                        request.getEquipment().getName(),
                        request.getEndDate(),
                        daysOverdue
                )
        );
    }

    @Async
    @Override
    public void sendRequestApproved(User user, BorrowRequest request) {
        sendPlainText(
                user.getEmail(),
                "[BorrowApp] Yêu cầu mượn đã được duyệt",
                buildApprovedBody(
                        user.getFullName(),
                        request.getEquipment().getName(),
                        request.getStartDate(),
                        request.getEndDate()
                )
        );
    }

    @Async
    @Override
    public void sendRequestRejected(User user, BorrowRequest request, String reason) {
        sendPlainText(
                user.getEmail(),
                "[BorrowApp] Yêu cầu mượn bị từ chối",
                buildRejectedBody(
                        user.getFullName(),
                        request.getEquipment().getName(),
                        reason
                )
        );
    }

    // ─── Private: transport ──────────────────────────────────────────────────

    /**
     * Gửi HTML email — throws exception để caller xử lý và ghi log.
     */
    private void sendHtml(String to, String subject, String htmlBody) throws MessagingException {
        var mime = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(mime, StandardCharsets.UTF_8.name());
        helper.setFrom(senderEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(mime);
    }

    /**
     * Gửi plain-text email — fire and forget.
     * Không throw exception ra ngoài; log lỗi nếu thất bại.
     */
    private void sendPlainText(String to, String subject, String body) {
        try {
            var msg = new SimpleMailMessage();
            msg.setFrom(senderEmail);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("[Email] Gửi thành công → to={} | subject={}", to, subject);
        } catch (Exception e) {
            log.error("[Email] Gửi thất bại → to={} | subject={} | error={}", to, subject, e.getMessage());
        }
    }

    // ─── Private: template builders ──────────────────────────────────────────

    private String buildDueSoonBody(String fullName, String equipmentName, LocalDate endDate) {
        return """
                Xin chào %s,

                Đây là nhắc nhở từ hệ thống BorrowApp.

                Thiết bị "%s" bạn đang mượn sẽ hết hạn vào ngày %s.
                Vui lòng trả đúng hạn để tránh bị tính điểm phạt.

                Trân trọng,
                BorrowApp
                """.formatted(fullName, equipmentName, endDate.format(DATE_FMT));
    }

    private String buildOverdueBody(String fullName, String equipmentName,
                                     LocalDate endDate, long daysOverdue) {
        return """
                Xin chào %s,

                Thiết bị "%s" mà bạn đang mượn đã QUÁ HẠN %d ngày (hạn trả: %s).
                Vui lòng liên hệ quản lý câu lạc bộ để trả thiết bị ngay.
                Tài khoản của bạn đã bị ghi điểm phạt.

                Trân trọng,
                BorrowApp
                """.formatted(fullName, equipmentName, daysOverdue, endDate.format(DATE_FMT));
    }

    private String buildApprovedBody(String fullName, String equipmentName,
                                      LocalDate startDate, LocalDate endDate) {
        return """
                Xin chào %s,

                Yêu cầu mượn thiết bị "%s" của bạn đã được DUYỆT.
                Thời gian mượn: %s → %s.

                Trân trọng,
                BorrowApp
                """.formatted(fullName, equipmentName,
                startDate.format(DATE_FMT), endDate.format(DATE_FMT));
    }

    private String buildRejectedBody(String fullName, String equipmentName, String reason) {
        return """
                Xin chào %s,

                Yêu cầu mượn thiết bị "%s" của bạn đã bị TỪ CHỐI.
                Lý do: %s.

                Trân trọng,
                BorrowApp
                """.formatted(fullName, equipmentName,
                (reason != null && !reason.isBlank()) ? reason : "Không có lý do cụ thể");
    }
}