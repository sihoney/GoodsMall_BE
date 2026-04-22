package com.example.member.application.event;

import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.Seller;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishMemberSignedUp(Member member) {
        applicationEventPublisher.publishEvent(new MemberSignedUpEvent(
                UUID.randomUUID(),
                member.getMemberId(),
                member.getEmail(),
                Instant.now()
        ));
    }

    public void publishSellerPromoted(Member member, Seller seller) {
        applicationEventPublisher.publishEvent(new SellerPromotedEvent(
                UUID.randomUUID(),
                member.getMemberId(),
                seller.getSellerId(),
                seller.getBankName(),
                Instant.now()
        ));
    }
}
