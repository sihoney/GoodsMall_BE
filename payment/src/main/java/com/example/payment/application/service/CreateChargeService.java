package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateChargeService implements ChargeCreateUseCase {

    private final ChargeRepository chargeRepository;
    private final WalletRepository walletRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public CreateChargeService(
            ChargeRepository chargeRepository,
            WalletRepository walletRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.chargeRepository = chargeRepository;
        this.walletRepository = walletRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public ChargeCreateResult createCharge(ChargeCreateCommand command) {
        validateCommand(command);

        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseThrow(WalletNotFoundException::new);

        UUID chargeId = identifierGenerator.generateUuid();
        String pgOrderId = generatePgOrderId(chargeId);
        LocalDateTime requestedAt = timeProvider.now();

        Charge charge = Charge.create(
                chargeId,
                command.memberId(),
                wallet.getWalletId(),
                command.amount(),
                command.pgProvider(),
                pgOrderId,
                requestedAt
        );

        chargeRepository.save(charge);

        return new ChargeCreateResult(
                charge.getChargeId(),
                wallet.getWalletId(),
                charge.getPgOrderId(),
                charge.getRequestedAmount(),
                charge.getPgProvider(),
                charge.getChargeStatus()
        );
    }

    private String generatePgOrderId(UUID chargeId) {
        return "CHARGE-" + chargeId;
    }

    private void validateCommand(ChargeCreateCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("memberId is required.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new InvalidChargeRequestException("amount must be positive.");
        }
        if (command.pgProvider() == null) {
            throw new InvalidChargeRequestException("pgProvider is required.");
        }
    }
}
