package com.example.member.verification.infrastructure.messaging;

import com.example.member.verification.application.event.AccountVerificationExpiredEvent;
import com.example.member.verification.application.event.AccountVerificationFailedEvent;
import com.example.member.verification.application.port.out.AccountVerificationEventPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountVerificationEventPublisher implements AccountVerificationEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishAccountVerificationExpired(UUID memberId, String sessionId, String reason) {
        applicationEventPublisher.publishEvent(new AccountVerificationExpiredEvent(
                memberId,
                sessionId,
                reason
        ));
    }

    @Override
    public void publishAccountVerificationFailed(UUID memberId, String sessionId, String reason) {
        applicationEventPublisher.publishEvent(new AccountVerificationFailedEvent(
                memberId,
                sessionId,
                reason
        ));
    }
}
