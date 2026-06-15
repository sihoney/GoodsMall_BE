package com.example.payment.auction.infrastructure.messaging.kafka;



import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.auction.application.usecase.AuctionDepositRefundUseCase;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeRefundRequestedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
public class AuctionBidFeeRefundRequestedEventConsumer {

    private static final TypeReference<EventEnvelope<BidFeeRefundRequestedMessage>> ENVELOPE_TYPE =
            new TypeReference<>() {};

    private final AuctionDepositRefundUseCase auctionDepositRefundUseCase;
    private final ObjectMapper objectMapper;

    public AuctionBidFeeRefundRequestedEventConsumer(
            AuctionDepositRefundUseCase auctionDepositRefundUseCase,
            ObjectMapper objectMapper
    ) {
        this.auctionDepositRefundUseCase = auctionDepositRefundUseCase;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.AUCTION_BID_FEE_REFUND_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "auctionBidFeeRefundRequestedKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        String eventJson = record.value();
        log.info("寃쎈ℓ ?낆같 ?덉튂湲??섎텋 ?붿껌 ?대깽???섏떊: topic={}, partition={}, offset={}, key={}",
                record.topic(), record.partition(), record.offset(), record.key());

        try {
            EventEnvelope<BidFeeRefundRequestedMessage> envelope =
                    objectMapper.readValue(eventJson, ENVELOPE_TYPE);
            BidFeeRefundRequestedMessage message = envelope.payload();

            log.info("寃쎈ℓ ?낆같 ?덉튂湲??섎텋 ?붿껌: bidId={}, auctionId={}, bidderId={}",
                    message.bidId(), message.auctionId(), message.bidderId());

            auctionDepositRefundUseCase.refund(message.bidId());
        } catch (Exception e) {
            log.error("寃쎈ℓ ?낆같 ?덉튂湲??섎텋 ?붿껌 泥섎━ ?ㅽ뙣: topic={}, partition={}, offset={}, key={}",
                    record.topic(), record.partition(), record.offset(), record.key(), e);
            throw new RuntimeException(e);
        }
    }
}
