package com.example.cartservice.cart.infrastructure.repository;

import com.example.cartservice.cart.domain.entity.Cart;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CartJpaRepository extends JpaRepository<Cart, UUID> {
    List<Cart> findAllByMemberId(UUID memberId);
    Optional<Cart> findByMemberIdAndProductId(UUID memberId, UUID productId);
    boolean existsByMemberIdAndProductId(UUID memberId, UUID productId);
    void deleteAllByCartItemIdIn(List<UUID> cartItemIds);
    long countByMemberId(UUID memberId);
    void deleteAllByMemberId(UUID memberId);
    void deleteByMemberIdAndProductIdIn(UUID memberId, List<UUID> productIds);
}
