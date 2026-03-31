package com.example.cartservice.cart.domain.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(UUID cartId);

    List<Cart> findAllByMemberId(UUID memberId);

    void deleteAllByMemberIdAndCartIdIn(UUID memberId, List<UUID> cartIds);

    boolean existsByMemberIdAndProductId(UUID memberId, UUID productId);

    long countCartItems(UUID memberId);

    void deleteAllByMemberId(UUID memberId);

    void deleteByMemberIdAndProductIdIn(UUID memberId, List<UUID> productIds);
}
