package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeFailedMessage;
import com.todaylunch.common.event.contract.EventEnvelope;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BidFeeChargeFailedConsumerTest {

    @Mock
    BidRepository bidRepository;
    @Mock
    AuctionRepository auctionRepository;
    @Mock
    SimpMessagingTemplate messagingTemplate;

    BidFeeChargeFailedConsumer consumer;
    ObjectMapper objectMapper;

    private Auction auction;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();

        consumer = new BidFeeChargeFailedConsumer(bidRepository, auctionRepository, messagingTemplate, objectMapper);

        LocalDateTime now = LocalDateTime.now();

        auction = Auction.create(UUID.randomUUID(),
                                 "테스트 상품",
                                 "test/thumbnail.jpg",
                                 UUID.randomUUID(),
                                 new BigDecimal("10000"),
                                 new BigDecimal("1000"),
                                 now,
                                 now.plusHours(1));
        auction.start();
    }

    @Test
    void 상태가_PENDING인_입찰_수신시_CANCELED로_전이() throws Exception {

        Bid bid = Bid.placePending(auction,
                                   UUID.randomUUID(),
                                   new BigDecimal("11000"));

        BidFeeChargeFailedMessage message = new BidFeeChargeFailedMessage(UUID.randomUUID(),
                                                                          bid.getBidId(),
                                                                          auction.getAuctionId(),
                                                                          "INSUFFICIENT_WALLET_BALANCE",
                                                                          "잔액 부족",
                                                                          Instant.now());

        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));
        given(bidRepository.findCurrentValidByAuctionId(auction.getAuctionId())).willReturn(Optional.empty());
        given(auctionRepository.findByIdWithLock(auction.getAuctionId())).willReturn(auction);

        consumer.handle(toEnvelopeJson(message));

        assertThat(bid.getStatus()).isEqualTo(BidStatus.CANCELED);
    }

    @Test
    void PENDING이_아닌_상태면_무시() throws Exception {
        Bid bid = Bid.place(auction,
                            UUID.randomUUID(),
                            new BigDecimal("11000"));

        BidFeeChargeFailedMessage message = new BidFeeChargeFailedMessage(UUID.randomUUID(),
                                                                          bid.getBidId(),
                                                                          auction.getAuctionId(),
                                                                          "INSUFFICIENT_WALLET_BALANCE",
                                                                          "잔액 부족",
                                                                          Instant.now());

        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));

        consumer.handle(toEnvelopeJson(message));

        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACTIVE);
    }

    private String toEnvelopeJson(BidFeeChargeFailedMessage message) throws Exception {
        EventEnvelope<BidFeeChargeFailedMessage> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "BID_FEE_CHARGE_FAILED",
                "payment-service",
                message.auctionId(),
                null,
                Instant.now(),
                "mock-trace-id",
                message
        );
        return objectMapper.writeValueAsString(envelope);
    }
}
