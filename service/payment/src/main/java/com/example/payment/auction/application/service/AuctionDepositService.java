package com.example.payment.auction.application.service;

import com.example.payment.auction.application.dto.AuctionDepositCommand;
import com.example.payment.auction.application.dto.AuctionDepositResult;
import com.example.payment.auction.application.usecase.AuctionDepositUseCase;
import com.example.payment.common.common.exception.InsufficientWalletBalanceException;
import com.example.payment.common.common.exception.InvalidAuctionBidFeeRequestException;
import com.example.payment.common.common.exception.WalletNotFoundException;
import com.example.payment.auction.domain.entity.AuctionDeposit;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.auction.domain.repository.AuctionDepositRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                command.bidId(),
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                holdWalletTransactionId,
                now
        );
        auctionDepositRepository.save(auctionDeposit);

        BigDecimal refundedAmount = BigDecimal.ZERO;
        UUID previousBidderId = null;
        if (previousHeldAuctionDeposit != null) {
            previousBidderId = previousHeldAuctionDeposit.getBidderId();
            refundedAmount = refundPreviousBidDeposit(previousHeldAuctionDeposit, now);
        }

        log.info(
                "寃쎈ℓ ?낆같 ?섏닔猷?泥섎━ ?꾨즺 auctionId={} highestBidderId={} heldAmount={} previousBidderId={} refundedAmount={}",
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                previousBidderId,
                refundedAmount
        );

        return new AuctionDepositResult(
                command.bidId(),
                command.auctionId(),
                command.highestBidderId(),
                command.highestBidderFee(),
                previousBidderId,
                refundedAmount
        );
    }

    private BigDecimal refundPreviousBidDeposit(
            AuctionDeposit previousAuctionDeposit,
            LocalDateTime occurredAt
    ) {
        Wallet previousBidderWallet = walletRepository.findByMemberIdForUpdate(previousAuctionDeposit.getBidderId())
                .orElseThrow(WalletNotFoundException::new);
        BigDecimal refundAmount = previousAuctionDeposit.getDepositAmount();
        BigDecimal balanceAfterRefund = previousBidderWallet.increaseBalance(refundAmount, occurredAt);
        walletRepository.save(previousBidderWallet);

        UUID refundWalletTransactionId = identifierGenerator.generateUuid();
        WalletTransaction refundWalletTransaction = WalletTransaction.auctionDepositRefund(
                refundWalletTransactionId,
                previousBidderWallet.getWalletId(),
                refundAmount,
                balanceAfterRefund,
                previousAuctionDeposit.getAuctionId(),
                occurredAt
        );
        walletTransactionRepository.save(refundWalletTransaction);

        previousAuctionDeposit.refund(refundWalletTransactionId, occurredAt);
        auctionDepositRepository.save(previousAuctionDeposit);
        return refundAmount;
    }

    private void validateCommand(AuctionDepositCommand command) {
        if (command == null) {
            throw new InvalidAuctionBidFeeRequestException("寃쎈ℓ ?낆같 ?섏닔猷??붿껌??鍮꾩뼱 ?덉뒿?덈떎.");
        }
        if (command.auctionId() == null) {
            throw new InvalidAuctionBidFeeRequestException("寃쎈ℓ ID媛 ?놁뼱 ?덉튂湲?泥섎━瑜?吏꾪뻾?????놁뒿?덈떎.");
        }
        if (command.highestBidderId() == null) {
            throw new InvalidAuctionBidFeeRequestException("理쒓퀬 ?낆같??ID媛 ?놁뼱 ?덉튂湲?泥섎━瑜?吏꾪뻾?????놁뒿?덈떎.");
        }
        if (command.highestBidderFee() == null || command.highestBidderFee().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAuctionBidFeeRequestException("理쒓퀬 ?낆같???덉튂湲덉? 0蹂대떎 而ㅼ빞 ?⑸땲??");
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
}
