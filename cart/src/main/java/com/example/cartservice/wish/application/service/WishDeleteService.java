package com.example.cartservice.wish.application.service;

import com.example.cartservice.cart.application.usecase.CartUpdateUseCase;
import com.example.cartservice.cart.presentation.dto.request.AddCartItemRequest;
import com.example.cartservice.wish.application.usecase.WishDeleteUseCase;
import com.example.cartservice.wish.domain.entity.Wish;
import com.example.cartservice.wish.domain.repository.WishRepository;
import com.example.cartservice.wish.presentation.exception.MemberNotAuthorizedException;
import com.example.cartservice.wish.presentation.exception.WishNotFoundException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class WishDeleteService implements WishDeleteUseCase {

    private final WishRepository wishRepository;
    private final CartUpdateUseCase cartUpdateUseCase;

    @Override
    public void moveWishToCart(UUID memberId, UUID wishId) {
        if (memberId == null) {
            throw new IllegalArgumentException("회원 ID는 필수입니다");
        }
        if (wishId == null) {
            throw new IllegalArgumentException("찜 ID는 필수입니다");
        }

        Wish wish = wishRepository.findById(wishId)
            .orElseThrow(WishNotFoundException::new);

        validateWishOwnership(wish, memberId);

        // 장바구니에 상품 추가 (수량 1로 기본)
        AddCartItemRequest request = new AddCartItemRequest(wish.getProductId(), 1);
        cartUpdateUseCase.addCartItem(memberId, request);

        // 찜에서 삭제
        wishRepository.delete(wish);
    }

    private void validateWishOwnership(Wish wish, UUID memberId) {
        if (!wish.getMemberId().equals(memberId)) {
            throw new MemberNotAuthorizedException();
        }
    }
}
