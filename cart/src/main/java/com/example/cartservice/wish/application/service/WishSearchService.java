package com.example.cartservice.wish.application.service;

import com.example.cartservice.wish.application.usecase.WishSearchUseCase;
import com.example.cartservice.wish.domain.entity.Wish;
import com.example.cartservice.wish.domain.repository.WishRepository;
import com.example.cartservice.wish.presentation.dto.response.WishListResponse;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class WishSearchService implements WishSearchUseCase {

    private final WishRepository wishRepository;

    @Override
    public WishListResponse getMyWishes(UUID memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }

        List<Wish> wishes = wishRepository.findByMemberId(memberId);
        List<UUID> productIds = wishes.stream()
            .map(Wish::getProductId)
            .collect(Collectors.toList());

        return new WishListResponse(productIds, productIds.size());
    }
}
