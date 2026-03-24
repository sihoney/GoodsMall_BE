package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.exception.ChargeNotFoundException;
import com.example.payment.domain.exception.ChargeStateException;
import com.example.payment.domain.exception.InvalidChargeRequestException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChargeService implements ChargeCreateUseCase, ChargeConfirmUseCase {

    private final ChargeRepository chargeRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public ChargeService(
            ChargeRepository chargeRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            TossPaymentGateway tossPaymentGateway,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.chargeRepository = chargeRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.tossPaymentGateway = tossPaymentGateway;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public ChargeCreateResult createCharge(ChargeCreateCommand command) {
        validateCreateCommand(command);

        LocalDateTime requestedAt = timeProvider.now();
        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseGet(() -> createWallet(command.memberId(), requestedAt));

        UUID chargeId = identifierGenerator.generateUuid();
        String pgOrderId = generatePgOrderId(chargeId);

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

    @Override
    public ChargeConfirmResult confirmCharge(ChargeConfirmCommand command) {
        validateConfirmCommand(command);

        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isPending()) {
            throw new ChargeStateException("Charge is not pending.");
        }
        if (!Objects.equals(charge.getPgOrderId(), command.pgOrderId())) {
            throw new InvalidChargeRequestException("pgOrderId does not match charge.");
        }
        if (!Objects.equals(charge.getRequestedAmount(), command.amount())) {
            throw new InvalidChargeRequestException("amount does not match charge.");
        }

        TossPaymentGateway.TossPaymentConfirmation confirmation = tossPaymentGateway.confirm(
                command.paymentKey(),
                command.pgOrderId(),
                command.amount()
        );

        Wallet wallet = walletRepository.findByWalletId(charge.getWalletId())
                .orElseThrow(WalletNotFoundException::new);

        charge.approve(
                confirmation.approvedAmount(),
                confirmation.paymentKey(),
                confirmation.approvedAt()
        );

        Long balanceAfter = wallet.increaseBalance(confirmation.approvedAmount(), confirmation.approvedAt());

        WalletTransaction walletTransaction = WalletTransaction.charge(
                identifierGenerator.generateUuid(),
                wallet.getWalletId(),
                confirmation.approvedAmount(),
                balanceAfter,
                charge.getChargeId(),
                confirmation.approvedAt()
        );

        chargeRepository.save(charge);
        walletRepository.save(wallet);
        walletTransactionRepository.save(walletTransaction);

        return new ChargeConfirmResult(
                charge.getChargeId(),
                charge.getChargeStatus(),
                charge.getApprovedAmount(),
                wallet.getBalance(),
                charge.getApprovedAt()
        );
    }

    private Wallet createWallet(UUID memberId, LocalDateTime now) {
        Wallet wallet = Wallet.create(
                identifierGenerator.generateUuid(),
                memberId,
                0L,
                now,
                now
        );
        return walletRepository.save(wallet);
    }

    private String generatePgOrderId(UUID chargeId) {
        return "CHARGE-" + chargeId;
    }

    private void validateCreateCommand(ChargeCreateCommand command) {
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

    private void validateConfirmCommand(ChargeConfirmCommand command) {
        if (command.chargeId() == null) {
            throw new InvalidChargeRequestException("chargeId is required.");
        }
        if (command.paymentKey() == null || command.paymentKey().isBlank()) {
            throw new InvalidChargeRequestException("paymentKey is required.");
        }
        if (command.pgOrderId() == null || command.pgOrderId().isBlank()) {
            throw new InvalidChargeRequestException("pgOrderId is required.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new InvalidChargeRequestException("amount must be positive.");
        }
    }
}
