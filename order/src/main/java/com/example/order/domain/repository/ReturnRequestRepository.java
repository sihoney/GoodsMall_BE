package com.example.order.domain.repository;

import com.example.order.domain.entity.ReturnRequest;

import java.util.List;
import java.util.UUID;

public interface ReturnRequestRepository {

    List<ReturnRequest> saveAll(List<ReturnRequest> returnRequests);

    boolean existsByOrderItemId(UUID orderItemId);
}
