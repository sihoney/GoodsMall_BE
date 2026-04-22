package com.todaylunch.auction.infrastructure.messaging.kafka.consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.infrastructure.messaging.kafka.message.BidFeeChargeFailedMessage;
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

@ExtendWith(MockitoExtension.class)
class BidFeeChargeFailedConsumerTest {

    @Mock
    BidRepository bidRepository;
    @Mock
    AuctionRepository auctionRepository;

    BidFeeChargeFailedConsumer consumer;
    ObjectMapper objectMapper;

    private Auction auction;

    @BeforeEach
    void setUp() {
        // 객체 직렬화
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule())
                                         .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        consumer = new BidFeeChargeFailedConsumer(bidRepository, auctionRepository, objectMapper);

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
        given(bidRepository.findActiveByAuctionId(auction.getAuctionId())).willReturn(Optional.empty());
        given(auctionRepository.findByIdWithLock(auction.getAuctionId())).willReturn(auction);

        consumer.handle(objectMapper.writeValueAsString(message));

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

        consumer.handle(objectMapper.writeValueAsString(message));

        assertThat(bid.getStatus()).isEqualTo(BidStatus.ACTIVE);
    }
}
