CREATE TABLE IF NOT EXISTS member_service.member_report (
    report_id UUID PRIMARY KEY,
    reporter_id UUID NOT NULL,
    reported_member_id UUID NOT NULL,
    reason VARCHAR(255) NOT NULL,
    report_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    review_comment VARCHAR(255),
    reviewed_by UUID,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_member_report_reporter_id
    ON member_service.member_report (reporter_id);

CREATE INDEX IF NOT EXISTS idx_member_report_reported_member_id
    ON member_service.member_report (reported_member_id);

CREATE INDEX IF NOT EXISTS idx_member_report_status
    ON member_service.member_report (status);
