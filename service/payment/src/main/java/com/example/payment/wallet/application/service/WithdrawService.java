package com.example.payment.wallet.application.service;

import com.example.payment.wallet.application.dto.WithdrawCommand;
import com.example.payment.wallet.application.dto.WithdrawResult;
import com.example.payment.wallet.application.usecase.WithdrawUseCase;
import com.example.payment.common.exception.InsufficientWalletBalanceException;
import com.example.payment.common.exception.InvalidWithdrawAccountException;
import com.example.payment.common.exception.InvalidWithdrawRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.common.exception.WithdrawAmountBelowMinimumException;
import com.example.payment.common.exception.WithdrawAmountNotGreaterThanFeeException;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.wallet.domain.entity.WithdrawRequest;
import com.example.payment.wallet.domain.enumtype.WalletTransactionType;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.wallet.domain.repository.WithdrawRequestRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import com.example.payment.wallet.infrastructure.crypto.WithdrawAccountEncryptionService;
import com.example.payment.wallet.infrastructure.crypto.WithdrawAccountMaskingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WithdrawService implements WithdrawUseCase {

    private static final BigDecimal MINIMUM_WITHDRAW_AMOUNT = BigDecimal.valueOf(5_000L);
    private static final BigDecimal WITHDRAW_FEE = BigDecimal.valueOf(1_000L);
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
        BigDecimal fee = calculateFee();
        BigDecimal actualAmount = command.amount().subtract(fee);
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

        BigDecimal balanceAfter = wallet.decreaseBalance(command.amount(), now);
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
            throw new InvalidWithdrawRequestException("?뚯썝 ?뺣낫媛 ?놁뼱 異쒓툑 ?붿껌??泥섎━?????놁뒿?덈떎.");
        }
        if (command.amount() == null || command.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidWithdrawRequestException("異쒓툑 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
        if (command.bankAccount() == null || command.bankAccount().isBlank()) {
            throw new InvalidWithdrawAccountException("異쒓툑 怨꾩쥖踰덊샇???꾩닔?낅땲??");
        }
        if (command.accountHolder() == null || command.accountHolder().isBlank()) {
            throw new InvalidWithdrawAccountException("?덇툑二쇰뒗 ?꾩닔?낅땲??");
        }
    }

    private void validateWithdrawalPolicy(Wallet wallet, BigDecimal amount) {
        if (amount.compareTo(MINIMUM_WITHDRAW_AMOUNT) < 0) {
            throw new WithdrawAmountBelowMinimumException();
        }
        if (amount.compareTo(WITHDRAW_FEE) <= 0) {
            throw new WithdrawAmountNotGreaterThanFeeException();
        }
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientWalletBalanceException();
        }
    }

    private BigDecimal calculateFee() {
        return WITHDRAW_FEE;
    }

    private WithdrawRequest createWithdrawRequest(
            WithdrawCommand command,
            Wallet wallet,
            BigDecimal fee,
            BigDecimal actualAmount,
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
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID withdrawRequestId,
            LocalDateTime createdAt
    ) {
        return WalletTransaction.create(
                identifierGenerator.generateUuid(),
                walletId,
                amount.negate(),
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
            throw new InvalidWithdrawAccountException("異쒓툑 怨꾩쥖踰덊샇???レ옄留??낅젰?????덉쑝硫?6???댁긽 20???댄븯?ъ빞 ?⑸땲??");
        }
        return normalized;
    }

    private String normalizeAccountHolder(String accountHolder) {
        String normalized = accountHolder.trim();
        if (normalized.isEmpty()) {
            throw new InvalidWithdrawAccountException("?덇툑二쇰뒗 怨듬갚?????놁뒿?덈떎.");
        }
        return normalized;
    }
}
