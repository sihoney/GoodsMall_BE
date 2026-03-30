CREATE TABLE IF NOT EXISTS member_service.member (
    member_id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    address VARCHAR(255),
    profile_image_key VARCHAR(500),
    role VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_member_email
    ON member_service.member (email);

CREATE INDEX IF NOT EXISTS idx_member_nickname
    ON member_service.member (nickname);

CREATE INDEX IF NOT EXISTS idx_member_role
    ON member_service.member (role);

CREATE INDEX IF NOT EXISTS idx_member_status
    ON member_service.member (status);

