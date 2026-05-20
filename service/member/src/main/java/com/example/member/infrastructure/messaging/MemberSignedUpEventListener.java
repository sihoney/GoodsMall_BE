package com.example.member.infrastructure.messaging;

import com.example.member.application.event.MemberSignedUpEvent;
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
    public void handle(MemberSignedUpEvent event) {
        log.info("Handling MemberSignedUpEvent after commit. memberId={}", event.memberId());
        memberEventKafkaProducer.sendMemberSignedUp(event);
    }
}
