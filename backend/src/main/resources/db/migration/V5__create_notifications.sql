-- ============================================================
-- V5__create_notifications.sql
-- Module 2.2 – Notification & Async Email
-- ============================================================

-- ── Bảng notification in-app (bell icon) ─────────────────────
CREATE TABLE notifications
(
    id              BIGSERIAL     PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    recipient_email VARCHAR(255)  NOT NULL,
    type            VARCHAR(40)   NOT NULL,
    title           VARCHAR(255)  NOT NULL,
    message         TEXT          NOT NULL,
    link            VARCHAR(500),
    status          VARCHAR(20)   NOT NULL DEFAULT 'UNREAD',
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    read_at         TIMESTAMPTZ,

    CONSTRAINT chk_notif_status CHECK (status IN ('UNREAD', 'READ'))
);

CREATE INDEX idx_notif_user_id    ON notifications (user_id);
CREATE INDEX idx_notif_status     ON notifications (status);
CREATE INDEX idx_notif_created_at ON notifications (created_at DESC);

-- ── Bảng log gửi email (audit trail) ─────────────────────────
CREATE TABLE notification_logs
(
    id            BIGSERIAL    PRIMARY KEY,
    to_email      VARCHAR(255) NOT NULL,
    subject       VARCHAR(500) NOT NULL,
    body          TEXT         NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'FAILED',
    retry_count   INT          NOT NULL DEFAULT 0,
    error_message TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,

    CONSTRAINT chk_nlog_status CHECK (status IN ('SUCCESS', 'FAILED', 'RETRYING'))
);

CREATE INDEX idx_nlog_status     ON notification_logs (status);
CREATE INDEX idx_nlog_to_email   ON notification_logs (to_email);
CREATE INDEX idx_nlog_created_at ON notification_logs (created_at DESC);

-- !! Không tạo policy xóa – log email là audit trail bất biến
