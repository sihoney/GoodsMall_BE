package com.example.member.application.event;

import com.example.member.infrastructure.messaging.MemberEventKafkaProducer;
import com.example.member.infrastructure.messaging.kafka.contract.MemberOauthLinkedPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberOauthLinkedEventListener {

    private final MemberEventKafkaProducer memberEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberOauthLinkedPayload payload) {
        log.info(
                "Handling MemberOauthLinkedPayload after commit. memberId={} provider={} providerUserId={}",
                payload.memberId(),
                payload.provider(),
                payload.providerUserId()
        );
        memberEventKafkaProducer.sendMemberOauthLinked(payload);
    }
}
