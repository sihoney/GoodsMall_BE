package com.example.payment.application.service;

import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.application.usecase.ChargeCreateUseCase;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 충전 요청 생성 유스케이스를 담당한다.
 * 실제 충전 승인과 지갑 잔액 반영은 별도 confirm 단계에서 처리하고,
 * 이 서비스는 PG 주문 식별자와 PENDING charge 생성까지만 책임진다.
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
     * 충전 요청을 검증한 뒤 wallet 소유 여부를 확인하고 PENDING charge를 생성한다.
     * 이 단계에서는 외부 PG를 호출하지 않으므로 금액은 아직 지갑에 반영되지 않는다.
     */
    public ChargeCreateResult createCharge(ChargeCreateCommand command) {
        validateCommand(command);

        // 지갑 여부를 확인
        Wallet wallet = walletRepository.findByMemberId(command.memberId())
                .orElseThrow(WalletNotFoundException::new);

        // uuid를 이용하여 orderId를 생성하기 위한 과정
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

        // JPA를 이용하여 데이터 등록
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
     * create 단계에서 필요한 최소 입력만 검증한다.
     * charge 상태나 PG 응답 검증은 confirm 단계에서 이어서 처리한다.
     */
    private void validateCommand(ChargeCreateCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("memberId is required.");
        }
        if (command.amount() == null || command.amount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidChargeRequestException("amount must be positive.");
        }
    }
}
