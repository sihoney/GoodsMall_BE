package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.event.MemberCreatedEvent;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.domain.exception.InvalidChargeRequestException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MemberCreatedEventConsumer {

    private final CreateWalletUseCase createWalletUseCase;

    public MemberCreatedEventConsumer(CreateWalletUseCase createWalletUseCase) {
        this.createWalletUseCase = createWalletUseCase;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.member-created:member.created}",
            groupId = "${payment.kafka.consumer-groups.member-created:payment-service}"
    )
    public void listen(MemberCreatedEvent event) {
        validateEvent(event);
        createWalletUseCase.createWallet(new CreateWalletCommand(
                event.memberId(),
                event.occurredAt()
        ));
    }

    private void validateEvent(MemberCreatedEvent event) {
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
