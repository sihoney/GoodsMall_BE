package com.example.payment.application.service;

import com.example.payment.application.dto.WithdrawCommand;
import com.example.payment.application.dto.WithdrawResult;
import com.example.payment.application.usecase.WithdrawUseCase;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.entity.WithdrawRequest;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.repository.WithdrawRequestRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithdrawService implements WithdrawUseCase {

    private static final long MINIMUM_WITHDRAW_AMOUNT = 5_000L;
    private static final long WITHDRAW_FEE = 1_000L;
    private static final String WITHDRAW_REFERENCE_TYPE = "WITHDRAW_REQUEST";

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public WithdrawService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            WithdrawRequestRepository withdrawRequestRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public WithdrawResult withdraw(WithdrawCommand command) {
        validateCommand(command);

        Wallet wallet = walletRepository.findByMemberIdForUpdate(command.memberId())
                .orElseThrow(WalletNotFoundException::new);
        validateWithdrawalPolicy(wallet, command.amount());

        LocalDateTime now = timeProvider.now();
        Long fee = calculateFee();
        Long actualAmount = command.amount() - fee;

        WithdrawRequest withdrawRequest = createWithdrawRequest(command, wallet, fee, actualAmount, now);
        withdrawRequestRepository.save(withdrawRequest);

        Long balanceAfter = wallet.decreaseBalance(command.amount(), now);
        walletRepository.save(wallet);

        WalletTransaction walletTransaction = createWithdrawalTransaction(
                wallet.getWalletId(),
                command.amount(),
                balanceAfter,
                withdrawRequest.getWithdrawRequestId(),
                now
        );
        walletTransactionRepository.save(walletTransaction);

        withdrawRequest.linkWalletTransaction(walletTransaction.getTransactionId(), now);
        withdrawRequest.complete(now);
        withdrawRequestRepository.save(withdrawRequest);

        return new WithdrawResult(
                withdrawRequest.getWithdrawRequestId(),
                withdrawRequest.getAmount(),
                withdrawRequest.getFee(),
                withdrawRequest.getActualAmount(),
                withdrawRequest.getStatus(),
                wallet.getBalance(),
                withdrawRequest.getRequestedAt(),
                withdrawRequest.getProcessedAt()
        );
    }

    private void validateCommand(WithdrawCommand command) {
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId is required.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new IllegalArgumentException("amount must be positive.");
        }
        if (command.bankAccount() == null || command.bankAccount().isBlank()) {
            throw new IllegalArgumentException("bankAccount is required.");
        }
        if (command.accountHolder() == null || command.accountHolder().isBlank()) {
            throw new IllegalArgumentException("accountHolder is required.");
        }
    }

    private void validateWithdrawalPolicy(Wallet wallet, Long amount) {
        if (amount < MINIMUM_WITHDRAW_AMOUNT) {
            throw new IllegalArgumentException("amount must be at least " + MINIMUM_WITHDRAW_AMOUNT + ".");
        }
        if (amount <= WITHDRAW_FEE) {
            throw new IllegalArgumentException("withdraw amount must be greater than fee.");
        }
        if (wallet.getBalance() < amount) {
            throw new IllegalArgumentException("balance is insufficient.");
        }
    }

    private Long calculateFee() {
        return WITHDRAW_FEE;
    }

    private WithdrawRequest createWithdrawRequest(
            WithdrawCommand command,
            Wallet wallet,
            Long fee,
            Long actualAmount,
            LocalDateTime requestedAt
    ) {
        return WithdrawRequest.createRequested(
                identifierGenerator.generateUuid(),
                command.memberId(),
                wallet.getWalletId(),
                command.amount(),
                fee,
                actualAmount,
                command.bankCode(),
                command.bankAccount(),
                command.accountHolder(),
                requestedAt
        );
    }

    private WalletTransaction createWithdrawalTransaction(
            UUID walletId,
            Long amount,
            Long balanceAfter,
            UUID withdrawRequestId,
            LocalDateTime createdAt
    ) {
        return WalletTransaction.create(
                identifierGenerator.generateUuid(),
                walletId,
                -amount,
                balanceAfter,
                WalletTransactionType.WITHDRAWAL,
                withdrawRequestId,
                WITHDRAW_REFERENCE_TYPE,
                "seller withdraw",
                createdAt
        );
    }
}
