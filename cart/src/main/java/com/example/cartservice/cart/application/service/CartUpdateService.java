package com.example.cartservice.cart.application.service;

import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.cart.presentation.dto.request.UpdateCartItemRequest;
import com.example.cartservice.cart.presentation.dto.response.CartItemResponse;
import com.example.cartservice.cart.presentation.dto.response.CartResponse;
import com.example.cartservice.cart.presentation.exception.CartItemDuplicateException;
import com.example.cartservice.cart.presentation.exception.CartItemNotFoundException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CartUpdateService implements CartUpdateUseCase {

    private final CartRepository cartRepository;

    @Override
    public CartResponse addCartItem(UUID memberId, AddCartItemRequest request) {
        validateAddCartItemParams(memberId, request);

        if (cartRepository.existsByMemberIdAndProductId(memberId, request.getProductId())) {
            throw new CartItemDuplicateException();
        }

        Cart cart = Cart.create(memberId, request.getProductId(), request.getQuantity());
        cartRepository.save(cart);

        return getCartResponse(memberId);
    }

    @Override
    public CartResponse updateCartItem(UUID memberId, UUID cartItemId, UpdateCartItemRequest request) {
        validateUpdateCartItemParams(memberId, cartItemId, request);

        Cart cart = findCartItem(cartItemId);
        cart.validateOwner(memberId);
        cart.updateQuantity(request.getQuantity());
        cartRepository.save(cart);

        return getCartResponse(memberId);
    }

    private void validateAddCartItemParams(UUID memberId, AddCartItemRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (request == null || request.getProductId() == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("상품 정보는 필수입니다");
        }
    }

    private void validateUpdateCartItemParams(UUID memberId, UUID cartItemId, UpdateCartItemRequest request) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (cartItemId == null) {
            throw new IllegalArgumentException("장바구니 항목 ID는 필수입니다");
        }
        if (request == null || request.getQuantity() == null) {
            throw new IllegalArgumentException("수량은 필수입니다");
        }
    }

    private Cart findCartItem(UUID cartItemId) {
        return cartRepository.findById(cartItemId)
            .orElseThrow(CartItemNotFoundException::new);
    }

    private CartResponse getCartResponse(UUID memberId) {
        List<Cart> cartItems = cartRepository.findAllByMemberId(memberId);
        List<CartItemResponse> itemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());
        return CartResponse.of(memberId, itemResponses);
    }
}
