package com.example.member.application.service;

import com.example.member.application.usecase.AccountVerificationUsecase;
import com.example.member.application.usecase.SellerUsecase;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.SellerRepository;
import com.example.member.presentation.dto.AccountVerificationCreateRequest;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import com.example.member.presentation.dto.SellerRegisterRequest;
import com.example.member.presentation.dto.SellerResponse;
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
    private final AccountVerificationUsecase accountVerificationUsecase;

    @Transactional
    @Override
    public AccountVerificationSendResponse registerSeller(UUID memberId, SellerRegisterRequest request) {
        validateRegisterRequest(request);
        getMember(memberId);

        if (sellerRepository.existsByMemberId(memberId)) {
            throw new SellerAlreadyRegisteredException();
        }

        return accountVerificationUsecase.createAccountVerification(
                memberId,
                new AccountVerificationCreateRequest(
                        normalizeRequired(request.bankName(), "bankName"),
                        normalizeRequired(request.account(), "account")
                )
        );
    }

    @Override
    public SellerResponse getCurrentSeller(UUID memberId) {
        getMember(memberId);
        return SellerResponse.from(
                sellerRepository.findByMemberId(memberId)
                        .orElseThrow(SellerNotFoundException::new)
        );
    }

    private void validateRegisterRequest(SellerRegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("판매자 등록 요청 본문은 필수입니다.");
        }
    }

    private Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
