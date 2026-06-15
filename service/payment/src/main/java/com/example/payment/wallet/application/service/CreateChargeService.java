package com.example.payment.wallet.application.service;

import com.example.payment.wallet.application.dto.ChargeCreateCommand;
import com.example.payment.wallet.application.dto.ChargeCreateResult;
import com.example.payment.wallet.application.usecase.ChargeCreateUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.wallet.domain.entity.Charge;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.wallet.domain.repository.ChargeRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 異⑹쟾 ?붿껌 ?앹꽦 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * ?ㅼ젣 異⑹쟾 ?뱀씤怨?吏媛??붿븸 諛섏쁺? 蹂꾨룄 confirm ?④퀎?먯꽌 泥섎━?섍퀬,
 * ???쒕퉬?ㅻ뒗 PG 二쇰Ц ?앸퀎?먯? PENDING charge ?앹꽦源뚯?留?梨낆엫吏꾨떎.
 */
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
    /**
     * 異⑹쟾 ?붿껌??寃利앺븳 ??wallet ?뚯쑀 ?щ?瑜??뺤씤?섍퀬 PENDING charge瑜??앹꽦?쒕떎.
     * ???④퀎?먯꽌???몃? PG瑜??몄텧?섏? ?딆쑝誘濡?湲덉븸? ?꾩쭅 吏媛묒뿉 諛섏쁺?섏? ?딅뒗??
     */
    public ChargeCreateResult createCharge(ChargeCreateCommand command) {
        validateCommand(command);

        // 吏媛??щ?瑜??뺤씤
        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseThrow(WalletNotFoundException::new);

        // uuid瑜??댁슜?섏뿬 orderId瑜??앹꽦?섍린 ?꾪븳 怨쇱젙
        UUID chargeId = identifierGenerator.generateUuid();
        String pgOrderId = generatePgOrderId(chargeId);
        LocalDateTime requestedAt = timeProvider.now();

        Charge charge = Charge.create(
                chargeId,
                command.memberId(),
                wallet.getWalletId(),
                command.amount(),
                pgOrderId,
                requestedAt
        );

        // JPA瑜??댁슜?섏뿬 ?곗씠???깅줉
        chargeRepository.save(charge);

        return new ChargeCreateResult(
                charge.getChargeId(),
                wallet.getWalletId(),
                charge.getPgOrderId(),
                charge.getRequestedAmount(),
                charge.getChargeStatus()
        );
    }

    private String generatePgOrderId(UUID chargeId) {
        return "CHARGE-" + chargeId;
    }

    /**
     * create ?④퀎?먯꽌 ?꾩슂??理쒖냼 ?낅젰留?寃利앺븳??
     * charge ?곹깭??PG ?묐떟 寃利앹? confirm ?④퀎?먯꽌 ?댁뼱??泥섎━?쒕떎.
     */
    private void validateCommand(ChargeCreateCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("?뚯썝 ID???꾩닔?낅땲??");
        }
        if (command.amount() == null || command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidChargeRequestException("湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
    }
}
