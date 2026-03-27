package com.example.member.application.service;

import com.example.member.application.usecase.SellerUsecase;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.Seller;
import com.example.member.domain.enumtype.MemberRole;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.SellerRepository;
import com.example.member.presentation.dto.SellerRegisterRequest;
import com.example.member.presentation.dto.SellerRegisterResponse;
import com.example.member.presentation.dto.SellerResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerService implements SellerUsecase {

    private final SellerRepository sellerRepository;
    private final MemberRepository memberRepository;

    // 판매자 등록
    @Transactional
    @Override
    public SellerRegisterResponse registerSeller(
            UUID memberId, 
            SellerRegisterRequest request
        ) {
        validateRegisterRequest(request);
        Member member = getMember(memberId);

        if (sellerRepository.existsByMemberId(memberId)) {
            throw new SellerAlreadyRegisteredException();
        }

        // 판매자 엔티티 생성 및 저장
        LocalDateTime now = LocalDateTime.now();
        Seller seller = Seller.create(
                UUID.randomUUID(),
                memberId,
                normalizeRequired(request.bankName(), "bankName"),
                normalizeRequired(request.account(), "account"),
                now
        );

        // 회원의 역할을 SELLER로 변경
        member.changeRole(MemberRole.SELLER, now);

        return SellerRegisterResponse.from(sellerRepository.save(seller));
    }

    // `판매자 정보 조회
    @Override
    public SellerResponse getCurrentSeller(UUID memberId) {
        getMember(memberId);
        Seller seller = sellerRepository.findByMemberId(memberId)
                .orElseThrow(SellerNotFoundException::new);
        return SellerResponse.from(seller);
    }

    private void validateRegisterRequest(SellerRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Seller register request body is required.");
        }
    }

    private Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
