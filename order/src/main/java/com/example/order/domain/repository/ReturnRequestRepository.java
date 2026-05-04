package com.example.order.domain.repository;

import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReturnRequestRepository {

    List<ReturnRequest> saveAll(List<ReturnRequest> returnRequests);

    boolean existsActiveByOrderItemId(UUID orderItemId);

    Optional<ReturnRequest> findById(UUID returnRequestId);

    List<ReturnRequest> findByStatusAndPickedUpAtBefore(ReturnRequestStatus status, LocalDateTime threshold);

    Page<ReturnRequest> findBySellerIdAndStatus(UUID sellerId, ReturnRequestStatus status, Pageable pageable);
}
