package com.todaylunch.auction.infrastructure.websocket;

import com.todaylunch.auction.application.event.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BidBroadcastListener {

    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void bidPlaced(BidPlacedEvent event) {
        messagingTemplate.convertAndSend("/topic/auctions/" + event.auctionId(), event);
    }
}
