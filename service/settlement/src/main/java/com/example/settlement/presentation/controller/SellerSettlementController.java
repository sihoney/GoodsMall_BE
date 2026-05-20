package com.example.settlement.presentation.controller;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.application.usecase.SellerSettlementSearchUseCase;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.PagedResponse;
import com.example.settlement.presentation.dto.response.SellerSettlementDetailResponse;
import com.example.settlement.presentation.dto.response.SellerSettlementListItemResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/settlements/seller")
@Tag(name = "Seller Settlement", description = "판매자 정산 조회 API")
public class SellerSettlementController {

    private final SellerSettlementSearchUseCase sellerSettlementSearchUseCase;

    public SellerSettlementController(SellerSettlementSearchUseCase sellerSettlementSearchUseCase) {
        this.sellerSettlementSearchUseCase = sellerSettlementSearchUseCase;
    }

    @GetMapping
    @Operation(summary = "판매자 정산 목록 조회")
    public ResponseEntity<ApiResponse<PagedResponse<SellerSettlementListItemResponse>>> findSettlements(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(required = false) SettlementType type,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validateSellerRole(authenticatedMember);

        PagedResult<SellerSettlementListItemResult> result = sellerSettlementSearchUseCase.findSettlements(
                authenticatedMember.memberId(),
                type,
                status,
                year,
                month,
                page,
                size
        );
        List<SellerSettlementListItemResponse> items = result.items().stream()
                .map(SellerSettlementListItemResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(new PagedResponse<>(
                items,
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages(),
                result.hasNext()
        )));
    }

    @GetMapping("/{settlementId}")
    @Operation(summary = "판매자 정산 상세 조회")
    public ResponseEntity<ApiResponse<SellerSettlementDetailResponse>> findSettlementDetail(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable UUID settlementId
    ) {
        validateSellerRole(authenticatedMember);

        return ResponseEntity.ok(ApiResponse.success(SellerSettlementDetailResponse.from(
                sellerSettlementSearchUseCase.findSettlementDetail(authenticatedMember.memberId(), settlementId)
        )));
    }

    private void validateSellerRole(AuthenticatedMember authenticatedMember) {
        if (authenticatedMember.role() != MemberRole.SELLER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "seller role is required.");
        }
    }
}
