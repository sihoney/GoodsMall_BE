package com.example.member.seller.application.service;

import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.seller.application.dto.command.SellerRegisterCommand;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.seller.application.dto.result.SellerResult;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.seller.application.port.out.SellerPersistencePort;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.seller.application.port.in.SellerUsecase;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.seller.exception.SellerErrorCode;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SellerService implements SellerUsecase {

    private final SellerPersistencePort sellerPersistencePort;
    private final MemberPersistencePort memberPersistencePort;
    private final AccountVerificationUsecase accountVerificationUsecase;

    @Transactional
    @Override
    public AccountVerificationSendResult registerSeller(UUID memberId, SellerRegisterCommand command) {
        getMember(memberId);

        if (sellerPersistencePort.existsByMemberId(memberId)) {
            throw new BusinessException(SellerErrorCode.SELLER_ALREADY_REGISTERED);
        }

        return accountVerificationUsecase.createAccountVerification(
                memberId,
                new AccountVerificationCreateCommand(
                        command.bankName().trim(),
                        command.account().trim()
                )
        );
    }

    @Override
    public SellerResult getCurrentSeller(UUID memberId) {
        getMember(memberId);
        Seller seller = sellerPersistencePort.findByMemberId(memberId)
                .orElseThrow(() -> new BusinessException(SellerErrorCode.SELLER_NOT_FOUND));

        return new SellerResult(
                seller.getSellerId(),
                seller.getMemberId(),
                seller.getBankName(),
                seller.getAccount(),
                seller.getApprovedAt()
        );
    }

    private Member getMember(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
