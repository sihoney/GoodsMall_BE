package com.example.payment.application.service;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.dto.CreateWalletResult;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 회원의 payment wallet 생성 유스케이스를 담당한다.
 * 이미 wallet이 있으면 기존 정보를 반환하고, 없으면 새 wallet을 생성한다.
 */
public class WalletCreationService implements CreateWalletUseCase {

    private final WalletRepository walletRepository;
    private final IdentifierGenerator identifierGenerator;

    public WalletCreationService(
            WalletRepository walletRepository,
            IdentifierGenerator identifierGenerator
    ) {
        this.walletRepository = walletRepository;
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    /**
     * memberId 기준 wallet 존재 여부를 먼저 확인한 뒤 없을 때만 생성한다.
     * 중복 생성 경쟁이 발생해도 기존 wallet을 다시 조회해 멱등하게 응답한다.
     */
    public CreateWalletResult createWallet(CreateWalletCommand command) {
        validateCommand(command);

        return walletRepository.findByMemberId(command.memberId())
                .map(wallet -> new CreateWalletResult(
                        wallet.getWalletId(),
                        wallet.getMemberId(),
                        wallet.getBalance(),
                        false
                ))
                .orElseGet(() -> createNewWallet(command));
    }

    /**
     * wallet을 새로 저장하되, 동시 생성으로 unique 제약이 충돌하면 기존 wallet을 재조회해 반환한다.
     */
    private CreateWalletResult createNewWallet(CreateWalletCommand command) {
        Wallet wallet = Wallet.create(
                identifierGenerator.generateUuid(),
                command.memberId(),
                java.math.BigDecimal.ZERO,
                command.createdAt(),
                command.createdAt()
        );
        Wallet savedWallet;
        try {
            savedWallet = walletRepository.save(wallet);
        } catch (DataIntegrityViolationException e) {
            Wallet existingWallet = walletRepository.findByMemberId(command.memberId())
                    .orElseThrow(() -> e);
            return new CreateWalletResult(
                    existingWallet.getWalletId(),
                    existingWallet.getMemberId(),
                    existingWallet.getBalance(),
                    false
            );
        }
        return new CreateWalletResult(
                savedWallet.getWalletId(),
                savedWallet.getMemberId(),
                savedWallet.getBalance(),
                true
        );
    }

    /**
     * wallet 생성에 필요한 최소 입력만 검증한다.
     */
    private void validateCommand(CreateWalletCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("회원 ID는 필수입니다.");
        }
        if (command.createdAt() == null) {
            throw new InvalidChargeRequestException("생성 시각은 필수입니다.");
        }
    }
}
