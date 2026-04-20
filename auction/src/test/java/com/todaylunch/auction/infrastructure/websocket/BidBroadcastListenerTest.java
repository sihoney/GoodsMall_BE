package com.todaylunch.auction.infrastructure.websocket;

import static org.mockito.Mockito.verify;

import com.todaylunch.auction.application.event.BidPlacedEvent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class BidBroadcastListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BidBroadcastListener listener;

    @Test
    void 이벤트가_들어오면_해당_경매_토픽으로_전송된다() {
        UUID auctionId = UUID.randomUUID();
        BidPlacedEvent event = new BidPlacedEvent(
                auctionId,
                UUID.randomUUID(),
                new BigDecimal("11000"),
                LocalDateTime.now().plusMinutes(30)
        );

        listener.bidPlaced(event);

        verify(messagingTemplate).convertAndSend("/topic/auctions/" + auctionId, event);
    }
}
