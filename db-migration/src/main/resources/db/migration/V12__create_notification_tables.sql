CREATE TABLE IF NOT EXISTS notification_service.notification (
    notification_id UUID PRIMARY KEY,
    member_id       UUID         NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    title           VARCHAR(255) NOT NULL,
    content         TEXT         NOT NULL,
    reference_id    UUID,
    reference_type  VARCHAR(50),
    is_read         BOOLEAN      NOT NULL DEFAULT false,
    created_at      TIMESTAMP    NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_notification_member_created_at
    ON notification_service.notification (member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notification_member_is_read
    ON notification_service.notification (member_id, is_read);
