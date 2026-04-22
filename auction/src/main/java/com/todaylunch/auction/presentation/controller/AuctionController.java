package com.todaylunch.auction.presentation.controller;

import com.todaylunch.auction.application.usecase.AuctionCreateUseCase;
import com.todaylunch.auction.application.usecase.AuctionSearchUseCase;
import com.todaylunch.auction.domain.enumtype.AuctionStatus;
import com.todaylunch.auction.presentation.dto.request.AuctionCreateRequest;
import com.todaylunch.auction.presentation.dto.response.ApiResponse;
import com.todaylunch.auction.presentation.dto.response.AuctionResponse;
import com.todaylunch.auction.presentation.dto.response.PagedResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionCreateUseCase auctionCreateUseCase;
    private final AuctionSearchUseCase auctionSearchUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<AuctionResponse>> create(
            @CurrentMember AuthenticatedMember member,
            @Valid @RequestBody AuctionCreateRequest request
    ) {
        AuctionResponse response = auctionCreateUseCase.create(member.memberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{auctionId:[0-9a-fA-F-]{36}}")
    public ResponseEntity<ApiResponse<AuctionResponse>> findById(@PathVariable UUID auctionId) {
        AuctionResponse response = auctionSearchUseCase.findById(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<AuctionResponse>>> search(
            @RequestParam(required = false) AuctionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size
    ) {
        PagedResponse<AuctionResponse> response = auctionSearchUseCase.search(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
