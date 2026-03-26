package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
/**
 * 회원 생성 이벤트를 payment wallet 생성 유스케이스로 연결하는 Kafka consumer다.
 * consumer는 계약 검증과 command 변환만 수행하고, wallet 생성 멱등성은 usecase에 위임한다.
 */
public class MemberCreatedEventConsumer {

    private final CreateWalletUseCase createWalletUseCase;

    public MemberCreatedEventConsumer(CreateWalletUseCase createWalletUseCase) {
        this.createWalletUseCase = createWalletUseCase;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.member-created:member.created}",
            groupId = "${payment.kafka.consumer-groups.member-created:payment-service}",
            containerFactory = "memberCreatedKafkaListenerContainerFactory"
    )
    /**
     * 회원 생성 이벤트를 wallet 생성 요청으로 변환한다.
     */
    public void listen(MemberCreatedMessage event) {
        validateEvent(event);
        createWalletUseCase.createWallet(new CreateWalletCommand(
                event.memberId(),
                event.occurredAt()
        ));
    }

    /**
     * member created 계약의 필수 필드만 검증한다.
     */
    private void validateEvent(MemberCreatedMessage event) {
        if (event == null) {
            throw new InvalidChargeRequestException("memberCreated event is required.");
        }
        if (event.memberId() == null) {
            throw new InvalidChargeRequestException("memberId is required.");
        }
        if (event.occurredAt() == null) {
            throw new InvalidChargeRequestException("occurredAt is required.");
        }
    }
}
