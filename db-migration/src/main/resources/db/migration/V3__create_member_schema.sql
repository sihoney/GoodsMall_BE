-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V5, V6, V17, V23, V24

CREATE SCHEMA IF NOT EXISTS member;

CREATE TABLE IF NOT EXISTS member.member (
    member_id          UUID          NOT NULL,
    email              VARCHAR(255)  NOT NULL,
    password           VARCHAR(255)  NOT NULL,
    nickname           VARCHAR(100)  NOT NULL,
    phone              VARCHAR(50),
    address            VARCHAR(255),
    profile_image_key  VARCHAR(255),
    role               VARCHAR(20)   NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uq_member_email UNIQUE (email),
    CONSTRAINT ck_member_status
        CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'DELETED'))
);

CREATE TABLE IF NOT EXISTS member.seller (
    seller_id    UUID         NOT NULL,
    member_id    UUID         NOT NULL,
    bank_name    VARCHAR(100),
    account      VARCHAR(100),
    approved_at  TIMESTAMP,
    CONSTRAINT pk_seller PRIMARY KEY (seller_id),
    CONSTRAINT uq_seller_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS member.member_report (
    report_id           UUID          NOT NULL,
    reporter_id         UUID          NOT NULL,
    reported_member_id  UUID          NOT NULL,
    reason              VARCHAR(1000) NOT NULL,
    report_type         VARCHAR(20)   NOT NULL,
    status              VARCHAR(20)   NOT NULL,
    review_comment      VARCHAR(1000),
    reviewed_by         UUID,
    reviewed_at         TIMESTAMP,
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member_report PRIMARY KEY (report_id)
);

CREATE INDEX IF NOT EXISTS idx_member_report_reporter_created_at
    ON member.member_report (reporter_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_member_report_reported_member_created_at
    ON member.member_report (reported_member_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_member_report_created_at
    ON member.member_report (created_at DESC);

CREATE TABLE IF NOT EXISTS member.member_restriction (
    restriction_id    UUID          NOT NULL,
    member_id         UUID          NOT NULL,
    admin_id          UUID          NOT NULL,
    reason            VARCHAR(1000) NOT NULL,
    restriction_type  VARCHAR(20)   NOT NULL,
    duration_hours    INTEGER       NOT NULL,
    end_at            TIMESTAMP     NOT NULL,
    is_active         BOOLEAN       NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP,
    CONSTRAINT pk_member_restriction PRIMARY KEY (restriction_id)
);

CREATE INDEX IF NOT EXISTS idx_member_restriction_member_type_active_end_at
    ON member.member_restriction (member_id, restriction_type, is_active, end_at DESC);
CREATE INDEX IF NOT EXISTS idx_member_restriction_member_created_at
    ON member.member_restriction (member_id, created_at DESC);

CREATE TABLE IF NOT EXISTS member.member_oauth_account (
    oauth_account_id   UUID         NOT NULL,
    member_id          UUID         NOT NULL,
    provider           VARCHAR(20)  NOT NULL,
    provider_user_id   VARCHAR(255) NOT NULL,
    provider_email     VARCHAR(255),
    provider_nickname  VARCHAR(255),
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member_oauth_account PRIMARY KEY (oauth_account_id),
    CONSTRAINT uq_member_oauth_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_member_oauth_member_provider UNIQUE (member_id, provider)
);

CREATE INDEX IF NOT EXISTS idx_member_oauth_member_id
    ON member.member_oauth_account (member_id);

CREATE TABLE IF NOT EXISTS member.email_verification (
    verification_id  UUID         NOT NULL,
    member_id        UUID         NOT NULL,
    email            VARCHAR(255) NOT NULL,
    token            VARCHAR(255) NOT NULL,
    purpose          VARCHAR(30)  NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    expires_at       TIMESTAMP    NOT NULL,
    verified_at      TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_email_verification PRIMARY KEY (verification_id),
    CONSTRAINT uq_email_verification_token UNIQUE (token),
    CONSTRAINT fk_email_verification_member
        FOREIGN KEY (member_id) REFERENCES member.member (member_id),
    CONSTRAINT ck_email_verification_purpose
        CHECK (purpose IN ('SIGNUP')),
    CONSTRAINT ck_email_verification_status
        CHECK (status IN ('PENDING', 'VERIFIED', 'EXPIRED', 'CANCELLED'))
);

CREATE INDEX IF NOT EXISTS idx_email_verification_member_created_at
    ON member.email_verification (member_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_email_verification_email_status
    ON member.email_verification (email, status);
CREATE INDEX IF NOT EXISTS idx_email_verification_expires_at
    ON member.email_verification (expires_at);
