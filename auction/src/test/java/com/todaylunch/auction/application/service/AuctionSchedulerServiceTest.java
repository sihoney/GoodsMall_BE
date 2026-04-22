package com.todaylunch.auction.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerServiceTest {

    @Mock
    AuctionRepository auctionRepository;
    @Mock
    BidRepository bidRepository;

    @InjectMocks
    AuctionSchedulerService auctionSchedulerService;

    private Auction waitingAuction;
    private Auction ongoingAuction;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        waitingAuction = Auction.create(UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        new BigDecimal("10000"),
                                        new BigDecimal("1000"),
                                        now.minusHours(1),
                                        now.plusHours(1));

        ongoingAuction = Auction.create(UUID.randomUUID(),
                                        UUID.randomUUID(),
                                        new BigDecimal("10000"),
                                        new BigDecimal("1000"),
                                        now.minusHours(2),
                                        now.minusMinutes(1));
        ongoingAuction.start();
    }

    @Test
    void WAITING_경매_시작_시간이_지나면_ONGOING으로_전이() {
        given(auctionRepository.findStartable(any())).willReturn(List.of(waitingAuction));

        auctionSchedulerService.startWaitingAuctions();

        assertThat(waitingAuction.getStatus()).isEqualTo(AuctionStatus.ONGOING);
    }

    @Test
    void 종료된_경매에_ACTIVE_입찰이_있으면_PENDING_PAYMENT로_전이() {
        Bid activeBid = Bid.place(ongoingAuction, UUID.randomUUID(), new BigDecimal("11000"));

        given(auctionRepository.findEndable(any())).willReturn(List.of(ongoingAuction));
        given(bidRepository.findActiveByAuctionId(ongoingAuction.getAuctionId())).willReturn(Optional.of(activeBid));

        auctionSchedulerService.endExpiredAuctions();

        assertThat(ongoingAuction.getStatus()).isEqualTo(AuctionStatus.PENDING_PAYMENT);
    }

    @Test
    void 종료된_경매에_ACTIVE_입찰이_없으면_FAILED로_전이() {
        given(auctionRepository.findEndable(any())).willReturn(List.of(ongoingAuction));

        given(bidRepository.findActiveByAuctionId(ongoingAuction.getAuctionId())).willReturn(Optional.empty());

        auctionSchedulerService.endExpiredAuctions();

        assertThat(ongoingAuction.getStatus()).isEqualTo(AuctionStatus.FAILED);
    }
}
