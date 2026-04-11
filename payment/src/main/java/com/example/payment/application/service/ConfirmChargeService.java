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
/**
 * 충전 승인 유스케이스를 담당한다.
 * 요청 단계에서 생성된 PENDING charge를 기준으로 PG 승인 결과를 검증하고,
 * 승인 성공 시 charge와 wallet을 함께 반영한다.
 */
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
    /**
     * charge 요청 정보와 PG 승인 응답을 대조한 뒤 wallet 잔액을 증가시킨다.
     * PG 호출이 실패하면 charge만 FAILED로 기록하고 wallet 변경은 수행하지 않는다.
     */
    public ChargeConfirmResult confirmCharge(ChargeConfirmCommand command) {
        validateCommand(command);

        // 충전 내역을 조회하여 PENDING 상태인지, PG 승인 정보와 일치하는지 검증한다.
        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isPending()) {
            throw new IllegalStateException("Charge is not pending.");
        }
        // 데이터 무결성 검사를 위하여 PG 승인 응답과 charge 요청 정보를 대조한다.
        if (!Objects.equals(charge.getPgOrderId(), command.pgOrderId())) {
            throw new InvalidChargeRequestException("pgOrderId does not match charge.");
        }
        // 금액 검증은 pg 요청에서 온 금액과 기록된 금액을 모두 비교
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
        // 지갑을 조회
        Wallet wallet = walletRepository.findByWalletId(charge.getWalletId())
                .orElseThrow(WalletNotFoundException::new);
        // 승인 결과 db에 반영
        charge.approve(
                confirmation.approvedAmount(),
                confirmation.paymentKey(),
                resolveTossBankCode(confirmation),
                confirmation.approvedAt()
        );

        // 지갑 증가 메서드를 이용해서 값을 증가
        Long balanceAfter = wallet.increaseBalance(confirmation.approvedAmount(), confirmation.approvedAt());
        // 지갑 변경 이력을 저장
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

    /**
     * PG 승인 실패를 charge 이력에 남긴다.
     * wallet 반영 이전 단계에서만 호출되므로 실패 기록과 지갑 변경이 섞이지 않는다.
     */
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

    private String resolveTossBankCode(TossPaymentGateway.TossPaymentConfirmation confirmation) {
        if (!"계좌이체".equals(confirmation.method())) {
            return null;
        }
        if (confirmation.transferBankCode() == null || confirmation.transferBankCode().isBlank()) {
            throw new PaymentGatewayException("Toss transfer response is missing bankCode.");
        }
        return confirmation.transferBankCode();
    }
}
