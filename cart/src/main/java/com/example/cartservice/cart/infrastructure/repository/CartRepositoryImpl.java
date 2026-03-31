package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import com.example.cartservice.cart.domain.repository.CartRepository;
import java.util.List;
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
    public List<Cart> findAllByMemberId(UUID memberId) {
        return cartJpaRepository.findAllByMemberId(memberId);
    }

    @Override
    public void deleteAllByMemberIdAndCartIdIn(UUID memberId, List<UUID> cartIds) {
        cartJpaRepository.deleteAllByMemberIdAndCartIdIn(memberId, cartIds);
    }

    @Override
    public boolean existsByMemberIdAndProductId(UUID memberId, UUID productId) {
        return cartJpaRepository.existsByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public void deleteAllByMemberId(UUID memberId) {
        cartJpaRepository.deleteAllByMemberId(memberId);
    }

    @Override
    public long countCartItems(UUID memberId) {
        return cartJpaRepository.countByMemberId(memberId);
    }

    @Override
    public void deleteByMemberIdAndProductIdIn(UUID memberId, List<UUID> productIds) {
        cartJpaRepository.deleteByMemberIdAndProductIdIn(memberId, productIds);
    }
}
