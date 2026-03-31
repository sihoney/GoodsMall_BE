package com.example.cartservice.cart.presentation.controller;

import com.example.cartservice.cart.application.usecase.CartDeleteUseCase;
import com.example.cartservice.cart.application.usecase.CartSearchUseCase;
import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.cart.presentation.dto.request.DeleteCartItemsRequest;
import com.example.cartservice.cart.presentation.dto.request.DeleteOrderedItemsRequest;
import com.example.cartservice.cart.presentation.dto.request.UpdateCartItemRequest;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
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

    /**
     * 장바구니 조회 API
     *
     * @param authenticatedMember 인증된 회원 정보 -> GateWay 전달
     * @return 회원의 장바구니 목록 (200 OK)
     */
    @GetMapping
    public ResponseEntity<CartResponse> findCart(
        @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        CartResponse response = cartSearchUseCase.findCart(authenticatedMember.memberId());
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 전체 비우기 API
     *
     * @param authenticatedMember 인증된 회원 정보 -> GateWay 전달
     * @return 204 No Content
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        cartDeleteUseCase.clearCart(authenticatedMember.memberId());
        return ResponseEntity.noContent().build();
    }

    /**
     * 장바구니 상품 단건 추가 API
     *
     * @param authenticatedMember 인증된 회원 정보 -> GateWay 전달
     * @param request 추가할 상품 ID, 수량 (productId, quantity)
     * @return 갱신된 장바구니 (201 CREATED)
     */
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addCartItem(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                cartUpdateUseCase.addCartItem(authenticatedMember.memberId(), request));
    }


    /**
     * 장바구니 상품 수량 변경 API
     *
     * @param authenticatedMember 인증된 회원 정보 -> GateWay 전달
     * @param cartId 수량 변경할 장바구니 항목 ID
     * @param request 변경할 수량 (quantity)
     * @return 갱신된 장바구니 (200 OK)
     */
    @PatchMapping("/items/{cartId}")
    public ResponseEntity<CartResponse> updateQuantity(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @PathVariable UUID cartId,
        @Valid @RequestBody UpdateCartItemRequest request
    ) {
        CartResponse response = cartUpdateUseCase.updateCartItem(authenticatedMember.memberId(), cartId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 장바구니 물품 삭제  API
     *
     * @param authenticatedMember 인증된 회원 정보 -> GateWay 전달
     * @param request 삭제할
     * @return 204 No Content
     */

    @DeleteMapping("/items")
    public ResponseEntity<CartResponse> deleteCartItems(
        @CurrentMember AuthenticatedMember authenticatedMember,
        @Valid @RequestBody DeleteCartItemsRequest request
    ) {
        CartResponse response = cartDeleteUseCase.deleteCartItems(authenticatedMember.memberId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 완료 후 장바구니 상품 제거 API -> 추후 Kafka Consumer로 전환 예정
     *
     * @param request 회원 ID, 주문된 상품 ID 목록 (memberId, productIds)
     * @return 204 No Content
     */
    @DeleteMapping("/complete-ordered")
    public ResponseEntity<Void> deleteOrderedItems(
        @Valid @RequestBody DeleteOrderedItemsRequest request
    ) {
        cartDeleteUseCase.deleteOrderedItems(request.getMemberId(), request.getProductIds());
        return ResponseEntity.noContent().build();
    }
}
