package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeConfirmCommand;
import com.example.payment.application.dto.ChargeConfirmResult;
import com.example.payment.application.usecase.ChargeConfirmUseCase;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfirmChargeService implements ChargeConfirmUseCase {

    private final ChargeRepository chargeRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public ConfirmChargeService(
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
    public ChargeConfirmResult confirmCharge(ChargeConfirmCommand command) {
        validateCommand(command);

        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isPending()) {
            throw new IllegalStateException("Charge is not pending.");
        }
        if (!Objects.equals(charge.getPgOrderId(), command.pgOrderId())) {
            throw new InvalidChargeRequestException("pgOrderId does not match charge.");
        }
        if (!Objects.equals(charge.getRequestedAmount(), command.amount())) {
            throw new InvalidChargeRequestException("amount does not match charge.");
        }

        TossPaymentGateway.TossPaymentConfirmation confirmation;
        try {
            confirmation = tossPaymentGateway.confirm(
                    command.paymentKey(),
                    command.pgOrderId(),
                    command.amount()
            );
        } catch (PaymentGatewayException e) {
            failCharge(charge, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            failCharge(charge, e.getMessage());
            throw e;
        }

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

    private void validateCommand(ChargeConfirmCommand command) {
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

    private void failCharge(Charge charge, String failureReason) {
        charge.fail(resolveFailureReason(failureReason), timeProvider.now());
        chargeRepository.save(charge);
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Payment confirmation failed.";
        }
        return failureReason;
    }
}
