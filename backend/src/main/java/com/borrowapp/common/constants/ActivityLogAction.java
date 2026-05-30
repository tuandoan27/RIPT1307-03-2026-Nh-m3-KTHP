package com.borrowapp.common.constants;

public enum ActivityLogAction {

    // Auth
    USER_REGISTER,
    USER_LOGIN,

    // Equipment
    EQUIPMENT_CREATED,
    EQUIPMENT_UPDATED,
    EQUIPMENT_DELETED,       // soft delete
    EQUIPMENT_STOCK_UPDATED,

    // Request
    REQUEST_CREATED,
    REQUEST_APPROVED,
    REQUEST_REJECTED,
    REQUEST_RETURNED,
    REQUEST_MARKED_OVERDUE,  // bởi cron job

    // Penalty & Account
    PENALTY_ADDED,
    PENALTY_ADJUSTED,        // admin điều chỉnh thủ công
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,

    // Notification
    EMAIL_SENT,
    EMAIL_FAILED,
    RETRY_EMAIL,             // retry gửi lại email FAILED
    NOTIFICATION_CREATED
}
