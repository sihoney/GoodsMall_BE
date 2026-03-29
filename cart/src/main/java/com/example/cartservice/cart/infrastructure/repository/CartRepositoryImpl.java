package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public Optional<Cart> findById(UUID cartId) {
        return cartJpaRepository.findById(cartId);
    }

    @Override
    public Optional<Cart> findByMemberId(UUID memberId) {
        return cartJpaRepository.findByMemberId(memberId);
    }

    @Override
    public void delete(Cart cart) {
        cartJpaRepository.delete(cart);
    }

    @Override
    public boolean existsById(UUID cartId) {
        return cartJpaRepository.existsById(cartId);
    }

    @Override
    public boolean existsByMemberId(UUID memberId) {
        return cartJpaRepository.existsByMemberId(memberId);
    }
}
