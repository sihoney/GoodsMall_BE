package com.example.product.presentation.controller;

import com.example.product.application.usecase.ProductSearchUseCase;
import com.example.product.presentation.dto.response.ProductSellerWithdrawalSummaryResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/products")
@RequiredArgsConstructor
public class ProductInternalController {

    private final ProductSearchUseCase productSearchUseCase;

    @GetMapping("/sellers/{sellerId}/withdrawal-summary")
    public ResponseEntity<ProductSellerWithdrawalSummaryResponse> getSellerWithdrawalSummary(
            @PathVariable UUID sellerId
    ) {
        return ResponseEntity.ok(
                new ProductSellerWithdrawalSummaryResponse(
                        productSearchUseCase.hasActiveProductsBySellerId(sellerId.toString())
                )
        );
    }
}
