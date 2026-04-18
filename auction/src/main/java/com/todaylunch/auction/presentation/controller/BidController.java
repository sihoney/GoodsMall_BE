package com.todaylunch.auction.presentation.controller;

import com.todaylunch.auction.application.usecase.BidPlaceUseCase;
import com.todaylunch.auction.presentation.dto.request.BidPlaceRequest;
import com.todaylunch.auction.presentation.dto.response.BidResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidPlaceUseCase bidPlaceUseCase;

    @PostMapping
    public ResponseEntity<BidResponse> place(
            @PathVariable UUID auctionId,
            @CurrentMember AuthenticatedMember member,
            @Valid @RequestBody BidPlaceRequest request
    ) {
        BidResponse response = bidPlaceUseCase.place(auctionId, member.memberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
