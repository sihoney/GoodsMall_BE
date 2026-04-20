ALTER TABLE notification.notification
    ADD COLUMN IF NOT EXISTS event_id UUID,
    ADD COLUMN IF NOT EXISTS trace_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS status VARCHAR(30),
    ADD COLUMN IF NOT EXISTS status_changed_at TIMESTAMP;

CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_event_id
    ON notification.notification (event_id);
