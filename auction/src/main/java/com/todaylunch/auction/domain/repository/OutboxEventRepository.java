package com.todaylunch.auction.domain.repository;

import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
import java.util.List;

public interface OutboxEventRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

    List<OutboxEvent> findAllByStatus(OutboxEventStatus status);
}
