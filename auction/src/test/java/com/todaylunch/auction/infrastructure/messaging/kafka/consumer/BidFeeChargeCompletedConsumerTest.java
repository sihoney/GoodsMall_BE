package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.todaylunch.auction.application.service.BidUpdateService;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeCompletedMessage;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BidFeeChargeCompletedConsumerTest {

    @Mock
    BidRepository bidRepository;
    @Mock
    BidUpdateService bidUpdateService;

    BidFeeChargeCompletedConsumer consumer;
    ObjectMapper objectMapper;

    private Auction auction;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        consumer = new BidFeeChargeCompletedConsumer(bidRepository, bidUpdateService, objectMapper);

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
    void PENDING_입찰_수신시_activate_호출() throws Exception {
        Bid bid = Bid.placePending(auction, UUID.randomUUID(), new BigDecimal("11000"));
        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));

        consumer.handle(toEnvelopeJson(bid.getBidId(), auction.getAuctionId()));

        then(bidUpdateService).should().activate(bid.getBidId());
    }

    @Test
    void PENDING이_아닌_상태면_activate_호출_안함() throws Exception {
        Bid bid = Bid.place(auction, UUID.randomUUID(), new BigDecimal("11000"));
        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));

        consumer.handle(toEnvelopeJson(bid.getBidId(), auction.getAuctionId()));

        then(bidUpdateService).should(never()).activate(any());
    }

    @Test
    void 낙관락_충돌시_MAX_RETRY만큼_재시도() throws Exception {
        Bid bid = Bid.placePending(auction, UUID.randomUUID(), new BigDecimal("11000"));
        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));
        willThrow(new ObjectOptimisticLockingFailureException(Auction.class, bid.getBidId()))
                .given(bidUpdateService).activate(bid.getBidId());

        consumer.handle(toEnvelopeJson(bid.getBidId(), auction.getAuctionId()));

        then(bidUpdateService).should(times(3)).activate(bid.getBidId());
        then(bidUpdateService).should().cancel(bid.getBidId());
    }

    @Test
    void 낙관락_충돌_후_성공하면_cancel_호출_안함() throws Exception {
        Bid bid = Bid.placePending(auction, UUID.randomUUID(), new BigDecimal("11000"));
        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));
        willThrow(new ObjectOptimisticLockingFailureException(Auction.class, bid.getBidId()))
                .willDoNothing()
                .given(bidUpdateService).activate(bid.getBidId());

        consumer.handle(toEnvelopeJson(bid.getBidId(), auction.getAuctionId()));

        then(bidUpdateService).should(times(2)).activate(bid.getBidId());
        then(bidUpdateService).should(never()).cancel(any());
    }

    private String toEnvelopeJson(UUID bidId, UUID auctionId) throws Exception {
        BidFeeChargeCompletedMessage message = new BidFeeChargeCompletedMessage(
                UUID.randomUUID(), bidId, auctionId, Instant.now());
        EventEnvelope<BidFeeChargeCompletedMessage> envelope = new EventEnvelope<>(
                UUID.randomUUID(),
                "BID_FEE_CHARGE_SUCCEEDED",
                "payment-service",
                auctionId,
                null,
                Instant.now(),
                "mock-trace-id",
                message
        );
        return objectMapper.writeValueAsString(envelope);
    }
}
