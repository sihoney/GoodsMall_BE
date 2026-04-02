package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.infrastructure.messaging.kafka.contract.MemberCreatedMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
/**
 * 회원 생성 이벤트를 payment wallet 생성 유스케이스로 연결하는 Kafka consumer다.
 * consumer는 계약 검증과 command 변환만 수행하고, wallet 생성 멱등성은 usecase에 위임한다.
 */
public class MemberCreatedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final CreateWalletUseCase createWalletUseCase;
    private final ObjectMapper objectMapper;

    public MemberCreatedEventConsumer(CreateWalletUseCase createWalletUseCase, ObjectMapper objectMapper) {
        this.createWalletUseCase = createWalletUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${payment.kafka.topics.member-created:member-signed-up}",
            groupId = "${payment.kafka.consumer-groups.member-created:payment-service}",
            containerFactory = "memberCreatedKafkaListenerContainerFactory"
    )
    /**
     * 회원 생성 이벤트를 wallet 생성 요청으로 변환한다.
     */
    public void listen(String eventJson) {
        log.info("event 받는거 : {}",eventJson);
        try {
            MemberCreatedMessage event = objectMapper.readValue(eventJson, MemberCreatedMessage.class);
            log.info("event 받는거 : {}",event);
            validateEvent(event);
            createWalletUseCase.createWallet(new CreateWalletCommand(
                    event.memberId(),
                    toKoreaLocalDateTime(event.occurredAt())
            ));
        } catch (Exception e) {
            log.error("Failed to process MemberCreatedMessage", e);
            throw new RuntimeException("Failed to deserialize MemberCreatedMessage", e);
        }
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

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
