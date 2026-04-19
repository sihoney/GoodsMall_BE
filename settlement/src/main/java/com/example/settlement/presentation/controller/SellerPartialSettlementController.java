package com.example.settlement.presentation.controller;

import com.example.settlement.application.dto.PartialSettlementExecutionCommand;
import com.example.settlement.application.usecase.PartialSettlementExecutionUseCase;
import com.example.settlement.application.usecase.PartialSettlementAvailabilityUseCase;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.PartialSettlementAvailableItemResponse;
import com.example.settlement.presentation.dto.request.PartialSettlementExecutionRequest;
import com.example.settlement.presentation.dto.response.PartialSettlementExecutionResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * 판매자 부분 정산 조회 API 진입점이다.
 */
@RestController
@RequestMapping("/api/settlements/seller/partial-settlements")
@Tag(name = "Seller Partial Settlement", description = "판매자 부분 정산 조회 API")
public class SellerPartialSettlementController {

    private final PartialSettlementAvailabilityUseCase partialSettlementAvailabilityUseCase;
    private final PartialSettlementExecutionUseCase partialSettlementExecutionUseCase;

    public SellerPartialSettlementController(
            PartialSettlementAvailabilityUseCase partialSettlementAvailabilityUseCase,
            PartialSettlementExecutionUseCase partialSettlementExecutionUseCase
    ) {
        this.partialSettlementAvailabilityUseCase = partialSettlementAvailabilityUseCase;
        this.partialSettlementExecutionUseCase = partialSettlementExecutionUseCase;
    }

    @GetMapping("/available")
    @Operation(summary = "판매자 부분 정산 가능 항목 조회")
    public ResponseEntity<ApiResponse<List<PartialSettlementAvailableItemResponse>>> findAvailableItemsForPartialSettlement(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        validateSellerRole(authenticatedMember);

        List<PartialSettlementAvailableItemResponse> response = partialSettlementAvailabilityUseCase
                .findAvailableItemsForPartialSettlement(authenticatedMember.memberId()).stream()
                .map(PartialSettlementAvailableItemResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    @Operation(summary = "판매자 부분 정산 실행")
    public ResponseEntity<ApiResponse<PartialSettlementExecutionResponse>> executePartialSettlement(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @Valid @RequestBody PartialSettlementExecutionRequest request
    ) {
        validateSellerRole(authenticatedMember);

        PartialSettlementExecutionResponse response = PartialSettlementExecutionResponse.from(
                partialSettlementExecutionUseCase.executePartialSettlement(
                        new PartialSettlementExecutionCommand(
                                authenticatedMember.memberId(),
                                request.settlementItemIds()
                        )
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    private void validateSellerRole(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember.role() != MemberRole.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "seller role is required.");
        }
    }
}
