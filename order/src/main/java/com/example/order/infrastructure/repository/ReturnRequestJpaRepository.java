package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReturnRequestJpaRepository extends JpaRepository<ReturnRequest, UUID> {

    boolean existsByOrderItem_OrderItemId(UUID orderItemId);

    List<ReturnRequest> findByStatusAndPickedUpAtBefore(ReturnRequestStatus status, LocalDateTime threshold);

    Page<ReturnRequest> findBySellerIdAndStatus(UUID sellerId, ReturnRequestStatus status, Pageable pageable);
}
