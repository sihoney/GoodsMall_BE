package com.example.cartservice.wish.infrastructure.repository;

import com.example.cartservice.wish.domain.entity.Wish;
import com.example.cartservice.wish.domain.repository.WishRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WishRepositoryImpl implements WishRepository {

    private final WishJpaRepository wishJpaRepository;

    @Override
    public Wish save(Wish wish) {
        return wishJpaRepository.save(wish);
    }

    @Override
    public Optional<Wish> findById(UUID wishId) {
        return wishJpaRepository.findById(wishId);
    }

    @Override
    public List<Wish> findByMemberId(UUID memberId) {
        return wishJpaRepository.findByMemberId(memberId);
    }

    @Override
    public Optional<Wish> findByMemberIdAndProductId(UUID memberId, UUID productId) {
        return wishJpaRepository.findByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public void delete(Wish wish) {
        wishJpaRepository.delete(wish);
    }

    @Override
    public void deleteByMemberIdAndProductId(UUID memberId, UUID productId) {
        wishJpaRepository.deleteByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public boolean existsById(UUID wishId) {
        return wishJpaRepository.existsById(wishId);
    }

    @Override
    public boolean existsByMemberIdAndProductId(UUID memberId, UUID productId) {
        return wishJpaRepository.existsByMemberIdAndProductId(memberId, productId);
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return wishJpaRepository.countByMemberId(memberId);
    }
}
