package com.example.member.seller.infrastructure.messaging;

import com.example.member.common.infrastructure.messaging.MemberEventKafkaProducer;

import com.example.member.seller.application.event.SellerPromotedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerPromotedEventListener {

    private final MemberEventKafkaProducer memberEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerPromotedEvent event) {
        log.info("Handling SellerPromotedEvent after commit. memberId={} sellerId={}", event.memberId(), event.sellerId());
        memberEventKafkaProducer.sendSellerPromoted(event);
    }
}
