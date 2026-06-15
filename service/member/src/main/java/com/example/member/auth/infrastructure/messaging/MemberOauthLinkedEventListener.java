package com.example.member.auth.infrastructure.messaging;

import com.example.member.auth.application.event.MemberOauthLinkedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberOauthLinkedEventListener {

    private final MemberOauthEventKafkaProducer memberOauthEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberOauthLinkedEvent event) {
        log.info(
                "Handling MemberOauthLinkedEvent after commit. memberId={} provider={} providerUserId={}",
                event.memberId(),
                event.provider(),
                event.providerUserId()
        );
        memberOauthEventKafkaProducer.sendMemberOauthLinked(event);
    }
}
