package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.application.event.SettlementCandidateCreatedEvent;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.common.exception.EscrowNotFoundException;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
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
 * 구매확정 이후 판매자 지갑 정산과 후속 이벤트 발행을 하나의 흐름으로 묶어 처리한다.
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
     * 이미 RELEASED인 escrow는 기존 결과를 재사용해 멱등하게 처리한다.
     * 실제 해제 시에는 escrow 상태 변경, 판매자 wallet 증가, 정산 이력 저장을 함께 수행한다.
     */
    public EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command) {
        validateCommand(command);

        Escrow escrow = escrowRepository.findByOrderId(command.orderId())
                .orElseThrow(EscrowNotFoundException::new);

        if (escrow.isReleased()) {
            return existingResult(command, escrow);
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

        return new EscrowReleaseResult(
                escrow.getOrderId(),
                escrow.getAmount(),
                escrow.getEscrowStatus(),
                escrow.getReleasedAt()
        );
    }

    /**
     * 중복 구매확정 이벤트가 들어온 경우 현재 seller wallet 상태를 포함한 기존 결과를 재구성한다.
     * 추가 정산 없이 동일한 성공 결과를 반환하기 위한 보조 메서드다.
     */
    private EscrowReleaseResult existingResult(EscrowReleaseCommand command, Escrow escrow) {
        return new EscrowReleaseResult(
                escrow.getOrderId(),
                escrow.getAmount(),
                escrow.getEscrowStatus(),
                escrow.getReleasedAt()
        );
    }

    /**
     * release 단계에서 필요한 최소 입력만 검증한다.
     * escrow 상태와 멱등 처리 정책은 조회 이후 본문에서 분기한다.
     */
    private void validateCommand(EscrowReleaseCommand command) {
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
