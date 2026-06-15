-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V11, V12, V29, V30, V36, V49
CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE IF NOT EXISTS notification.notification (
    notification_id   UUID         NOT NULL,
    member_id         UUID         NOT NULL,
    type              VARCHAR(50)  NOT NULL,
    title             VARCHAR(255) NOT NULL,
    content           TEXT         NOT NULL,
    reference_id      UUID,
    reference_type    VARCHAR(50),
    event_id          UUID,
    trace_id          VARCHAR(100),
    status            VARCHAR(30),
    is_read           BOOLEAN      NOT NULL DEFAULT FALSE,
    status_changed_at TIMESTAMP,
    created_at        TIMESTAMP    NOT NULL,
    CONSTRAINT pk_notification PRIMARY KEY (notification_id)
);

CREATE INDEX IF NOT EXISTS idx_notification_member_created_at
    ON notification.notification (member_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notification_member_is_read
    ON notification.notification (member_id, is_read);
CREATE UNIQUE INDEX IF NOT EXISTS uq_notification_event_id
    ON notification.notification (event_id);
