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
import com.example.cartservice.cart.presentation.exception.CartLimitExceededException;
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

    private static final int MAX_CART_ITEMS = 10;

    private final CartRepository cartRepository;

    @Override
    public CartResponse addCartItem(UUID memberId,
                                    AddCartItemRequest request) {
        validateDuplicateItem(memberId, request);
        validateCartItemRange(memberId);

        Cart cart = Cart.create(memberId, request.getProductId(), request.getQuantity());
        cartRepository.save(cart);

        return convertCartResponse(memberId);
    }

    private void validateCartItemRange(UUID memberId) {
        if (cartRepository.countCartItems(memberId) >= MAX_CART_ITEMS) {
            throw new CartLimitExceededException();
        }
    }

    private void validateDuplicateItem(UUID memberId,
                                       AddCartItemRequest request) {
        if (cartRepository.existsByMemberIdAndProductId(memberId, request.getProductId())) {
            throw new CartItemDuplicateException();
        }
    }

    @Override
    public CartResponse updateCartItem(UUID memberId,
                                       UUID cartItemId,
                                       UpdateCartItemRequest request) {
        Cart cart = findCartItem(cartItemId);
        cart.validateOwner(memberId);
        cart.updateQuantity(request.getQuantity());
        cartRepository.save(cart);

        // 다시 해당 회원의 전체 장바구니를 조회해서 반환 하는 형태
        return convertCartResponse(memberId);
    }

    private Cart findCartItem(UUID cartItemId) {
        return cartRepository.findById(cartItemId)
            .orElseThrow(CartItemNotFoundException::new);
    }

    private CartResponse convertCartResponse(UUID memberId) {
        List<Cart> cartItems = cartRepository.findAllByMemberId(memberId);
        List<CartItemResponse> itemResponses = cartItems.stream()
            .map(CartItemResponse::from)
            .collect(Collectors.toList());
        return CartResponse.of(memberId, itemResponses);
    }
}
