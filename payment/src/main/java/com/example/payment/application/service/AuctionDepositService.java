package com.example.payment.application.service;

import com.example.payment.application.dto.AuctionDepositCommand;
import com.example.payment.application.dto.AuctionDepositResult;
import com.example.payment.application.usecase.AuctionDepositUseCase;
import com.example.payment.common.exception.AuctionDepositNotFoundException;
import com.example.payment.common.exception.InsufficientWalletBalanceException;
import com.example.payment.common.exception.InvalidAuctionBidFeeRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.AuctionDeposit;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.repository.AuctionDepositRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class AuctionDepositService implements AuctionDepositUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final AuctionDepositRepository auctionDepositRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public AuctionDepositService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            AuctionDepositRepository auctionDepositRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.auctionDepositRepository = auctionDepositRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public AuctionDepositResult processAuctionDeposit(AuctionDepositCommand command) {
        validateCommand(command);

        LocalDateTime now = timeProvider.now();
        AuctionDeposit previousHeldAuctionDeposit = lockPreviousHeldAuctionDeposit(command.auctionId());

        validatePreviousBidderContext(command, previousHeldAuctionDeposit);
        validatePreviousHeldDeposit(command, previousHeldAuctionDeposit);

        Wallet highestBidderWallet = walletRepository.findByMemberIdForUpdate(command.highestBidderId())
                .orElseThrow(WalletNotFoundException::new);
        validateSufficientBalance(highestBidderWallet, command.highestBidderFee());

        BigDecimal balanceAfterHold = highestBidderWallet.decreaseBalance(command.highestBidderFee(), now);
        walletRepository.save(highestBidderWallet);

        UUID holdWalletTransactionId = identifierGenerator.generateUuid();
        WalletTransaction holdWalletTransaction = WalletTransaction.auctionDepositHold(
                holdWalletTransactionId,
                highestBidderWallet.getWalletId(),
                command.highestBidderFee(),
                balanceAfterHold,
                command.auctionId(),
                now
        );
        walletTransactionRepository.save(holdWalletTransaction);

        AuctionDeposit auctionDeposit = AuctionDeposit.hold(
                identifierGenerator.generateUuid(),
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                holdWalletTransactionId,
                now
        );
        auctionDepositRepository.save(auctionDeposit);

        BigDecimal refundedAmount = BigDecimal.ZERO;
        if (previousHeldAuctionDeposit != null) {
            refundedAmount = refundPreviousBidDeposit(command, previousHeldAuctionDeposit, now);
        }

        log.info(
                "경매 입찰 수수료 처리 완료 auctionId={} highestBidderId={} heldAmount={} previousBidderId={} refundedAmount={}",
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                command.previousBidderId(),
                refundedAmount
        );

        return new AuctionDepositResult(
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                command.previousBidderId(),
                refundedAmount
        );
    }

    private BigDecimal refundPreviousBidDeposit(
            AuctionDepositCommand command,
            AuctionDeposit previousAuctionDeposit,
            LocalDateTime occurredAt
    ) {
        if (!Objects.equals(previousAuctionDeposit.getAuctionId(), command.auctionId())) {
            throw new IllegalStateException("이전 최고 입찰 예치금이 현재 경매와 일치하지 않습니다.");
        }
        if (!Objects.equals(previousAuctionDeposit.getBidderId(), command.previousBidderId())) {
            throw new IllegalStateException("이전 최고 입찰자 정보가 예치금 원장과 일치하지 않습니다.");
        }
        if (previousAuctionDeposit.getDepositAmount().compareTo(command.previousBidderPaidFee()) != 0) {
            throw new IllegalStateException("이전 최고 입찰 예치금 금액이 예치금 원장과 일치하지 않습니다.");
        }

        Wallet previousBidderWallet = walletRepository.findByMemberIdForUpdate(command.previousBidderId())
                .orElseThrow(WalletNotFoundException::new);
        BigDecimal balanceAfterRefund = previousBidderWallet.increaseBalance(command.previousBidderPaidFee(), occurredAt);
        walletRepository.save(previousBidderWallet);

        UUID refundWalletTransactionId = identifierGenerator.generateUuid();
        WalletTransaction refundWalletTransaction = WalletTransaction.auctionDepositRefund(
                refundWalletTransactionId,
                previousBidderWallet.getWalletId(),
                command.previousBidderPaidFee(),
                balanceAfterRefund,
                command.auctionId(),
                occurredAt
        );
        walletTransactionRepository.save(refundWalletTransaction);

        previousAuctionDeposit.refund(refundWalletTransactionId, occurredAt);
        auctionDepositRepository.save(previousAuctionDeposit);
        return command.previousBidderPaidFee();
    }

    private void validateCommand(AuctionDepositCommand command) {
        if (command == null) {
            throw new InvalidAuctionBidFeeRequestException("경매 입찰 수수료 요청이 비어 있습니다.");
        }
        if (command.auctionId() == null) {
            throw new InvalidAuctionBidFeeRequestException("경매 ID가 없어 예치금 처리를 진행할 수 없습니다.");
        }
        if (command.highestBidderId() == null) {
            throw new InvalidAuctionBidFeeRequestException("최고 입찰자 ID가 없어 예치금 처리를 진행할 수 없습니다.");
        }
        if (command.highestBidderFee() == null || command.highestBidderFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAuctionBidFeeRequestException("최고 입찰자 예치금은 0보다 커야 합니다.");
        }

        validateFirstBidContext(command);
    }

    private void validateFirstBidContext(AuctionDepositCommand command) {
        boolean hasPreviousBidContext = command.previousBidderId() != null || command.previousBidderPaidFee() != null;

        if (command.isFirst()) {
            if (hasPreviousBidContext) {
                throw new InvalidAuctionBidFeeRequestException("첫 입찰 요청에는 이전 최고 입찰자 정보가 포함될 수 없습니다.");
            }
            return;
        }

        if (!hasPreviousBidContext) {
            throw new InvalidAuctionBidFeeRequestException("첫 입찰이 아닌 요청에는 이전 최고 입찰자 정보가 필요합니다.");
        }
        if (command.previousBidderId() == null) {
            throw new InvalidAuctionBidFeeRequestException("이전 최고 입찰자 ID가 누락되었습니다.");
        }
        if (command.previousBidderPaidFee() == null || command.previousBidderPaidFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAuctionBidFeeRequestException("이전 최고 입찰자 예치금은 0보다 커야 합니다.");
        }
    }

    private void validateSufficientBalance(Wallet wallet, BigDecimal amount) {
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new InsufficientWalletBalanceException();
        }
    }

    private AuctionDeposit lockPreviousHeldAuctionDeposit(UUID auctionId) {
        return auctionDepositRepository.findHeldByAuctionIdForUpdate(auctionId).orElse(null);
    }

    private void validatePreviousBidderContext(AuctionDepositCommand command, AuctionDeposit previousHeldAuctionDeposit) {
        boolean hasPreviousBidContext = command.previousBidderId() != null || command.previousBidderPaidFee() != null;
        if (!hasPreviousBidContext) {
            if (previousHeldAuctionDeposit != null) {
                throw new InvalidAuctionBidFeeRequestException("이전 최고 입찰 예치금 정보가 존재하는데 요청값이 누락되었습니다.");
            }
            return;
        }

        if (previousHeldAuctionDeposit == null) {
            throw new AuctionDepositNotFoundException("이전 최고 입찰의 예치금 원장을 찾을 수 없습니다.");
        }
    }

    private void validatePreviousHeldDeposit(AuctionDepositCommand command, AuctionDeposit previousHeldAuctionDeposit) {
        if (previousHeldAuctionDeposit == null) {
            return;
        }

        if (Objects.equals(previousHeldAuctionDeposit.getBidderId(), command.highestBidderId())) {
            throw new InvalidAuctionBidFeeRequestException("현재 최고 입찰자와 동일한 회원의 재입찰 요청은 처리할 수 없습니다.");
        }
        if (!Objects.equals(previousHeldAuctionDeposit.getBidderId(), command.previousBidderId())) {
            throw new InvalidAuctionBidFeeRequestException("이전 최고 입찰자 정보가 현재 활성 예치금과 일치하지 않습니다.");
        }
        if (previousHeldAuctionDeposit.getDepositAmount().compareTo(command.previousBidderPaidFee()) != 0) {
            throw new InvalidAuctionBidFeeRequestException("이전 최고 입찰자 예치금 금액이 현재 활성 예치금과 일치하지 않습니다.");
        }
    }
}
