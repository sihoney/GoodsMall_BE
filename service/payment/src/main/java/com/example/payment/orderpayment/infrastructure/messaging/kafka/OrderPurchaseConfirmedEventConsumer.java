package com.example.payment.orderpayment.infrastructure.messaging.kafka;



import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.common.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.escrow.application.dto.EscrowReleaseCommand;
import com.example.payment.escrow.application.usecase.EscrowReleaseUseCase;
import com.example.payment.common.infrastructure.messaging.kafka.contract.OrderPurchaseConfirmedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
/**
 * ?섎룞 援щℓ?뺤젙 ?대깽?몃? escrow release ?좎뒪耳?댁뒪濡??곌껐?섎뒗 Kafka consumer??
 * consumer??MANUAL 怨꾩빟 寃利앷낵 command 蹂?섎쭔 ?대떦?섍퀬, ?뺤궛 ?뺤콉? usecase???꾩엫?쒕떎.
 */
public class OrderPurchaseConfirmedEventConsumer {

    private static final String ORDER_PURCHASE_CONFIRMED_EVENT_TYPE = "ORDER_PURCHASE_CONFIRMED";
    private static final TypeReference<EventEnvelope<OrderPurchaseConfirmedMessage>> ORDER_PURCHASE_CONFIRMED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final EscrowReleaseUseCase escrowReleaseUseCase;
    private final ObjectMapper objectMapper;

    public OrderPurchaseConfirmedEventConsumer(EscrowReleaseUseCase escrowReleaseUseCase, ObjectMapper objectMapper) {
        this.escrowReleaseUseCase = escrowReleaseUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.ORDER_PURCHASE_CONFIRMED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "orderPurchaseConfirmedKafkaListenerContainerFactory"
    )
    /**
     * ?섎룞 援щℓ?뺤젙 ?대깽?몃쭔 escrow release ?붿껌?쇰줈 ?꾨떖?쒕떎.
     * AUTO 援щℓ?뺤젙? scheduler 寃쎈줈?먯꽌 泥섎━?섎?濡?consumer ?④퀎?먯꽌 李⑤떒?쒕떎.
     */
    public void listen(String eventJson) {
        try {
            EventEnvelope<OrderPurchaseConfirmedMessage> event = objectMapper.readValue(
                    eventJson,
                    ORDER_PURCHASE_CONFIRMED_ENVELOPE_TYPE
            );
            validateEvent(event);
            OrderPurchaseConfirmedMessage payload = event.payload();
            escrowReleaseUseCase.releaseEscrow(new EscrowReleaseCommand(
                    payload.orderId(),
                    payload.sellerMemberId(),
                    payload.confirmationType()
            ));
        } catch (Exception e) {
            log.error("二쇰Ц 援щℓ ?뺤젙 ?대깽???붾쾶濡쒗봽 泥섎━???ㅽ뙣?덉뒿?덈떎.", e);
            throw new RuntimeException("二쇰Ц 援щℓ ?뺤젙 ?대깽???붾쾶濡쒗봽 ??쭅?ы솕???ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    /**
     * 援щℓ?뺤젙 ?대깽??怨꾩빟怨??덉슜 confirmation type??寃利앺븳??
     */
    private void validateEvent(EventEnvelope<OrderPurchaseConfirmedMessage> event) {
        if (event == null) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц 援щℓ ?뺤젙 ?대깽?몃뒗 ?꾩닔?낅땲??");
        }
        if (event.eventId() == null) {
            throw new InvalidOrderPaymentRequestException("eventId???꾩닔?낅땲??");
        }
        if (!ORDER_PURCHASE_CONFIRMED_EVENT_TYPE.equals(event.eventType())) {
            throw new InvalidOrderPaymentRequestException("吏?먰븯吏 ?딅뒗 eventType?낅땲?? eventType=" + event.eventType());
        }
        if (event.source() == null || event.source().isBlank()) {
            throw new InvalidOrderPaymentRequestException("source???꾩닔?낅땲??");
        }
        if (event.aggregateId() == null) {
            throw new InvalidOrderPaymentRequestException("aggregateId???꾩닔?낅땲??");
        }
        if (event.occurredAt() == null) {
            throw new InvalidOrderPaymentRequestException("occurredAt? ?꾩닔?낅땲??");
        }
        if (event.traceId() == null || event.traceId().isBlank()) {
            throw new InvalidOrderPaymentRequestException("traceId???꾩닔?낅땲??");
        }
        if (event.payload() == null) {
            throw new InvalidOrderPaymentRequestException("payload???꾩닔?낅땲??");
        }
        if (event.payload().orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId???꾩닔?낅땲??");
        }
        if (event.payload().sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId???꾩닔?낅땲??");
        }
        if (event.payload().confirmedAt() == null) {
            throw new InvalidOrderPaymentRequestException("confirmedAt? ?꾩닔?낅땲??");
        }
        if (event.payload().confirmationType() == null) {
            throw new InvalidOrderPaymentRequestException("confirmationType? ?꾩닔?낅땲??");
        }
        if (!Objects.equals(event.aggregateId(), event.payload().orderId())) {
            throw new InvalidOrderPaymentRequestException("aggregateId? payload.orderId???쇱튂?댁빞 ?⑸땲??");
        }
    }
}
