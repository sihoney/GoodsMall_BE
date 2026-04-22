package com.example.member.application.event;

import com.example.member.infrastructure.messaging.MemberEventKafkaProducer;
import com.example.member.infrastructure.messaging.kafka.contract.AccountVerificationExpiredPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountVerificationExpiredEventListener {

    private final MemberEventKafkaProducer memberEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AccountVerificationExpiredPayload payload) {
        log.info(
                "Handling AccountVerificationExpiredPayload after commit. memberId={} sessionId={} reason={}",
                payload.memberId(),
                payload.sessionId(),
                payload.reason()
        );
        memberEventKafkaProducer.sendAccountVerificationExpired(payload);
    }
}
