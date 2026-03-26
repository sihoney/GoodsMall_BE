package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 주문 결제 유스케이스를 담당한다.
 * 구매자 wallet 차감과 escrow 생성까지를 하나의 흐름으로 처리하고, 중복 주문 결제 요청은 기존 결과를 재사용한다.
 */
public class OrderPaymentService implements OrderPaymentUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EscrowRepository escrowRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public OrderPaymentService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            EscrowRepository escrowRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * 주문 결제 요청을 검증한 뒤 구매자 wallet을 차감하고 seller 정산용 escrow를 생성한다.
     * 이미 같은 orderId의 escrow가 있으면 추가 차감 없이 기존 결과를 반환해 멱등하게 처리한다.
     */
    public OrderPaymentResult payOrder(OrderPaymentCommand command) {
        validateCommand(command);

        Escrow existingEscrow = escrowRepository.findByOrderId(command.orderId()).orElse(null);
        if (existingEscrow != null) {
            return existingResult(command, existingEscrow);
        }

        Wallet buyerWallet = walletRepository.findByMemberId(command.buyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        LocalDateTime now = timeProvider.now();
        Long balanceAfter = buyerWallet.decreaseBalance(command.orderAmount(), now);

        WalletTransaction purchaseTransaction = WalletTransaction.purchase(
                identifierGenerator.generateUuid(),
                buyerWallet.getWalletId(),
                command.orderAmount(),
                balanceAfter,
                command.orderId(),
                now
        );

        Escrow escrow = Escrow.createHeld(
                identifierGenerator.generateUuid(),
                command.orderId(),
                command.buyerMemberId(),
                command.sellerMemberId(),
                command.sellerReceivableAmount(),
                command.releaseAt(),
                now
        );

        walletRepository.save(buyerWallet);
        walletTransactionRepository.save(purchaseTransaction);
        escrowRepository.save(escrow);

        return new OrderPaymentResult(
                command.orderId(),
                buyerWallet.getWalletId(),
                escrow.getEscrowId(),
                command.orderAmount(),
                buyerWallet.getBalance(),
                escrow.getEscrowStatus(),
                escrow.getReleaseAt()
        );
    }

    private OrderPaymentResult existingResult(OrderPaymentCommand command, Escrow existingEscrow) {
        Wallet buyerWallet = walletRepository.findByMemberId(command.buyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        return new OrderPaymentResult(
                command.orderId(),
                buyerWallet.getWalletId(),
                existingEscrow.getEscrowId(),
                command.orderAmount(),
                buyerWallet.getBalance(),
                existingEscrow.getEscrowStatus(),
                existingEscrow.getReleaseAt()
        );
    }

    /**
     * 주문 결제 단계의 필수 입력만 검증한다.
     * wallet 잔액 부족과 중복 결제 같은 상태 판단은 본문 흐름에서 처리한다.
     */
    private void validateCommand(OrderPaymentCommand command) {
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("orderId is required.");
        }
        if (command.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("buyerMemberId is required.");
        }
        if (command.sellerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("sellerMemberId is required.");
        }
        if (command.orderAmount() == null || command.orderAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("orderAmount must be positive.");
        }
        if (command.sellerReceivableAmount() == null || command.sellerReceivableAmount() <= 0) {
            throw new InvalidOrderPaymentRequestException("sellerReceivableAmount must be positive.");
        }
        if (command.sellerReceivableAmount() > command.orderAmount()) {
            throw new InvalidOrderPaymentRequestException("sellerReceivableAmount cannot exceed orderAmount.");
        }
    }
}
