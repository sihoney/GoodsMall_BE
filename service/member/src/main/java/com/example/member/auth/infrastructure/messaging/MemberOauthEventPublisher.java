package com.example.member.auth.infrastructure.messaging;

import com.example.member.auth.application.event.MemberOauthLinkedEvent;
import com.example.member.auth.application.port.out.MemberOauthEventPort;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberOauthEventPublisher implements MemberOauthEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishMemberOauthLinked(
            UUID memberId,
            String provider,
            String providerUserId,
            String providerEmail,
            String providerNickname,
            LocalDateTime linkedAt
    ) {
        applicationEventPublisher.publishEvent(new MemberOauthLinkedEvent(
                memberId,
                provider,
                providerUserId,
                providerEmail,
                providerNickname,
                linkedAt
        ));
    }
}
