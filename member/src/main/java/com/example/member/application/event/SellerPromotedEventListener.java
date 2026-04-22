package com.example.member.application.event;

import com.example.member.infrastructure.messaging.MemberEventKafkaProducer;
import com.example.member.infrastructure.messaging.kafka.contract.SellerPromotedPayload;
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
    public void handle(SellerPromotedPayload payload) {
        log.info("Handling SellerPromotedPayload after commit. memberId={} sellerId={}", payload.memberId(), payload.sellerId());
        memberEventKafkaProducer.sendSellerPromoted(payload);
    }
}
