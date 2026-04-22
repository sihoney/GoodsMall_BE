package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.todaylunch.auction.application.event.BidPlacedEvent;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeCompletedMessage;
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
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
class BidFeeChargeCompletedConsumerTest {

    @Mock
    BidRepository bidRepository;
    @Mock
    ApplicationEventPublisher applicationEventPublisher;

    BidFeeChargeCompletedConsumer consumer;
    ObjectMapper objectMapper;

    private Auction auction;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();

        consumer = new BidFeeChargeCompletedConsumer(bidRepository,
                                                     applicationEventPublisher,
                                                     objectMapper);

        LocalDateTime now = LocalDateTime.now();

        auction = Auction.create(UUID.randomUUID(),
                                 UUID.randomUUID(),
                                 new BigDecimal("10000"),
                                 new BigDecimal("1000"),
                                 now,
                                 now.plusHours(1));
        auction.start();
    }

    @Test
    void PENDING_입찰_수신시_ACTIVE로_전이하고_이벤트_발행() throws Exception {
        Bid bid = Bid.placePending(auction,
                                   UUID.randomUUID(),
                                   new BigDecimal("11000"));

        BidFeeChargeCompletedMessage message = new BidFeeChargeCompletedMessage(UUID.randomUUID(),
                                                                                bid.getBidId(),
                                                                                auction.getAuctionId(),
                                                                                Instant.now());

        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));

        given(bidRepository.findActiveByAuctionId(auction.getAuctionId())).willReturn(Optional.empty());

        consumer.handle(objectMapper.writeValueAsString(message));

        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACTIVE);

        then(applicationEventPublisher).should().publishEvent(any(BidPlacedEvent.class));
    }

    @Test
    void 이전_ACTIVE_입찰이_있으면_OUTBID_처리() throws Exception {
        Bid previousBid = Bid.place(auction,
                                    UUID.randomUUID(),
                                    new BigDecimal("11000"));

        Bid newBid = Bid.placePending(auction,
                                      UUID.randomUUID(),
                                      new BigDecimal("12000"));

        BidFeeChargeCompletedMessage message = new BidFeeChargeCompletedMessage(UUID.randomUUID(),
                                                                                newBid.getBidId(),
                                                                                auction.getAuctionId(),
                                                                                Instant.now());

        given(bidRepository.findById(newBid.getBidId())).willReturn(Optional.of(newBid));

        given(bidRepository.findActiveByAuctionId(auction.getAuctionId())).willReturn(Optional.of(previousBid));

        consumer.handle(objectMapper.writeValueAsString(message));

        assertThat(newBid.getStatus()).isEqualTo(BidStatus.ACTIVE);
        assertThat(previousBid.getStatus()).isEqualTo(BidStatus.OUTBID);
    }

    @Test
    void PENDING이_아닌_상태면_무시() throws Exception {
        Bid bid = Bid.place(auction,
                            UUID.randomUUID(),
                            new BigDecimal("11000"));
        BidFeeChargeCompletedMessage message = new BidFeeChargeCompletedMessage(UUID.randomUUID(),
                                                                                bid.getBidId(),
                                                                                auction.getAuctionId(),
                                                                                Instant.now());
        given(bidRepository.findById(bid.getBidId())).willReturn(Optional.of(bid));

        consumer.handle(objectMapper.writeValueAsString(message));

        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACTIVE);
        then(applicationEventPublisher).should(never()).publishEvent(any(BidPlacedEvent.class));
    }
}
