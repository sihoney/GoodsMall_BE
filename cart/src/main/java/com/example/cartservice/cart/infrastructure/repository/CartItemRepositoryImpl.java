package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.CartItem;
import com.example.cartservice.cart.domain.repository.CartItemRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartItemRepositoryImpl implements CartItemRepository {

    private final CartItemJpaRepository cartItemJpaRepository;

    @Override
    public CartItem save(CartItem cartItem) {
        return cartItemJpaRepository.save(cartItem);
    }

    @Override
    public Optional<CartItem> findById(UUID cartItemId) {
        return cartItemJpaRepository.findById(cartItemId);
    }

    @Override
    public List<CartItem> findByCartId(UUID cartId) {
        return cartItemJpaRepository.findByCartId(cartId);
    }

    @Override
    public Optional<CartItem> findByCartIdAndProductId(UUID cartId, UUID productId) {
        return cartItemJpaRepository.findByCartIdAndProductId(cartId, productId);
    }

    @Override
    public void delete(CartItem cartItem) {
        cartItemJpaRepository.delete(cartItem);
    }

    @Override
    public void deleteAllByCartId(UUID cartId) {
        cartItemJpaRepository.deleteAllByCartId(cartId);
    }

    @Override
    public boolean existsById(UUID cartItemId) {
        return cartItemJpaRepository.existsById(cartItemId);
    }
}
