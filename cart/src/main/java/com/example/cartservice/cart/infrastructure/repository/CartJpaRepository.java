package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    List<Cart> findAllByMemberId(UUID memberId);
    Optional<Cart> findByMemberIdAndProductId(UUID memberId, UUID productId);
    boolean existsByMemberIdAndProductId(UUID memberId, UUID productId);
    void deleteAllByCartItemIdIn(List<UUID> cartItemIds);
}
