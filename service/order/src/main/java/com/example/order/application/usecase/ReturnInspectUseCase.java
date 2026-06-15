package com.example.order.application.usecase;

import com.example.order.presentation.dto.request.ReturnInspectRequest;
import com.example.order.presentation.dto.response.ReturnInspectResponse;

import java.util.UUID;

public interface ReturnInspectUseCase {

    ReturnInspectResponse inspect(UUID returnRequestId, UUID sellerMemberId, ReturnInspectRequest request);
}
