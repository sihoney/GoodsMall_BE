package com.example.payment.wallet.infrastructure.messaging.kafka;



import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.wallet.application.dto.CreateWalletCommand;
import com.example.payment.wallet.application.usecase.CreateWalletUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.infrastructure.messaging.kafka.contract.MemberSignedUpPayload;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class MemberCreatedEventConsumer {

    private static final String MEMBER_SIGNED_UP_EVENT_TYPE = "MEMBER_SIGNED_UP";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final TypeReference<EventEnvelope<MemberSignedUpPayload>> MEMBER_SIGNED_UP_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final CreateWalletUseCase createWalletUseCase;
    private final ObjectMapper objectMapper;

    public MemberCreatedEventConsumer(CreateWalletUseCase createWalletUseCase, ObjectMapper objectMapper) {
        this.createWalletUseCase = createWalletUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.MEMBER_CREATED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "memberCreatedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        try {
            EventEnvelope<MemberSignedUpPayload> event = objectMapper.readValue(
                    eventJson,
                    MEMBER_SIGNED_UP_ENVELOPE_TYPE
            );
            validateEvent(event);

            MemberSignedUpPayload payload = event.payload();
            createWalletUseCase.createWallet(new CreateWalletCommand(
                    payload.memberId(),
                    toKoreaLocalDateTime(event.occurredAt())
            ));
        } catch (Exception e) {
            log.error("?뚯썝 媛???대깽???붾쾶濡쒗봽 泥섎━???ㅽ뙣?덉뒿?덈떎.", e);
            throw new RuntimeException("?뚯썝 媛???대깽???붾쾶濡쒗봽 泥섎━???ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    private void validateEvent(EventEnvelope<MemberSignedUpPayload> event) {
        if (event == null) {
            throw new InvalidChargeRequestException("?뚯썝 媛???대깽?몃뒗 ?꾩닔?낅땲??");
        }
        if (event.eventId() == null) {
            throw new InvalidChargeRequestException("eventId???꾩닔?낅땲??");
        }
        if (!MEMBER_SIGNED_UP_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidChargeRequestException("吏?먰븯吏 ?딅뒗 eventType?낅땲?? eventType=" + event.eventType());
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidChargeRequestException("source???꾩닔?낅땲??");
        }
        if (event.recipientId() == null) {
            throw new InvalidChargeRequestException("recipientId???꾩닔?낅땲??");
        }
        if (event.payload() == null) {
            throw new InvalidChargeRequestException("payload???꾩닔?낅땲??");
        }
        if (event.payload().memberId() == null) {
            throw new InvalidChargeRequestException("payload.memberId???꾩닔?낅땲??");
        }
        if (event.payload().email() == null || event.payload().email().isBlank()) {
            throw new InvalidChargeRequestException("payload.email? ?꾩닔?낅땲??");
        }
        if (event.occurredAt() == null) {
            throw new InvalidChargeRequestException("occurredAt? ?꾩닔?낅땲??");
        }
        if (!Objects.equals(event.recipientId(), event.payload().memberId())) {
            throw new InvalidChargeRequestException("recipientId? payload.memberId???쇱튂?댁빞 ?⑸땲??");
        }
    }

    private LocalDateTime toKoreaLocalDateTime(java.time.Instant instant) {
        return LocalDateTime.ofInstant(instant, KOREA_ZONE_ID);
    }
}
