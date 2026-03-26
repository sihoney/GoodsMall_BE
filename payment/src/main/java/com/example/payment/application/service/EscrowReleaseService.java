package com.example.payment.application.service;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;
import com.example.payment.application.event.AutoPurchaseConfirmedEvent;
import com.example.payment.application.event.SellerIncomeReleasedEvent;
import com.example.payment.application.usecase.EscrowReleaseUseCase;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.ConfirmationType;
import com.example.payment.domain.exception.EscrowAlreadyRefundedException;
import com.example.payment.domain.exception.EscrowAlreadyReleasedException;
import com.example.payment.domain.exception.EscrowNotFoundException;
import com.example.payment.domain.exception.EscrowStateException;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.AutoPurchaseConfirmedEventPublisher;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.SellerIncomeReleasedEventPublisher;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EscrowReleaseService implements EscrowReleaseUseCase {

    private final EscrowRepository escrowRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final IdentifierGenerator identifierGenerator;
    private final AutoPurchaseConfirmedEventPublisher autoPurchaseConfirmedEventPublisher;
    private final SellerIncomeReleasedEventPublisher sellerIncomeReleasedEventPublisher;
    private final TimeProvider timeProvider;

    public EscrowReleaseService(
            EscrowRepository escrowRepository,
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            IdentifierGenerator identifierGenerator,
            AutoPurchaseConfirmedEventPublisher autoPurchaseConfirmedEventPublisher,
            SellerIncomeReleasedEventPublisher sellerIncomeReleasedEventPublisher,
            TimeProvider timeProvider
    ) {
        this.escrowRepository = escrowRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.identifierGenerator = identifierGenerator;
        this.autoPurchaseConfirmedEventPublisher = autoPurchaseConfirmedEventPublisher;
        this.sellerIncomeReleasedEventPublisher = sellerIncomeReleasedEventPublisher;
        this.timeProvider = timeProvider;
    }

    @Override
    public EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command) {
        validateCommand(command);

        Escrow escrow = escrowRepository.findByOrderId(command.orderId())
                .orElseThrow(EscrowNotFoundException::new);

        if (escrow.isReleased()) {
            throw new EscrowAlreadyReleasedException();
        }
        if (escrow.isRefunded()) {
            throw new EscrowAlreadyRefundedException();
        }
        if (!escrow.isHeld()) {
            throw new EscrowStateException("Escrow is not releasable.");
        }

        Wallet sellerWallet = walletRepository.findByMemberId(command.sellerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        LocalDateTime now = timeProvider.now();
        escrow.release(now, now);

        Long balanceAfter = sellerWallet.increaseBalance(escrow.getAmount(), now);

        WalletTransaction saleIncomeTransaction = WalletTransaction.saleIncome(
                identifierGenerator.generateUuid(),
                sellerWallet.getWalletId(),
                escrow.getAmount(),
                balanceAfter,
                escrow.getOrderId(),
                now
        );

        escrowRepository.save(escrow);
        walletRepository.save(sellerWallet);
        walletTransactionRepository.save(saleIncomeTransaction);
        sellerIncomeReleasedEventPublisher.publish(new SellerIncomeReleasedEvent(
                escrow.getOrderId(),
                sellerWallet.getMemberId(),
                sellerWallet.getWalletId(),
                escrow.getAmount(),
                escrow.getReleasedAt(),
                command.confirmationType()
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
                sellerWallet.getWalletId(),
                escrow.getAmount(),
                sellerWallet.getBalance(),
                escrow.getEscrowStatus(),
                escrow.getReleasedAt()
        );
    }

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
