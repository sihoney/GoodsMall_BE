package com.example.settlement.presentation.controller;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.PartialSettlementAvailableItemResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.application.usecase.PartialSettlementAvailabilityUseCase;
import com.example.settlement.application.usecase.SellerSettlementSearchUseCase;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.SettlementSellerWithdrawalSummaryResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/settlements")
public class SettlementInternalController {

    private final SellerSettlementSearchUseCase sellerSettlementSearchUseCase;
    private final PartialSettlementAvailabilityUseCase partialSettlementAvailabilityUseCase;

    public SettlementInternalController(
            SellerSettlementSearchUseCase sellerSettlementSearchUseCase,
            PartialSettlementAvailabilityUseCase partialSettlementAvailabilityUseCase
    ) {
        this.sellerSettlementSearchUseCase = sellerSettlementSearchUseCase;
        this.partialSettlementAvailabilityUseCase = partialSettlementAvailabilityUseCase;
    }

    @GetMapping("/sellers/{sellerId}/withdrawal-summary")
    public ResponseEntity<ApiResponse<SettlementSellerWithdrawalSummaryResponse>> getSellerWithdrawalSummary(
            @PathVariable UUID sellerId
    ) {
        PagedResult<SellerSettlementListItemResult> pendingSettlements = sellerSettlementSearchUseCase.findSettlements(
                sellerId,
                null,
                SettlementStatus.PENDING,
                null,
                null,
                0,
                1
        );
        PagedResult<SellerSettlementListItemResult> processingSettlements = sellerSettlementSearchUseCase.findSettlements(
                sellerId,
                null,
                SettlementStatus.PROCESSING,
                null,
                null,
                0,
                1
        );
        List<PartialSettlementAvailableItemResult> partialAvailables =
                partialSettlementAvailabilityUseCase.findAvailableItemsForPartialSettlement(sellerId);

        return ResponseEntity.ok(ApiResponse.success(
                new SettlementSellerWithdrawalSummaryResponse(
                        pendingSettlements.totalElements() > 0,
                        processingSettlements.totalElements() > 0,
                        !partialAvailables.isEmpty()
                )
        ));
    }
}
