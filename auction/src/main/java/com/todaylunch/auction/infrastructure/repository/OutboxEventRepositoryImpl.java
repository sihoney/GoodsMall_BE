package com.todaylunch.auction.infrastructure.repository;

import com.todaylunch.auction.domain.entity.OutboxEvent;
import com.todaylunch.auction.domain.enumtype.OutboxEventStatus;
import com.todaylunch.auction.domain.repository.OutboxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final OutboxEventJpaRepository jpaRepository;

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return jpaRepository.save(outboxEvent);
    }

    @Override
    public List<OutboxEvent> findAllByStatus(OutboxEventStatus status) {
        return jpaRepository.findAllByStatusOrderByCreatedAtAsc(status);
    }

}
