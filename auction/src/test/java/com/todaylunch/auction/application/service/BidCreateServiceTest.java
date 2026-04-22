package com.todaylunch.auction.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.domain.entity.Auction;
import com.todaylunch.auction.domain.entity.Bid;
import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.repository.AuctionRepository;
import com.todaylunch.auction.domain.repository.BidRepository;
import com.todaylunch.auction.domain.repository.OutboxEventRepository;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class BidCreateServiceTest {

    @Mock
    AuctionRepository auctionRepository;
    @Mock
    BidRepository bidRepository;
    @Mock
    OutboxEventRepository outboxEventRepository;
    @Mock
    ApplicationEventPublisher applicationEventPublisher;
    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

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

        given(outboxEventRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        BidResponse response = bidCreateService.place(auctionId, bidderId, request);

        assertThat(response.status()).isEqualTo(BidStatus.PENDING);
        then(outboxEventRepository).should(times(1)).save(any(OutboxEvent.class));
    }

    @Test
    void 재입찰은_이전_입찰_정보가_이벤트에_포함() {
        UUID previousBidderId = UUID.randomUUID();
        Bid previousBid = Bid.place(auction, previousBidderId, new BigDecimal("11000"));
        BidPlaceRequest request = new BidPlaceRequest(new BigDecimal("12000"));

        given(auctionRepository.findByIdWithLock(auctionId)).willReturn(auction);
        given(bidRepository.findActiveByAuctionId(auctionId)).willReturn(Optional.of(previousBid));
        given(bidRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(outboxEventRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        bidCreateService.place(auctionId, bidderId, request);

        then(outboxEventRepository).should(times(1)).save(any(OutboxEvent.class));
    }
}
