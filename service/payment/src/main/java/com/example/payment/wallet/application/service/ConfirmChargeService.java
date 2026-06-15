package com.example.payment.wallet.application.service;

import com.example.payment.wallet.application.dto.ChargeConfirmCommand;
import com.example.payment.wallet.application.dto.ChargeConfirmResult;
import com.example.payment.wallet.application.usecase.ChargeConfirmUseCase;
import com.example.payment.wallet.domain.entity.Charge;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.common.exception.ChargeConfirmationMismatchException;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.wallet.domain.repository.ChargeRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.payment.domain.service.TossPaymentGateway;
import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 異⑹쟾 ?뱀씤 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * ?붿껌 ?④퀎?먯꽌 ?앹꽦??PENDING charge瑜?湲곗??쇰줈 PG ?뱀씤 寃곌낵瑜?寃利앺븯怨?
 * ?뱀씤 ?깃났 ??charge? wallet???④퍡 諛섏쁺?쒕떎.
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
     * charge ?붿껌 ?뺣낫? PG ?뱀씤 ?묐떟???議고븳 ??wallet ?붿븸??利앷??쒗궓??
     * PG ?몄텧???ㅽ뙣?섎㈃ charge留?FAILED濡?湲곕줉?섍퀬 wallet 蹂寃쎌? ?섑뻾?섏? ?딅뒗??
     */
    public ChargeConfirmResult confirmCharge(ChargeConfirmCommand command) {
        validateCommand(command);

        // 異⑹쟾 ?댁뿭??議고쉶?섏뿬 PENDING ?곹깭?몄?, PG ?뱀씤 ?뺣낫? ?쇱튂?섎뒗吏 寃利앺븳??
        Charge charge = chargeRepository.findByChargeId(command.chargeId())
                .orElseThrow(ChargeNotFoundException::new);

        if (!charge.isPending()) {
            throw new IllegalStateException("Charge is not pending.");
        }
        // ?곗씠??臾닿껐??寃?щ? ?꾪븯??PG ?뱀씤 ?묐떟怨?charge ?붿껌 ?뺣낫瑜??議고븳??
        if (!Objects.equals(charge.getPgOrderId(), command.pgOrderId())) {
            throw new InvalidChargeRequestException("PG 二쇰Ц ID媛 異⑹쟾 ?붿껌 ?뺣낫? ?쇱튂?섏? ?딆뒿?덈떎.");
        }
        // 湲덉븸 寃利앹? pg ?붿껌?먯꽌 ??湲덉븸怨?湲곕줉??湲덉븸??紐⑤몢 鍮꾧탳
        if (charge.getRequestedAmount() == null
                || command.amount() == null
                || charge.getRequestedAmount().compareTo(command.amount()) != 0) {
            throw new InvalidChargeRequestException("?뱀씤 湲덉븸??異⑹쟾 ?붿껌 湲덉븸怨??쇱튂?섏? ?딆뒿?덈떎.");
        }

        TossPaymentGateway.TossPaymentConfirmation confirmation;
        try {
            confirmation = tossPaymentGateway.confirm(
                    command.paymentKey(),
                    command.pgOrderId(),
                    command.amount()
            );
            validateConfirmation(command, confirmation);
        } catch (PaymentGatewayException e) {
            failCharge(charge, e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            failCharge(charge, e.getMessage());
            throw e;
        }
        // 吏媛묒쓣 議고쉶
        Wallet wallet = walletRepository.findByWalletId(charge.getWalletId())
                .orElseThrow(WalletNotFoundException::new);
        // ?뱀씤 寃곌낵 db??諛섏쁺
        charge.approve(
                confirmation.approvedAmount(),
                confirmation.paymentKey(),
                confirmation.approvedAt(),
                resolveTossBankCode(confirmation)
        );

        // 吏媛?利앷? 硫붿꽌?쒕? ?댁슜?댁꽌 媛믪쓣 利앷?
        BigDecimal balanceAfter = wallet.increaseBalance(confirmation.approvedAmount(), confirmation.approvedAt());
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
            throw new InvalidChargeRequestException("異⑹쟾 ID???꾩닔?낅땲??");
        }
        if (command.paymentKey() == null || command.paymentKey().isBlank()) {
            throw new InvalidChargeRequestException("paymentKey???꾩닔?낅땲??");
        }
        if (command.pgOrderId() == null || command.pgOrderId().isBlank()) {
            throw new InvalidChargeRequestException("PG 二쇰Ц ID???꾩닔?낅땲??");
        }
        if (command.amount() == null || command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidChargeRequestException("湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
    }

    private void validateConfirmation(
            ChargeConfirmCommand command,
            TossPaymentGateway.TossPaymentConfirmation confirmation
    ) {
        if (!Objects.equals(confirmation.paymentKey(), command.paymentKey())) {
            throw new ChargeConfirmationMismatchException("Toss ?뱀씤 ?묐떟??paymentKey媛 異⑹쟾 ?붿껌怨??쇱튂?섏? ?딆뒿?덈떎.");
        }
        if (!Objects.equals(confirmation.orderId(), command.pgOrderId())) {
            throw new ChargeConfirmationMismatchException("Toss ?뱀씤 ?묐떟??orderId媛 異⑹쟾 ?붿껌怨??쇱튂?섏? ?딆뒿?덈떎.");
        }
        if (confirmation.approvedAmount() == null
                || confirmation.approvedAmount().compareTo(command.amount()) != 0) {
            throw new ChargeConfirmationMismatchException("Toss ?뱀씤 湲덉븸??異⑹쟾 ?붿껌 湲덉븸怨??쇱튂?섏? ?딆뒿?덈떎.");
        }
    }

    /**
     * PG ?뱀씤 ?ㅽ뙣瑜?charge ?대젰???④릿??
     * wallet 諛섏쁺 ?댁쟾 ?④퀎?먯꽌留??몄텧?섎?濡??ㅽ뙣 湲곕줉怨?吏媛?蹂寃쎌씠 ?욎씠吏 ?딅뒗??
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
        if (!"怨꾩쥖?댁껜".equals(confirmation.method())) {
            return null;
        }
        if (confirmation.transferBankCode() == null || confirmation.transferBankCode().isBlank()) {
            throw new PaymentGatewayException("?좎뒪 怨꾩쥖?댁껜 ?묐떟??bankCode媛 ?놁뒿?덈떎.");
        }
        return confirmation.transferBankCode();
    }
}
