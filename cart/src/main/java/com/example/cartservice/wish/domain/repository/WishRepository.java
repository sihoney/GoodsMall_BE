package com.example.cartservice.wish.domain.repository;

import com.example.cartservice.wish.domain.entity.Wish;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WishRepository {

    Wish save(Wish wish);

    Optional<Wish> findById(UUID wishId);

    List<Wish> findByMemberId(UUID memberId);

    Optional<Wish> findByMemberIdAndProductId(UUID memberId, UUID productId);

    void delete(Wish wish);

    void deleteByMemberIdAndProductId(UUID memberId, UUID productId);

    boolean existsById(UUID wishId);

    boolean existsByMemberIdAndProductId(UUID memberId, UUID productId);

    long countByMemberId(UUID memberId);
}
