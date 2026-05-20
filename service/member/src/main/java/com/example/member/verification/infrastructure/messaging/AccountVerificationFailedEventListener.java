package com.example.member.verification.infrastructure.messaging;

import com.example.member.common.infrastructure.messaging.MemberEventKafkaProducer;

import com.example.member.verification.application.event.AccountVerificationFailedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountVerificationFailedEventListener {

    private final MemberEventKafkaProducer memberEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountVerificationFailedEvent event) {
        log.info(
                "Handling AccountVerificationFailedEvent after commit. memberId={} sessionId={} reason={}",
                event.memberId(),
                event.sessionId(),
                event.reason()
        );
        memberEventKafkaProducer.sendAccountVerificationFailed(event);
    }
}
