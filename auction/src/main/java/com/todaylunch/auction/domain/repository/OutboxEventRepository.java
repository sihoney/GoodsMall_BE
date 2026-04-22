package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findAllByStatus(OutboxEventStatus status);

    int changeToPublishedIfPending(UUID id);
}
