package com.example.payment.escrow.application.service;

import com.example.payment.escrow.application.dto.EscrowReleaseCommand;
import com.example.payment.escrow.application.dto.EscrowReleaseResult;
import com.example.payment.wallet.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.escrow.application.usecase.EscrowReleaseUseCase;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.entity.EscrowTransaction;
import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import com.example.payment.escrow.domain.repository.EscrowRepository;
import com.example.payment.escrow.domain.repository.EscrowTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.outbox.infrastructure.messaging.kafka.SettlementCandidateCreatedOutboxEventSaver;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * escrow ?댁젣 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * 援щℓ?뺤젙 ?댄썑 seller蹂?escrow?ㅼ쓣 RELEASED濡??꾪솚?섍퀬 ?뺤궛 ?꾨낫 ?대깽?몃? 諛쒗뻾?쒕떎.
 */
public class EscrowReleaseService implements EscrowReleaseUseCase {

    private final EscrowRepository escrowRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final IdentifierGenerator identifierGenerator;
    private final SettlementCandidateCreatedOutboxEventSaver settlementCandidateCreatedOutboxEventSaver;
    private final TimeProvider timeProvider;

    public EscrowReleaseService(
            EscrowRepository escrowRepository,
            EscrowTransactionRepository escrowTransactionRepository,
            IdentifierGenerator identifierGenerator,
            SettlementCandidateCreatedOutboxEventSaver settlementCandidateCreatedOutboxEventSaver,
            TimeProvider timeProvider
    ) {
        this.escrowRepository = escrowRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.identifierGenerator = identifierGenerator;
        this.settlementCandidateCreatedOutboxEventSaver = settlementCandidateCreatedOutboxEventSaver;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * orderId? sellerMemberId???대떦?섎뒗 escrow?ㅼ쓣 ?댁젣?섍퀬 ?꾩냽 ?대깽?몃? 諛쒗뻾?쒕떎.
     * ?대? RELEASED ?곹깭硫?湲곗〈 寃곌낵瑜?洹몃?濡?諛섑솚??硫깅벑?섍쾶 泥섎━?쒕떎.
     */
    public EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command) {
        validateCommand(command);

        // orderItem ?⑥쐞 escrow濡??꾪솚?섏뼱??援щℓ?뺤젙 ?⑥쐞??orderId + sellerMemberId瑜??좎??쒕떎.
        List<Escrow> sellerEscrows = escrowRepository.findAllByOrderIdAndSellerMemberId(
                command.orderId(),
                command.sellerMemberId()
        );
        if (sellerEscrows.isEmpty()) {
            throw new EscrowNotFoundException();
        }

        LocalDateTime now = timeProvider.now();
        java.math.BigDecimal releasedAmount = java.math.BigDecimal.ZERO;
        boolean releasedAny = false;

        for (Escrow escrow : sellerEscrows) {
            if (escrow.isReleased()) {
                releasedAmount = releasedAmount.add(escrow.getAmount());
                continue;
            }
            if (escrow.isRefunded()) {
                continue;
            }
            if (!escrow.isHeld()) {
                throw new IllegalStateException("Escrow is not releasable.");
            }

            java.math.BigDecimal beforeAmount = escrow.getAmount();
            escrow.release(now, now);
            escrowRepository.save(escrow);
            recordReleaseEscrowTransaction(escrow, beforeAmount, escrow.getAmount(), now);
            releasedAny = true;
            releasedAmount = releasedAmount.add(escrow.getAmount());

            // settlement??escrow ?⑥쐞 ?먯쿇 ??ぉ???곸옱?섎?濡? ?댁젣??escrow蹂꾨줈 ?꾨낫瑜?諛쒗뻾?쒕떎.
            settlementCandidateCreatedOutboxEventSaver.save(new SettlementCandidateCreatedEvent(
                    identifierGenerator.generateUuid(),
                    escrow.getOrderId(),
                    escrow.getEscrowId(),
                    escrow.getSellerMemberId(),
                    escrow.getAmount(),
                    escrow.getReleasedAt(),
                    command.confirmationType(),
                    now
            ));
        }

        if (!releasedAny) {
            return existingResult(command.orderId(), releasedAmount);
        }

        return new EscrowReleaseResult(
                command.orderId(),
                releasedAmount,
                releasedAmount.compareTo(java.math.BigDecimal.ZERO) > 0 ? EscrowStatus.RELEASED : EscrowStatus.REFUNDED,
                now
        );
    }

    /**
     * ?대? ?댁젣??escrow??寃곌낵瑜??꾩옱 ?묐떟 ?뺤떇?쇰줈 ?ш뎄?깊븳??
     */
    private EscrowReleaseResult existingResult(UUID orderId, java.math.BigDecimal releasedAmount) {
        return new EscrowReleaseResult(
                orderId,
                releasedAmount,
                releasedAmount.compareTo(java.math.BigDecimal.ZERO) > 0 ? EscrowStatus.RELEASED : EscrowStatus.REFUNDED,
                timeProvider.now()
        );
    }

    /**
     * escrow ?댁젣 怨꾩빟??理쒖냼 ?꾩닔 ?낅젰留?寃利앺븳??
     */
    private void validateCommand(EscrowReleaseCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("?먯뒪?щ줈 ?뺤궛 ?붿껌? ?꾩닔?낅땲??");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ID???꾩닔?낅땲??");
        }
        if (command.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("?먮ℓ???뚯썝 ID???꾩닔?낅땲??");
        }
        if (command.confirmationType() == null) {
            throw new InvalidOrderPaymentRequestException("援щℓ ?뺤젙 ?좏삎? ?꾩닔?낅땲??");
        }
    }

    private void recordReleaseEscrowTransaction(
            Escrow escrow,
            java.math.BigDecimal beforeAmount,
            java.math.BigDecimal afterAmount,
            LocalDateTime occurredAt
    ) {
        EscrowTransaction transaction = EscrowTransaction.release(
                identifierGenerator.generateUuid(),
                escrow.getEscrowId(),
                escrow.getOrderId(),
                escrow.isOrderItemReference() ? escrow.getReferenceId() : null,
                escrow.getSellerMemberId(),
                escrow.getBuyerMemberId(),
                beforeAmount,
                beforeAmount,
                afterAmount,
                null,
                "ESCROW_RELEASE",
                "escrow release",
                occurredAt,
                occurredAt
        );
        escrowTransactionRepository.save(transaction);
    }
}
