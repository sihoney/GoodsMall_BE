package com.example.product.application.usecase;

import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import java.util.List;

public interface ProductCheckUseCase {
    List<ProductAvailabilityResponse> checkAvailability(List<ProductCheckRequest> productRequests);
}
