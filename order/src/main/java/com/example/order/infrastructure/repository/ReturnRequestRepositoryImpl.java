package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ReturnRequestRepositoryImpl implements ReturnRequestRepository {

    private final ReturnRequestJpaRepository returnRequestJpaRepository;

    @Override
    public List<ReturnRequest> saveAll(List<ReturnRequest> returnRequests) {
        return returnRequestJpaRepository.saveAll(returnRequests);
    }

    @Override
    public boolean existsByOrderItemId(UUID orderItemId) {
        return returnRequestJpaRepository.existsByOrderItem_OrderItemId(orderItemId);
    }
}
