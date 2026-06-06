package com.borrowapp.notification.service;

import com.borrowapp.request.entity.BorrowRequest;
import com.borrowapp.user.entity.User;

public interface EmailService {

    
    void sendAsync(String to, String subject, String htmlBody);

    void retryAsync(Long notificationLogId);


    void sendDueSoonReminder(User user, BorrowRequest request);

    void sendOverdueWarning(User user, BorrowRequest request);

    void sendRequestApproved(User user, BorrowRequest request);

    void sendRequestRejected(User user, BorrowRequest request, String reason);
}