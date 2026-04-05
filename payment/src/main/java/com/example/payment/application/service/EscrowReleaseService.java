package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.SettlementCandidateCreatedEventPublisher;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * escrow 해제 유스케이스를 담당한다.
 * 구매확정 이후 seller별 escrow를 RELEASED로 전환하고 정산 후보 이벤트를 발행한다.
 */
public class EscrowReleaseService implements EscrowReleaseUseCase {

    private final EscrowRepository escrowRepository;
    private final IdentifierGenerator identifierGenerator;
    private final AutoPurchaseConfirmedEventPublisher autoPurchaseConfirmedEventPublisher;
    private final SettlementCandidateCreatedEventPublisher settlementCandidateCreatedEventPublisher;
    private final TimeProvider timeProvider;

    public EscrowReleaseService(
            EscrowRepository escrowRepository,
            IdentifierGenerator identifierGenerator,
            AutoPurchaseConfirmedEventPublisher autoPurchaseConfirmedEventPublisher,
            SettlementCandidateCreatedEventPublisher settlementCandidateCreatedEventPublisher,
            TimeProvider timeProvider
    ) {
        this.escrowRepository = escrowRepository;
        this.identifierGenerator = identifierGenerator;
        this.autoPurchaseConfirmedEventPublisher = autoPurchaseConfirmedEventPublisher;
        this.settlementCandidateCreatedEventPublisher = settlementCandidateCreatedEventPublisher;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * orderId와 sellerMemberId에 해당하는 escrow를 해제하고 후속 이벤트를 발행한다.
     * 이미 RELEASED 상태면 기존 결과를 그대로 반환해 멱등하게 처리한다.
     */
    public EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command) {
        validateCommand(command);

        // 다중 seller 주문에서는 구매확정 단위를 orderId + sellerMemberId로 본다.
        Escrow escrow = escrowRepository.findByOrderIdAndSellerMemberId(command.orderId(), command.sellerMemberId())
                .orElseThrow(EscrowNotFoundException::new);

        if (escrow.isReleased()) {
            return existingResult(escrow);
        }
        if (escrow.isRefunded()) {
            throw new IllegalStateException("Escrow is not releasable.");
        }
        if (!escrow.isHeld()) {
            throw new IllegalStateException("Escrow is not releasable.");
        }

        LocalDateTime now = timeProvider.now();
        escrow.release(now, now);

        escrowRepository.save(escrow);
        // settlement는 escrow 단위 원천 항목을 적재하므로, 해제된 escrow별로 후보를 발행한다.
        settlementCandidateCreatedEventPublisher.publish(new SettlementCandidateCreatedEvent(
                identifierGenerator.generateUuid(),
                escrow.getOrderId(),
                escrow.getEscrowId(),
                escrow.getSellerMemberId(),
                escrow.getAmount(),
                escrow.getReleasedAt(),
                command.confirmationType(),
                now
        ));
        if (command.confirmationType() == ConfirmationType.AUTO) {
            autoPurchaseConfirmedEventPublisher.publish(new AutoPurchaseConfirmedEvent(
                    escrow.getOrderId(),
                    escrow.getBuyerMemberId(),
                    escrow.getReleasedAt()
            ));
        }
        // todo: 현재는 통신 기준이 정확하지 않아 반환값이 존재
        //  api 통신이면 살리고 카프카로 확정이면 반환값 제거 고려
        return new EscrowReleaseResult(
                escrow.getOrderId(),
                escrow.getAmount(),
                escrow.getEscrowStatus(),
                escrow.getReleasedAt()
        );
    }

    /**
     * 이미 해제된 escrow의 결과를 현재 응답 형식으로 재구성한다.
     */
    private EscrowReleaseResult existingResult(Escrow escrow) {
        return new EscrowReleaseResult(
                escrow.getOrderId(),
                escrow.getAmount(),
                escrow.getEscrowStatus(),
                escrow.getReleasedAt()
        );
    }

    /**
     * escrow 해제 계약의 최소 필수 입력만 검증한다.
     */
    private void validateCommand(EscrowReleaseCommand command) {
        if (command == null) {
            throw new InvalidOrderPaymentRequestException("command is required.");
        }
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (command.confirmationType() == null) {
            throw new InvalidOrderPaymentRequestException("confirmationType is required.");
        }
    }
}
