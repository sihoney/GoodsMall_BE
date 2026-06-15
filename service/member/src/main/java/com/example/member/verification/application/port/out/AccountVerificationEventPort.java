package com.example.member.verification.application.port.out;

import java.util.UUID;

public interface AccountVerificationEventPort {

    void publishAccountVerificationExpired(UUID memberId, String sessionId, String reason);

    void publishAccountVerificationFailed(UUID memberId, String sessionId, String reason);
}
