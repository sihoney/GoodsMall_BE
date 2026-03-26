package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeRefundCommand;
import com.example.payment.application.dto.ChargeRefundResult;
import com.example.payment.application.usecase.ChargeRefundUseCase;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.common.exception.WalletNotFoundException;
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
/**
 * 충전 환불 유스케이스를 담당한다.
 * 승인된 charge를 기준으로 환불 가능 상태를 확인하고, PG 취소 성공 후 wallet 잔액을 차감한다.
 */
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
    /**
     * 환불 가능한 charge인지 먼저 검증한 뒤 PG 취소와 wallet 차감을 순서대로 수행한다.
     * PG 취소가 실패하면 failed refund 이력만 남기고 charge와 wallet 상태는 유지한다.
     */
    public ChargeRefundResult refundCharge(ChargeRefundCommand command) {
        validateCommand(command);

        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isSuccess()) {
            throw new IllegalStateException("Charge is not refundable.");
        }
        if (chargeRefundRepository.existsRefundedByChargeId(charge.getChargeId())) {
            throw new IllegalStateException("Charge refund has already been completed.");
        }
        if (charge.getApprovedAmount() == null || charge.getPgPaymentKey() == null) {
            throw new IllegalStateException("Charge refund information is incomplete.");
        }

        LocalDateTime refundRequestedAt = timeProvider.now();
        Wallet wallet = walletRepository.findByWalletId(charge.getWalletId())
                .orElseThrow(WalletNotFoundException::new);

        if (wallet.getBalance() < charge.getApprovedAmount()) {
            throw new IllegalStateException("Charge has already been used and cannot be refunded.");
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

    /**
     * 환불 PG 호출 실패를 refund 이력으로 남긴다.
     * 실제 환불 완료 이력과 분리해서 저장해 재시도 또는 원인 추적이 가능하도록 한다.
     */
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
