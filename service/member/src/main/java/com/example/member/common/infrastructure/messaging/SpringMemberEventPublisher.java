package com.example.member.common.infrastructure.messaging;

import com.example.member.verification.application.event.AccountVerificationExpiredEvent;
import com.example.member.verification.application.event.AccountVerificationFailedEvent;
import com.example.member.auth.application.event.MemberOauthLinkedEvent;
import com.example.member.member.application.event.MemberSignedUpEvent;
import com.example.member.seller.application.event.SellerPromotedEvent;
import com.example.member.common.application.port.out.MemberEventPort;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringMemberEventPublisher implements MemberEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishMemberSignedUp(Member member) {
        applicationEventPublisher.publishEvent(new MemberSignedUpEvent(
                member.getMemberId(),
                member.getEmail()
        ));
    }

    @Override
    public void publishSellerPromoted(Member member, Seller seller) {
        applicationEventPublisher.publishEvent(new SellerPromotedEvent(
                member.getMemberId(),
                seller.getSellerId(),
                seller.getBankName()
        ));
    }

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
