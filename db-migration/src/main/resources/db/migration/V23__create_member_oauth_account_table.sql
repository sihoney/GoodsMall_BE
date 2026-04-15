CREATE TABLE IF NOT EXISTS member.member_oauth_account (
    oauth_account_id  UUID         NOT NULL,
    member_id         UUID         NOT NULL,
    provider          VARCHAR(20)  NOT NULL,
    provider_user_id  VARCHAR(255) NOT NULL,
    provider_email    VARCHAR(255),
    provider_nickname VARCHAR(255),
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_member_oauth_account PRIMARY KEY (oauth_account_id),
    CONSTRAINT uq_member_oauth_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT uq_member_oauth_member_provider UNIQUE (member_id, provider)
);

CREATE INDEX IF NOT EXISTS idx_member_oauth_member_id
    ON member.member_oauth_account (member_id);
