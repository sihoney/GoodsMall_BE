package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.ChargeRefundResult;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.exception.ChargeNotFoundException;
import com.example.payment.domain.exception.ChargeStateException;
import com.example.payment.domain.exception.InvalidChargeRequestException;
import com.example.payment.domain.exception.PaymentGatewayException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.ChargeRefundRepository;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.domain.service.TossPaymentGateway;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RefundChargeService implements ChargeRefundUseCase {

    private final ChargeRepository chargeRepository;
    private final ChargeRefundRepository chargeRefundRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final TossPaymentGateway tossPaymentGateway;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public RefundChargeService(
            ChargeRepository chargeRepository,
            ChargeRefundRepository chargeRefundRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            TossPaymentGateway tossPaymentGateway,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.chargeRepository = chargeRepository;
        this.chargeRefundRepository = chargeRefundRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.tossPaymentGateway = tossPaymentGateway;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public ChargeRefundResult refundCharge(ChargeRefundCommand command) {
        validateCommand(command);

        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isSuccess()) {
            throw new ChargeStateException("Charge is not refundable.");
        }
        if (chargeRefundRepository.existsRefundedByChargeId(charge.getChargeId())) {
            throw new ChargeStateException("Charge refund has already been completed.");
        }
        if (charge.getApprovedAmount() == null || charge.getPgPaymentKey() == null) {
            throw new ChargeStateException("Charge refund information is incomplete.");
        }

        LocalDateTime refundRequestedAt = timeProvider.now();
        Wallet wallet = walletRepository.findByWalletId(charge.getWalletId())
                .orElseThrow(WalletNotFoundException::new);

        if (wallet.getBalance() < charge.getApprovedAmount()) {
            throw new ChargeStateException("Charge has already been used and cannot be refunded.");
        }

        TossPaymentGateway.TossPaymentCancellation cancellation;
        try {
            cancellation = tossPaymentGateway.cancel(
                    charge.getPgPaymentKey(),
                    command.refundReason(),
                    charge.getApprovedAmount()
            );
        } catch (PaymentGatewayException e) {
            failRefund(charge, command.refundReason(), refundRequestedAt, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            failRefund(charge, command.refundReason(), refundRequestedAt, e.getMessage());
            throw e;
        }

        Long balanceAfter = wallet.decreaseBalance(cancellation.canceledAmount(), cancellation.canceledAt());
        WalletTransaction walletTransaction = WalletTransaction.refund(
                identifierGenerator.generateUuid(),
                wallet.getWalletId(),
                cancellation.canceledAmount(),
                balanceAfter,
                charge.getChargeId(),
                cancellation.canceledAt()
        );

        ChargeRefund chargeRefund = ChargeRefund.refunded(
                identifierGenerator.generateUuid(),
                charge.getChargeId(),
                cancellation.canceledAmount(),
                command.refundReason(),
                refundRequestedAt,
                cancellation.canceledAt()
        );

        chargeRefundRepository.save(chargeRefund);
        walletRepository.save(wallet);
        walletTransactionRepository.save(walletTransaction);

        return new ChargeRefundResult(
                charge.getChargeId(),
                chargeRefund.getRefundStatus(),
                chargeRefund.getRefundAmount(),
                wallet.getBalance(),
                chargeRefund.getRefundedAt()
        );
    }

    private void validateCommand(ChargeRefundCommand command) {
        if (command.chargeId() == null) {
            throw new InvalidChargeRequestException("chargeId is required.");
        }
        if (command.refundReason() == null || command.refundReason().isBlank()) {
            throw new InvalidChargeRequestException("refundReason is required.");
        }
    }

    private void failRefund(Charge charge, String refundReason, LocalDateTime refundRequestedAt, String failureReason) {
        LocalDateTime failedAt = timeProvider.now();
        ChargeRefund chargeRefund = ChargeRefund.failed(
                identifierGenerator.generateUuid(),
                charge.getChargeId(),
                charge.getApprovedAmount(),
                refundReason,
                refundRequestedAt,
                failedAt,
                resolveFailureReason(failureReason)
        );
        chargeRefundRepository.save(chargeRefund);
    }

    private String resolveFailureReason(String failureReason) {
        if (failureReason == null || failureReason.isBlank()) {
            return "Payment confirmation failed.";
        }
        return failureReason;
    }
}
