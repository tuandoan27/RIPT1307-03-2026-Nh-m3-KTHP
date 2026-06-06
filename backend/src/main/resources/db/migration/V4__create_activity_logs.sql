-- ============================================================
-- V4__create_activity_logs.sql
-- Module 2.1 – Activity Log System
-- ============================================================

CREATE TABLE activity_logs
(
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT,                          -- NULL = SYSTEM action
    user_name   VARCHAR(100),
    action      VARCHAR(50)  NOT NULL,
    target_type VARCHAR(50),
    target_id   BIGINT,
    detail      TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    -- Soft reference – không FOREIGN KEY cứng để log không bị xóa theo user
    CONSTRAINT chk_action_not_empty CHECK (action <> '')
);

-- ── Indexes ──────────────────────────────────────────────────
CREATE INDEX idx_activity_user_id    ON activity_logs (user_id);
CREATE INDEX idx_activity_action     ON activity_logs (action);
CREATE INDEX idx_activity_target     ON activity_logs (target_type, target_id);
CREATE INDEX idx_activity_created_at ON activity_logs (created_at DESC);

-- !! Không tạo policy xóa – log là bất biến
-- !! Nếu cần archiving, dùng partition by range(created_at)
