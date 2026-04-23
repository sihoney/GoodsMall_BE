package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.AuctionDepositCommand;
import com.example.payment.application.dto.AuctionDepositResult;
import com.example.payment.application.usecase.AuctionDepositUseCase;
import com.example.payment.common.exception.AuctionBidFeeEventValidationException;
import com.example.payment.common.exception.CustomException;
import com.example.payment.common.exception.ErrorCode;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeRequestMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * auction -> payment 경매 입찰 보증금 처리 요청 이벤트를 소비한다.
 */
@Slf4j
@Component
public class AuctionBidFeeChargeRequestedEventConsumer {

    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String BID_FEE_CHARGE_REQUESTED_EVENT_TYPE = "BID_FEE_CHARGE_REQUESTED";
    private static final TypeReference<EventEnvelope<BidFeeChargeRequestMessage>> BID_FEE_CHARGE_REQUESTED_ENVELOPE_TYPE =
            new TypeReference<>() {
            };

    private final AuctionDepositUseCase auctionDepositUseCase;
    private final KafkaAuctionBidFeeChargeResultEventPublisher resultEventPublisher;
    private final ObjectMapper objectMapper;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public AuctionBidFeeChargeRequestedEventConsumer(
            AuctionDepositUseCase auctionDepositUseCase,
            KafkaAuctionBidFeeChargeResultEventPublisher resultEventPublisher,
            ObjectMapper objectMapper,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.auctionDepositUseCase = auctionDepositUseCase;
        this.resultEventPublisher = resultEventPublisher;
        this.objectMapper = objectMapper;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @KafkaListener(
            topics = KafkaTopics.AUCTION_BID_FEE_CHARGE_REQUESTED,
            groupId = KafkaConsumerGroups.PAYMENT_SERVICE,
            containerFactory = "auctionBidFeeChargeRequestedKafkaListenerContainerFactory"
    )
    public void listen(String eventJson) {
        EventEnvelope<BidFeeChargeRequestMessage> envelope = readEnvelope(eventJson);
        validateEnvelope(envelope);
        BidFeeChargeRequestMessage event = envelope.payload();

        try {
            validateEvent(event);
            AuctionDepositResult result = auctionDepositUseCase.processAuctionDeposit(toCommand(event));
            publishSuccess(result);
        } catch (CustomException e) {
            log.warn("경매 입찰 보증금 처리 비즈니스 실패 auctionId={} errorCode={}",
                    event == null ? null : event.auctionId(), e.getErrorCode().name(), e);
            if (!canPublishFailure(event)) {
                throw new RuntimeException("경매 입찰 보증금 실패 이벤트를 발행할 경매 ID가 없습니다.", e);
            }
            publishFailure(event, e.getErrorCode().name(), e.getMessage());
        } catch (RuntimeException e) {
            log.error("경매 입찰 보증금 처리 실패 auctionId={}", event.auctionId(), e);
            publishFailure(event, ErrorCode.AUCTION_DEPOSIT_PROCESSING_FAILED.name(),
                    ErrorCode.AUCTION_DEPOSIT_PROCESSING_FAILED.getMessage());
        }
    }

    private EventEnvelope<BidFeeChargeRequestMessage> readEnvelope(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, BID_FEE_CHARGE_REQUESTED_ENVELOPE_TYPE);
        } catch (Exception e) {
            log.error("경매 입찰 보증금 처리 요청 이벤트 역직렬화에 실패했습니다.", e);
            throw new RuntimeException("경매 입찰 보증금 처리 요청 이벤트 역직렬화에 실패했습니다.", e);
        }
    }

    private void validateEnvelope(EventEnvelope<BidFeeChargeRequestMessage> envelope) {
        if (envelope == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.eventId() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (!BID_FEE_CHARGE_REQUESTED_EVENT_TYPE.equals(envelope.eventType())) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.source() == null || envelope.source().isBlank()) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.aggregateId() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_AUCTION_ID_REQUIRED);
        }
        if (envelope.occurredAt() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.traceId() == null || envelope.traceId().isBlank()) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (envelope.payload() == null) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_EVENT_REQUIRED);
        }
        if (!Objects.equals(envelope.aggregateId(), envelope.payload().auctionId())) {
            throw new AuctionBidFeeEventValidationException(ErrorCode.AUCTION_BID_FEE_AUCTION_ID_REQUIRED);
        }
        if (envelope.recipientId() != null
                && !Objects.equals(envelope.recipientId(), envelope.payload().highestBidderId())) {
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
                event.isFirst(),
                event.previousBidderId(),
                event.previousBidderPaidFee(),
                event.highestBidderId(),
                event.highestBidderFee()
        );
    }

    private void publishSuccess(AuctionDepositResult result) {
        resultEventPublisher.publishSuccess(new BidFeeChargeSucceededMessage(
                identifierGenerator.generateUuid(),
                result.bidId(),
                result.auctionId(),
                nowAsInstant()
        ));
    }

    private void publishFailure(BidFeeChargeRequestMessage event, String errorCode, String errorMessage) {
        resultEventPublisher.publishFailure(new BidFeeChargeFailedMessage(
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

    private Instant nowAsInstant() {
        return timeProvider.now().atZone(KOREA_ZONE_ID).toInstant();
    }
}
