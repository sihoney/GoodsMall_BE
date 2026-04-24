package com.example.ai.application.service;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * 경매 가격 추천을 위한 규칙 기반 가격 계산기.
 * 필수 입력(currentBidPrice, startPrice)과 선택 입력(bidCount, remainingSeconds)을 조합하여
 * 추천 입찰가와 예상 형성 가격을 산출한다.
 */
@Component
public class AuctionPriceCalculator {

    private static final BigDecimal BASE_RATE = new BigDecimal("0.05");
    private static final BigDecimal BID_BONUS_PER_TEN_BIDS = new BigDecimal("0.01");
    private static final int MAX_BID_BONUS_PERCENT = 5;
    private static final long LONG_REMAINING_THRESHOLD_SECONDS = 3600L;
    private static final long SHORT_REMAINING_THRESHOLD_SECONDS = 1800L;
    private static final BigDecimal LONG_REMAINING_BONUS = new BigDecimal("0.03");
    private static final BigDecimal SHORT_REMAINING_BONUS = new BigDecimal("0.01");
    private static final int PRICE_SCALE = 0;

    /**
     * 현재 시점 기준 추천 입찰가를 계산한다.
     * 기본 상승률 + 입찰 경쟁 보정을 반영한다.
     */
    public BigDecimal calculateRecommendedBidPrice(AuctionPriceRecommendationCommand command) {
        BigDecimal totalRate = BASE_RATE.add(resolveBidBonus(command.bidCount()));
        BigDecimal rawRecommendedPrice = applyRate(command.currentBidPrice(), totalRate);
        BigDecimal candidatePrice = rawRecommendedPrice.max(command.nextMinimumBidPrice());
        return alignToBidUnit(candidatePrice, command.bidUnit());
    }

    /**
     * 예상 최종 형성 가격을 계산한다.
     * 기본 상승률 + 입찰 경쟁 보정 + 남은 시간 보정을 반영한다.
     */
    public BigDecimal calculateExpectedFinalPrice(AuctionPriceRecommendationCommand command) {
        BigDecimal totalRate = BASE_RATE
                .add(resolveBidBonus(command.bidCount()))
                .add(resolveTimeBonus(command.remainingSeconds()));
        BigDecimal expectedFinalPrice = applyRate(command.currentBidPrice(), totalRate);
        return alignToBidUnit(expectedFinalPrice.max(command.nextMinimumBidPrice()), command.bidUnit());
    }

    private BigDecimal resolveBidBonus(Integer bidCount) {
        if (bidCount == null || bidCount <= 0) {
            return BigDecimal.ZERO;
        }
        int bonusSteps = Math.min(bidCount / 10, MAX_BID_BONUS_PERCENT);
        return BID_BONUS_PER_TEN_BIDS.multiply(BigDecimal.valueOf(bonusSteps));
    }

    private BigDecimal resolveTimeBonus(Long remainingSeconds) {
        if (remainingSeconds == null) {
            return BigDecimal.ZERO;
        }
        if (remainingSeconds >= LONG_REMAINING_THRESHOLD_SECONDS) {
            return LONG_REMAINING_BONUS;
        }
        if (remainingSeconds >= SHORT_REMAINING_THRESHOLD_SECONDS) {
            return SHORT_REMAINING_BONUS;
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal applyRate(BigDecimal basePrice, BigDecimal rate) {
        return basePrice
                .multiply(BigDecimal.ONE.add(rate))
                .setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal alignToBidUnit(BigDecimal price, BigDecimal bidUnit) {
        BigDecimal normalizedBidUnit = bidUnit.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        BigDecimal normalizedPrice = price.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
        BigDecimal remainder = normalizedPrice.remainder(normalizedBidUnit);
        if (remainder.compareTo(BigDecimal.ZERO) == 0) {
            return normalizedPrice;
        }
        return normalizedPrice
                .subtract(remainder)
                .add(normalizedBidUnit);
    }
}

