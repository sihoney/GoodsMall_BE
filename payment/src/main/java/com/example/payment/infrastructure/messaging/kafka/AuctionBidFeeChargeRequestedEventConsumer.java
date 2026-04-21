package com.example.payment.infrastructure.messaging.kafka;

import com.example.payment.application.dto.AuctionDepositCommand;
import com.example.payment.application.dto.AuctionDepositResult;
import com.example.payment.application.usecase.AuctionDepositUseCase;
import com.example.payment.common.exception.CustomException;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeFailedMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeRequestMessage;
import com.example.payment.infrastructure.messaging.kafka.contract.BidFeeChargeSucceededMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * auction -> payment 경매 입찰 보증금 처리 요청 이벤트를 소비한다.
 */
@Slf4j
@Component
public class AuctionBidFeeChargeRequestedEventConsumer {

    private static final String UNKNOWN_FAILURE_CODE = "AUCTION_DEPOSIT_PROCESSING_FAILED";
    private static final ZoneId KOREA_ZONE_ID = ZoneId.of("Asia/Seoul");

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
        BidFeeChargeRequestMessage event = readEvent(eventJson);

        try {
            validateEvent(event);
            AuctionDepositResult result = auctionDepositUseCase.processAuctionDeposit(toCommand(event));
            publishSuccess(result);
        } catch (CustomException e) {
            log.warn("경매 입찰 보증금 처리 비즈니스 실패 auctionId={} errorCode={}",
                    event.auctionId(), e.getErrorCode().name(), e);
            publishFailure(event, e.getErrorCode().name(), e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("경매 입찰 보증금 처리 요청값 검증 실패 auctionId={} message={}",
                    event.auctionId(), e.getMessage(), e);
            publishFailure(event, "INVALID_AUCTION_BID_FEE_REQUEST", e.getMessage());
        } catch (RuntimeException e) {
            log.error("경매 입찰 보증금 처리 실패 auctionId={}", event.auctionId(), e);
            publishFailure(event, UNKNOWN_FAILURE_CODE, "경매 입찰 보증금 처리 중 오류가 발생했습니다.");
        }
    }

    private BidFeeChargeRequestMessage readEvent(String eventJson) {
        try {
            return objectMapper.readValue(eventJson, BidFeeChargeRequestMessage.class);
        } catch (Exception e) {
            log.error("경매 입찰 보증금 처리 요청 이벤트 역직렬화에 실패했습니다.", e);
            throw new RuntimeException("경매 입찰 보증금 처리 요청 이벤트 역직렬화에 실패했습니다.", e);
        }
    }

    private void validateEvent(BidFeeChargeRequestMessage event) {
        Objects.requireNonNull(event, "경매 입찰 보증금 처리 요청 이벤트가 비어 있습니다.");
        Objects.requireNonNull(event.auctionId(), "경매 ID가 필요합니다.");
        Objects.requireNonNull(event.highestBidderId(), "최고 입찰자 ID가 필요합니다.");
        if (event.highestBidderFee() == null || event.highestBidderFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("최고 입찰자 보증금은 0보다 커야 합니다.");
        }
    }

    private AuctionDepositCommand toCommand(BidFeeChargeRequestMessage event) {
        return new AuctionDepositCommand(
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
                result.auctionId(),
                nowAsInstant()
        ));
    }

    private void publishFailure(BidFeeChargeRequestMessage event, String errorCode, String errorMessage) {
        resultEventPublisher.publishFailure(new BidFeeChargeFailedMessage(
                identifierGenerator.generateUuid(),
                event.auctionId(),
                errorCode,
                errorMessage,
                nowAsInstant()
        ));
    }

    private Instant nowAsInstant() {
        return timeProvider.now().atZone(KOREA_ZONE_ID).toInstant();
    }
}
