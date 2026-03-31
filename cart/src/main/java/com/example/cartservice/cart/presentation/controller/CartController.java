package com.example.cartservice.cart.presentation.controller;

import com.example.cartservice.cart.application.usecase.CartDeleteUseCase;
import com.example.cartservice.cart.application.usecase.CartSearchUseCase;
import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.request.UpdateCartItemRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartSearchUseCase cartSearchUseCase;
    private final CartUpdateUseCase cartUpdateUseCase;
    private final CartDeleteUseCase cartDeleteUseCase;

    @GetMapping
    public ResponseEntity<CartResponse> getMyCart(
        @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        CartResponse response = cartSearchUseCase.getMyCart(authenticatedMember.memberId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addCartItem(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @RequestBody AddCartItemRequest request
    ) {
        CartResponse response = cartUpdateUseCase.addCartItem(authenticatedMember.memberId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @PathVariable UUID cartItemId,
        @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartUpdateUseCase.updateCartItem(authenticatedMember.memberId(), cartItemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> deleteCartItem(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @PathVariable UUID cartItemId
    ) {
        CartResponse response = cartDeleteUseCase.deleteCartItem(authenticatedMember.memberId(), cartItemId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items")
    public ResponseEntity<CartResponse> deleteCartItems(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @RequestBody DeleteCartItemsRequest request
    ) {
        CartResponse response = cartDeleteUseCase.deleteCartItems(authenticatedMember.memberId(), request);
        return ResponseEntity.ok(response);
    }
}
