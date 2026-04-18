package com.example.payment.application.service;

import com.example.payment.application.dto.WithdrawCommand;
import com.example.payment.application.dto.WithdrawResult;
import com.example.payment.application.usecase.WithdrawUseCase;
import com.example.payment.common.exception.InsufficientWalletBalanceException;
import com.example.payment.common.exception.InvalidWithdrawAccountException;
import com.example.payment.common.exception.InvalidWithdrawRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.common.exception.WithdrawAmountBelowMinimumException;
import com.example.payment.common.exception.WithdrawAmountNotGreaterThanFeeException;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.entity.WithdrawRequest;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.repository.WithdrawRequestRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import com.example.payment.infrastructure.crypto.WithdrawAccountEncryptionService;
import com.example.payment.infrastructure.crypto.WithdrawAccountMaskingService;
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
    private final WithdrawAccountEncryptionService withdrawAccountEncryptionService;
    private final WithdrawAccountMaskingService withdrawAccountMaskingService;

    public WithdrawService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            WithdrawRequestRepository withdrawRequestRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider,
            WithdrawAccountEncryptionService withdrawAccountEncryptionService,
            WithdrawAccountMaskingService withdrawAccountMaskingService
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.withdrawRequestRepository = withdrawRequestRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
        this.withdrawAccountEncryptionService = withdrawAccountEncryptionService;
        this.withdrawAccountMaskingService = withdrawAccountMaskingService;
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
        String normalizedBankAccount = normalizeBankAccount(command.bankAccount());
        String normalizedAccountHolder = normalizeAccountHolder(command.accountHolder());

        WithdrawRequest withdrawRequest = createWithdrawRequest(
                command,
                wallet,
                fee,
                actualAmount,
                normalizedBankAccount,
                normalizedAccountHolder,
                now
        );
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
                withdrawRequest.getMaskedBankAccount(),
                withdrawRequest.getStatus(),
                wallet.getBalance(),
                withdrawRequest.getRequestedAt(),
                withdrawRequest.getProcessedAt()
        );
    }

    private void validateCommand(WithdrawCommand command) {
        if (command.memberId() == null) {
            throw new InvalidWithdrawRequestException("회원 정보가 없어 출금 요청을 처리할 수 없습니다.");
        }
        if (command.amount() == null || command.amount() <= 0) {
            throw new InvalidWithdrawRequestException("출금 금액은 0보다 커야 합니다.");
        }
        if (command.bankAccount() == null || command.bankAccount().isBlank()) {
            throw new InvalidWithdrawAccountException("출금 계좌번호는 필수입니다.");
        }
        if (command.accountHolder() == null || command.accountHolder().isBlank()) {
            throw new InvalidWithdrawAccountException("예금주는 필수입니다.");
        }
    }

    private void validateWithdrawalPolicy(Wallet wallet, Long amount) {
        if (amount < MINIMUM_WITHDRAW_AMOUNT) {
            throw new WithdrawAmountBelowMinimumException();
        }
        if (amount <= WITHDRAW_FEE) {
            throw new WithdrawAmountNotGreaterThanFeeException();
        }
        if (wallet.getBalance() < amount) {
            throw new InsufficientWalletBalanceException();
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
            String normalizedBankAccount,
            String normalizedAccountHolder,
            LocalDateTime requestedAt
    ) {
        return WithdrawRequest.createRequested(
                identifierGenerator.generateUuid(),
                command.memberId(),
                wallet.getWalletId(),
                command.amount(),
                fee,
                actualAmount,
                withdrawAccountEncryptionService.encrypt(normalizedBankAccount),
                withdrawAccountEncryptionService.encrypt(normalizedAccountHolder),
                withdrawAccountMaskingService.mask(normalizedBankAccount),
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
                "wallet withdraw",
                createdAt
        );
    }

    private String normalizeBankAccount(String bankAccount) {
        String normalized = bankAccount.trim().replace("-", "").replace(" ", "");
        if (!normalized.matches("\\d{6,20}")) {
            throw new InvalidWithdrawAccountException("출금 계좌번호는 숫자만 입력할 수 있으며 6자 이상 20자 이하여야 합니다.");
        }
        return normalized;
    }

    private String normalizeAccountHolder(String accountHolder) {
        String normalized = accountHolder.trim();
        if (normalized.isEmpty()) {
            throw new InvalidWithdrawAccountException("예금주는 공백일 수 없습니다.");
        }
        return normalized;
    }
}
