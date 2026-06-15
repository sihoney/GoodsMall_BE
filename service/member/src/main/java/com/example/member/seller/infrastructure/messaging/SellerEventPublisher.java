package com.example.member.seller.infrastructure.messaging;

import com.example.member.member.domain.entity.Member;
import com.example.member.seller.application.event.SellerPromotedEvent;
import com.example.member.seller.application.port.out.SellerEventPort;
import com.example.member.seller.domain.entity.Seller;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SellerEventPublisher implements SellerEventPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishSellerPromoted(Member member, Seller seller) {
        applicationEventPublisher.publishEvent(new SellerPromotedEvent(
                member.getMemberId(),
                seller.getSellerId(),
                seller.getBankName()
        ));
    }
}
