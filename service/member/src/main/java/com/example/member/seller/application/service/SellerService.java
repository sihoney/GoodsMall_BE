package com.example.member.seller.application.service;

import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.seller.application.dto.command.SellerRegisterCommand;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.seller.application.dto.result.SellerResult;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.seller.application.port.out.SellerPersistencePort;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.seller.application.port.in.SellerUsecase;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerService implements SellerUsecase {

    private final SellerPersistencePort sellerPersistencePort;
    private final MemberPersistencePort memberPersistencePort;
    private final AccountVerificationUsecase accountVerificationUsecase;

    @Transactional
    @Override
    public AccountVerificationSendResult registerSeller(UUID memberId, SellerRegisterCommand command) {
        validateRegisterCommand(command);
        getMember(memberId);

        if (sellerPersistencePort.existsByMemberId(memberId)) {
            throw new SellerAlreadyRegisteredException();
        }

        return accountVerificationUsecase.createAccountVerification(
                memberId,
                new AccountVerificationCreateCommand(
                        normalizeRequired(command.bankName(), "bankName"),
                        normalizeRequired(command.account(), "account")
                )
        );
    }

    @Override
    public SellerResult getCurrentSeller(UUID memberId) {
        getMember(memberId);
        Seller seller = sellerPersistencePort.findByMemberId(memberId)
                .orElseThrow(SellerNotFoundException::new);

        return new SellerResult(
                seller.getSellerId(),
                seller.getMemberId(),
                seller.getBankName(),
                seller.getAccount(),
                seller.getApprovedAt()
        );
    }

    private void validateRegisterCommand(SellerRegisterCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("판매자 등록 요청은 필수입니다.");
        }
    }

    private Member getMember(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}

