CREATE TABLE IF NOT EXISTS member_service.seller (
    seller_id UUID PRIMARY KEY,
    member_id UUID NOT NULL,
    bank_name VARCHAR(100),
    account VARCHAR(100),
    approved_at TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_seller_member_id
    ON member_service.seller (member_id);

CREATE INDEX IF NOT EXISTS idx_seller_approved_at
    ON member_service.seller (approved_at);
