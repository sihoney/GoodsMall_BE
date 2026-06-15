CREATE TABLE IF NOT EXISTS member_service.member_restriction (
    restriction_id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    admin_id UUID NOT NULL,
    reason VARCHAR(255) NOT NULL,
    restriction_type VARCHAR(30) NOT NULL,
    duration_hours INTEGER NOT NULL CHECK (duration_hours > 0),
    end_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_member_restriction_member_id
    ON member_service.member_restriction (member_id);

CREATE INDEX IF NOT EXISTS idx_member_restriction_active_lookup
    ON member_service.member_restriction (member_id, restriction_type, is_active, end_at);
