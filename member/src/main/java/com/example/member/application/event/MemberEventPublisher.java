package com.example.member.application.event;

import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.Seller;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationExpiredPayload;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationFailedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.example.member.infrastructure.messaging.kafka.contract.MemberOauthLinkedPayload;
import com.example.member.infrastructure.messaging.kafka.contract.SellerPromotedPayload;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishMemberSignedUp(Member member) {
        applicationEventPublisher.publishEvent(new MemberSignedUpPayload(
                member.getMemberId(),
                member.getEmail()
        ));
    }

    public void publishSellerPromoted(Member member, Seller seller) {
        applicationEventPublisher.publishEvent(new SellerPromotedPayload(
                member.getMemberId(),
                seller.getSellerId(),
                seller.getBankName()
        ));
    }

    public void publishAccountVerificationExpired(UUID memberId, String sessionId, String reason) {
        applicationEventPublisher.publishEvent(new AccountVerificationExpiredPayload(
                memberId,
                sessionId,
                reason
        ));
    }

    public void publishAccountVerificationFailed(UUID memberId, String sessionId, String reason) {
        applicationEventPublisher.publishEvent(new AccountVerificationFailedPayload(
                memberId,
                sessionId,
                reason
        ));
    }

    public void publishMemberOauthLinked(
            UUID memberId,
            String provider,
            String providerUserId,
            String providerEmail,
            String providerNickname,
            LocalDateTime linkedAt
    ) {
        applicationEventPublisher.publishEvent(new MemberOauthLinkedPayload(
                memberId,
                provider,
                providerUserId,
                providerEmail,
                providerNickname,
                linkedAt
        ));
    }
}
