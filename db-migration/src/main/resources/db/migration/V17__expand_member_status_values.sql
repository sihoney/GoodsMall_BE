ALTER TABLE member.member
    DROP CONSTRAINT IF EXISTS ck_member_status;

ALTER TABLE member.member
    ADD CONSTRAINT ck_member_status
        CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'WITHDRAWN', 'DELETED'));
