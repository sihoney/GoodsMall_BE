package com.example.cartservice.wish.presentation.controller;

import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.example.cartservice.wish.application.usecase.WishCreateUseCase;
import com.example.cartservice.wish.application.usecase.WishDeleteUseCase;
import com.example.cartservice.wish.application.usecase.WishSearchUseCase;
import com.example.cartservice.wish.presentation.dto.response.WishListResponse;
import com.example.cartservice.wish.presentation.dto.response.WishToggleResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
@Validated
public class WishController {

    private final WishCreateUseCase wishCreateUseCase;
    private final WishSearchUseCase wishSearchUseCase;
    private final WishDeleteUseCase wishDeleteUseCase;

    @GetMapping
    public ResponseEntity<WishListResponse> getMyWishes(
        @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        WishListResponse response = wishSearchUseCase.getMyWishes(authenticatedMember.memberId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishToggleResponse> toggleWish(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @PathVariable UUID productId
    ) {
        WishToggleResponse response = wishCreateUseCase.toggleWish(authenticatedMember.memberId(), productId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{wishId}/to-cart")
    public ResponseEntity<Void> moveWishToCart(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @PathVariable UUID wishId
    ) {
        wishDeleteUseCase.moveWishToCart(authenticatedMember.memberId(), wishId);
        return ResponseEntity.noContent().build();
    }
}
