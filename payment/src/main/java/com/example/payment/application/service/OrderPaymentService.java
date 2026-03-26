package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.OrderPaymentAlreadyCompletedException;
import com.example.payment.domain.exception.WalletNotFoundException;
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
    public OrderPaymentResult payOrder(OrderPaymentCommand command) {
        validateCommand(command);

        if (escrowRepository.findByOrderId(command.orderId()).isPresent()) {
            throw new OrderPaymentAlreadyCompletedException();
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
