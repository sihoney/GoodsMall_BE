package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findAllByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

}
