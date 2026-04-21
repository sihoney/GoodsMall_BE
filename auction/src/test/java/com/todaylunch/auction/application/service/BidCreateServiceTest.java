package com.todaylunch.auction.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.todaylunch.auction.application.port.BidFeeChargeEventPublisher;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.domain.enumtype.BidStatus;
import com.todaylunch.auction.presentation.dto.request.BidPlaceRequest;
import com.todaylunch.auction.presentation.dto.response.BidResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BidCreateServiceTest {

    @Mock
    AuctionRepository auctionRepository;
    @Mock
    BidRepository bidRepository;
    @Mock
    BidFeeChargeEventPublisher bidFeeChargeEventPublisher;

    @InjectMocks
    BidCreateService bidCreateService;

    private Auction auction;
    private UUID auctionId;
    private UUID bidderId;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        auction = Auction.create(UUID.randomUUID(),
                                 UUID.randomUUID(),
                                 new BigDecimal("10000"),
                                 new BigDecimal("1000"),
                                 now,
                                 now.plusHours(1));

        auction.start();
        auctionId = auction.getAuctionId();
        bidderId = UUID.randomUUID();
    }

    @Test
    void 첫_입찰은_PENDING_상태로_저장되고_isFirst_true로_이벤트_발행() {
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("11000"));
        given(auctionRepository.findByIdWithLock(auctionId)).willReturn(auction);
        given(bidRepository.findActiveByAuctionId(auctionId)).willReturn(Optional.empty());
        given(bidRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        BidResponse response = bidCreateService.place(auctionId, bidderId, request);

        assertThat(response.status()).isEqualTo(BidStatus.PENDING);

        ArgumentCaptor<BidFeeChargeRequest> captor = ArgumentCaptor.forClass(BidFeeChargeRequest.class);
        then(bidFeeChargeEventPublisher).should(times(1)).publish(captor.capture());
        assertThat(captor.getValue().isFirst()).isTrue();
        assertThat(captor.getValue().previousBidderId()).isNull();
    }

    @Test
    void 재입찰은_이전_입찰_정보가_이벤트에_포함() {
        UUID previousBidderId = UUID.randomUUID();
        Bid previousBid = Bid.place(auction, previousBidderId, new BigDecimal("11000"));
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("12000"));

        given(auctionRepository.findByIdWithLock(auctionId)).willReturn(auction);
        given(bidRepository.findActiveByAuctionId(auctionId)).willReturn(Optional.of(previousBid));
        given(bidRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        bidCreateService.place(auctionId, bidderId, request);

        ArgumentCaptor<BidFeeChargeRequest> captor = ArgumentCaptor.forClass(BidFeeChargeRequest.class);
        then(bidFeeChargeEventPublisher).should(times(1)).publish(captor.capture());
        assertThat(captor.getValue().isFirst()).isFalse();
        assertThat(captor.getValue().previousBidderId()).isEqualTo(previousBidderId);
    }
}
