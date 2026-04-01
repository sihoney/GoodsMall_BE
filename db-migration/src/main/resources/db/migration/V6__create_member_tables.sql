CREATE TABLE IF NOT EXISTS member_service.member (
    member_id         UUID          NOT NULL,
    email             VARCHAR(255)  NOT NULL,
    password          VARCHAR(255)  NOT NULL,
    nickname          VARCHAR(100)  NOT NULL,
    phone             VARCHAR(50),
    address           VARCHAR(255),
    profile_image_key VARCHAR(255),
    role              VARCHAR(20)   NOT NULL,
    status            VARCHAR(20)   NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member PRIMARY KEY (member_id),
    CONSTRAINT uq_member_email UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS member_service.seller (
    seller_id   UUID         NOT NULL,
    member_id   UUID         NOT NULL,
    bank_name   VARCHAR(100),
    account     VARCHAR(100),
    approved_at TIMESTAMP,
    CONSTRAINT pk_seller PRIMARY KEY (seller_id),
    CONSTRAINT uq_seller_member_id UNIQUE (member_id)
);

CREATE TABLE IF NOT EXISTS member_service.member_report (
    report_id          UUID          NOT NULL,
    reporter_id        UUID          NOT NULL,
    reported_member_id UUID          NOT NULL,
    reason             VARCHAR(1000) NOT NULL,
    report_type        VARCHAR(20)   NOT NULL,
    status             VARCHAR(20)   NOT NULL,
    review_comment     VARCHAR(1000),
    reviewed_by        UUID,
    reviewed_at        TIMESTAMP,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member_report PRIMARY KEY (report_id)
);

CREATE TABLE IF NOT EXISTS member_service.member_restriction (
    restriction_id   UUID          NOT NULL,
    member_id        UUID          NOT NULL,
    admin_id         UUID          NOT NULL,
    reason           VARCHAR(1000) NOT NULL,
    restriction_type VARCHAR(20)   NOT NULL,
    duration_hours   INTEGER       NOT NULL,
    end_at           TIMESTAMP     NOT NULL,
    is_active        BOOLEAN       NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT pk_member_restriction PRIMARY KEY (restriction_id)
);

CREATE INDEX IF NOT EXISTS idx_member_restriction_member_type_active_end_at
    ON member_service.member_restriction (member_id, restriction_type, is_active, end_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_restriction_member_created_at
    ON member_service.member_restriction (member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_report_reporter_created_at
    ON member_service.member_report (reporter_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_report_reported_member_created_at
    ON member_service.member_report (reported_member_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_member_report_created_at
    ON member_service.member_report (created_at DESC);
