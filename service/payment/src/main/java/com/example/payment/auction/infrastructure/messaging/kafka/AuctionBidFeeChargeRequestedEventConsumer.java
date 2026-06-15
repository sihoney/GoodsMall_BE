package com.example.payment.auction.infrastructure.messaging.kafka;




import com.example.payment.common.infrastructure.messaging.kafka.KafkaConsumerGroups;
import com.example.payment.common.infrastructure.messaging.kafka.KafkaTopics;
import com.example.payment.outbox.infrastructure.messaging.kafka.AuctionBidFeeChargeResultOutboxEventSaver;
import com.example.payment.auction.application.dto.AuctionDepositCommand;
import com.example.payment.auction.application.dto.AuctionDepositResult;
import com.example.payment.auction.application.usecase.AuctionDepositUseCase;
import com.example.payment.common.exception.AuctionBidFeeEventValidationException;
import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeChargeRequestMessage;
import com.example.payment.common.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * auction -> payment 寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽?몃? ?뚮퉬?쒕떎.
 */
@Slf4j
@Component
public class AuctionBidFeeChargeRequestedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String BID_FEE_CHARGE_REQUESTED_EVENT_TYPE = "BID_FEE_CHARGE_REQUESTED";
    private static final String BID_FEE_CHARGE_SUCCEEDED_EVENT_TYPE = "BID_FEE_CHARGE_SUCCEEDED";
    private static final String BID_FEE_CHARGE_FAILED_EVENT_TYPE = "BID_FEE_CHARGE_FAILED";
    private static final TypeReference<EventEnvelope<BidFeeChargeRequestMessage>> BID_FEE_CHARGE_REQUESTED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final AuctionDepositUseCase auctionDepositUseCase;
    private final AuctionBidFeeChargeResultOutboxEventSaver resultOutboxEventSaver;
    private final ObjectMapper objectMapper;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public AuctionBidFeeChargeRequestedEventConsumer(
            AuctionDepositUseCase auctionDepositUseCase,
            AuctionBidFeeChargeResultOutboxEventSaver resultOutboxEventSaver,
            ObjectMapper objectMapper,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.auctionDepositUseCase = auctionDepositUseCase;
        this.resultOutboxEventSaver = resultOutboxEventSaver;
        this.objectMapper = objectMapper;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @KafkaListener(
            topics = KafkaTopics.AUCTION_BID_FEE_CHARGE_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "auctionBidFeeChargeRequestedKafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        String eventJson = record.value();
        BidFeeChargeRequestMessage event = null;

        log.info("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽???섏떊: topic={}, partition={}, offset={}, key={}, payloadSize={}, payloadSnippet={}",
                record.topic(), record.partition(), record.offset(), record.key(), payloadSize(eventJson),
                summarizePayload(eventJson));

        try {
            EventEnvelope<BidFeeChargeRequestMessage> envelope = readEnvelope(eventJson, record);
            validateEnvelope(envelope, record);
            event = envelope.payload();
            log.info("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 envelope 寃利??깃났: eventId={}, eventType={}, aggregateId={}, recipientId={}",
                    envelope.eventId(), envelope.eventType(), envelope.aggregateId(), envelope.recipientId());

            validateEvent(event);
            log.info("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 payload 寃利??깃났: bidId={}, auctionId={}, highestBidderId={}, highestBidderFee={}",
                    event.bidId(), event.auctionId(), event.highestBidderId(), event.highestBidderFee());

            AuctionDepositResult result = auctionDepositUseCase.processAuctionDeposit(toCommand(event));
            log.info("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?깃났: bidId={}, auctionId={}", result.bidId(), result.auctionId());
            publishSuccess(result);
        } catch (AuctionBidFeeEventValidationException e) {
            log.error("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 寃利??ㅽ뙣: topic={}, partition={}, offset={}, key={}, payloadSnippet={}",
                    record.topic(), record.partition(), record.offset(), record.key(), summarizePayload(eventJson), e);
            throw e;
        } catch (CustomException e) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ 鍮꾩쫰?덉뒪 ?ㅽ뙣 auctionId={} errorCode={}",
                    event == null ? null : event.auctionId(), e.getErrorCode().name(), e);
            if (!canPublishFailure(event)) {
                throw new RuntimeException("寃쎈ℓ ?낆같 蹂댁쬆湲??ㅽ뙣 ?대깽?몃? 諛쒗뻾??寃쎈ℓ ID媛 ?놁뒿?덈떎.", e);
            }
            publishFailure(event, e.getErrorCode().name(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?ㅽ뙣: topic={}, partition={}, offset={}, key={}, auctionId={}, payloadSnippet={}",
                    record.topic(), record.partition(), record.offset(), record.key(),
                    event == null ? null : event.auctionId(), summarizePayload(eventJson), e);
            throw e;
        }
    }

    private EventEnvelope<BidFeeChargeRequestMessage> readEnvelope(String eventJson, ConsumerRecord<String, String> record) {
        try {
            return objectMapper.readValue(eventJson, BID_FEE_CHARGE_REQUESTED_ENVELOPE_TYPE);
        } catch (Exception e) {
            log.error("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽????쭅?ы솕 ?ㅽ뙣: topic={}, partition={}, offset={}, key={}, payloadSnippet={}",
                    record.topic(), record.partition(), record.offset(), record.key(), summarizePayload(eventJson), e);
            throw new AuctionBidFeeEventValidationException(
                    ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED,
                    "寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 ?대깽????쭅?ы솕???ㅽ뙣?덉뒿?덈떎."
            );
        }
    }

    private void validateEnvelope(EventEnvelope<BidFeeChargeRequestMessage> envelope, ConsumerRecord<String, String> record) {
        if (envelope == null) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 envelope ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.eventId() == null) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 eventId ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (!BID_FEE_CHARGE_REQUESTED_EVENT_TYPE.equals(envelope.eventType())) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 eventType 遺덉씪移? topic={}, partition={}, offset={}, actualEventType={}",
                    record.topic(), record.partition(), record.offset(), envelope.eventType());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 source ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.aggregateId() == null) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 aggregateId ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_AUCTION_ID_REQUIRED);
        }
        if (envelope.occurredAt() == null) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 occurredAt ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 traceId ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.payload() == null) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 payload ?꾨씫: topic={}, partition={}, offset={}",
                    record.topic(), record.partition(), record.offset());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (!Objects.equals(envelope.aggregateId(), envelope.payload().auctionId())) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 aggregateId 遺덉씪移? topic={}, partition={}, offset={}, aggregateId={}, payloadAuctionId={}",
                    record.topic(), record.partition(), record.offset(), envelope.aggregateId(),
                    envelope.payload().auctionId());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_AUCTION_ID_REQUIRED);
        }
        if (envelope.recipientId() != null
                && !Objects.equals(envelope.recipientId(), envelope.payload().highestBidderId())) {
            log.warn("寃쎈ℓ ?낆같 蹂댁쬆湲?泥섎━ ?붿껌 recipientId 遺덉씪移? topic={}, partition={}, offset={}, recipientId={}, payloadHighestBidderId={}",
                    record.topic(), record.partition(), record.offset(), envelope.recipientId(),
                    envelope.payload().highestBidderId());
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_HIGHEST_BIDDER_REQUIRED);
        }
    }

    private void validateEvent(BidFeeChargeRequestMessage event) {
        if (event == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (event.auctionId() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_AUCTION_ID_REQUIRED);
        }
        if (event.highestBidderId() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_HIGHEST_BIDDER_REQUIRED);
        }
        if (event.highestBidderFee() == null || event.highestBidderFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_HIGHEST_FEE_INVALID);
        }
    }

    private AuctionDepositCommand toCommand(BidFeeChargeRequestMessage event) {
        return new AuctionDepositCommand(
                event.bidId(),
                event.auctionId(),
                event.highestBidderId(),
                event.highestBidderFee()
        );
    }

    private void publishSuccess(AuctionDepositResult result) {
        resultOutboxEventSaver.saveSuccess(BID_FEE_CHARGE_SUCCEEDED_EVENT_TYPE, new BidFeeChargeSucceededMessage(
                identifierGenerator.generateUuid(),
                result.bidId(),
                result.auctionId(),
                nowAsInstant()
        ));
    }

    private void publishFailure(BidFeeChargeRequestMessage event, String errorCode, String errorMessage) {
        resultOutboxEventSaver.saveFailure(BID_FEE_CHARGE_FAILED_EVENT_TYPE, new BidFeeChargeFailedMessage(
                identifierGenerator.generateUuid(),
                event.bidId(),
                event.auctionId(),
                errorCode,
                errorMessage,
                nowAsInstant()
        ));
    }

    private boolean canPublishFailure(BidFeeChargeRequestMessage event) {
        return event != null && event.auctionId() != null;
    }

    private int payloadSize(String payload) {
        return payload == null ? 0 : payload.length();
    }

    private String summarizePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return "<empty>";
        }
        String normalized = payload.replaceAll("\\s+", " ").trim();
        return normalized.length() <= 300 ? normalized : normalized.substring(0, 300) + "...";
    }

    private Instant nowAsInstant() {
        return timeProvider.now().atZone(KOREA_ZONE_ID).toInstant();
    }
}
