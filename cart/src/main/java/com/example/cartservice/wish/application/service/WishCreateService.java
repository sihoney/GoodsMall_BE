package com.example.cartservice.wish.application.service;

import com.example.cartservice.wish.application.usecase.WishCreateUseCase;
import com.example.cartservice.wish.domain.entity.Wish;
import com.example.cartservice.wish.domain.repository.WishRepository;
import com.example.cartservice.wish.presentation.dto.response.WishToggleResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WishCreateService implements WishCreateUseCase {

    private final WishRepository wishRepository;

    @Override
    public WishToggleResponse toggleWish(UUID memberId, UUID productId) {
        if (wishRepository.existsByMemberIdAndProductId(memberId, productId)) {
            wishRepository.deleteByMemberIdAndProductId(memberId, productId);
            return new WishToggleResponse(false);
        }

        Wish wish = Wish.create(memberId, productId);
        wishRepository.save(wish);

        return new WishToggleResponse(true);
    }
}
