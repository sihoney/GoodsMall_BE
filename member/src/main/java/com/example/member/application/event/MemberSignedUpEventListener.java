package com.example.member.application.event;

import com.example.member.infrastructure.messaging.MemberEventKafkaProducer;
import com.example.member.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberSignedUpEventListener {

    private final MemberEventKafkaProducer memberEventKafkaProducer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(MemberSignedUpPayload payload) {
        log.info("Handling MemberSignedUpPayload after commit. memberId={}", payload.memberId());
        memberEventKafkaProducer.sendMemberSignedUp(payload);
    }
}
