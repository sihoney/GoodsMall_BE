package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.domain.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public boolean existsActiveByOrderItemId(UUID orderItemId) {
        return returnRequestJpaRepository.existsByOrderItem_OrderItemIdAndStatusNotIn(
                orderItemId,
                List.of(ReturnRequestStatus.FAILED)
        );
    }

    @Override
    public Optional<ReturnRequest> findById(UUID returnRequestId) {
        return returnRequestJpaRepository.findById(returnRequestId);
    }

    @Override
    public List<ReturnRequest> findByStatusAndPickedUpAtBefore(ReturnRequestStatus status, LocalDateTime threshold) {
        return returnRequestJpaRepository.findByStatusAndPickedUpAtBefore(status, threshold);
    }

    @Override
    public Page<ReturnRequest> findBySellerIdAndStatus(UUID sellerId, ReturnRequestStatus status, Pageable pageable) {
        return returnRequestJpaRepository.findBySellerIdAndStatus(sellerId, status, pageable);
    }
}
