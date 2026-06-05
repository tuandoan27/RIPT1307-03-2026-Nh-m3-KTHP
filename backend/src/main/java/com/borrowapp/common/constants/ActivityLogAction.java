package com.borrowapp.common.constants;

public enum ActivityLogAction {
    // Device
    CREATE_DEVICE, UPDATE_DEVICE, DELETE_DEVICE,
    // Request
    CREATE_REQUEST, APPROVE_REQUEST, REJECT_REQUEST,
    RETURN_REQUEST, MARK_OVERDUE,
    // User
    LOGIN, LOGOUT, UPDATE_PROFILE, CHANGE_PASSWORD,
    // Notification
    SEND_EMAIL, RETRY_EMAIL,
    // Admin
    EXPORT_DATA, IMPORT_DATA
}
