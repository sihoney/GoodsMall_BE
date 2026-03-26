package com.example.payment.application.service;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.dto.CreateWalletResult;
import com.example.payment.application.usecase.CreateWalletUseCase;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.exception.InvalidChargeRequestException;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
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

    private CreateWalletResult createNewWallet(CreateWalletCommand command) {
        Wallet wallet = Wallet.create(
                identifierGenerator.generateUuid(),
                command.memberId(),
                0L,
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

    private void validateCommand(CreateWalletCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("memberId is required.");
        }
        if (command.createdAt() == null) {
            throw new InvalidChargeRequestException("createdAt is required.");
        }
    }
}
