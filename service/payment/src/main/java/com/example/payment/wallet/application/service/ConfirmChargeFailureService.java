package com.example.payment.wallet.application.service;

import com.example.payment.wallet.application.dto.ChargeConfirmFailureCommand;
import com.example.payment.wallet.application.dto.ChargeConfirmFailureResult;
import com.example.payment.wallet.application.usecase.ChargeConfirmFailureUseCase;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.wallet.domain.entity.Charge;
import com.example.payment.wallet.domain.enumtype.ChargeStatus;
import com.example.payment.wallet.domain.repository.ChargeRepository;
import com.example.payment.common.domain.service.TimeProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConfirmChargeFailureService implements ChargeConfirmFailureUseCase {

    private final ChargeRepository chargeRepository;
    private final TimeProvider timeProvider;

    public ConfirmChargeFailureService(
            ChargeRepository chargeRepository,
            TimeProvider timeProvider
    ) {
        this.chargeRepository = chargeRepository;
        this.timeProvider = timeProvider;
    }

    @Override
    public ChargeConfirmFailureResult confirmChargeFailure(ChargeConfirmFailureCommand command) {
        validateCommand(command);

        // pending ?곹깭??chargeId瑜?李얜뒗??orderId瑜??꾨줎?몄뿉??諛쏄린 ?뚮Ц)
        Charge charge = chargeRepository.findByPgOrderId(command.orderId())
                .orElseThrow(ChargeNotFoundException::new);

        // ?곹깭媛 pending?대㈃ ?ㅽ뙣濡??꾪솚, pending???꾨땲硫댁꽌 ?ㅽ뙣 ?곹깭媛 ?꾨땲硫??덉쇅
        // ?대? ?ㅽ뙣 ?곹깭??寃쎌슦??硫깅벑??泥섎━瑜??꾪빐 湲곗〈 寃곌낵瑜?洹몃?濡?諛섑솚
        if (charge.isPending()) {
            charge.failAtRedirect(buildFailureReason(command.failureCode(), command.failureMessage()), timeProvider.now());
            chargeRepository.save(charge);
        } else if (!isFailed(charge)) {
            throw new IllegalStateException("Charge is not pending.");
        }

        return new ChargeConfirmFailureResult(
                charge.getChargeId(),
                charge.getChargeStatus(),
                charge.getPgOrderId(),
                charge.getFailureReason(),
                charge.getFailedAt()
        );
    }

    private void validateCommand(ChargeConfirmFailureCommand command) {
        if (command.orderId() == null || command.orderId().isBlank()) {
            throw new InvalidChargeRequestException("二쇰Ц ID???꾩닔?낅땲??");
        }
        if (command.failureMessage() == null || command.failureMessage().isBlank()) {
            throw new InvalidChargeRequestException("?ㅽ뙣 硫붿떆吏???꾩닔?낅땲??");
        }
    }

    // ?묐떟 肄붾뱶???⑹퀜??failreason??留뚮뱺??
    private String buildFailureReason(String code, String message) {
        if (code == null || code.isBlank()) {
            return message;
        }
        return "[%s] %s".formatted(code, message);
    }

    // ?ㅽ뙣 ?곹깭?몄? ?먭?
    private boolean isFailed(Charge charge) {
        return charge.getChargeStatus() == ChargeStatus.REDIRECT_FAILED;
    }
}
