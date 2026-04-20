package com.example.ai.application.service;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import com.example.ai.application.dto.AuctionPriceRecommendationResult;
import com.example.ai.application.usecase.AuctionPriceRecommendationUseCase;
import com.example.ai.common.exception.AuctionPriceRecommendationProcessingException;
import com.example.ai.common.exception.AuctionPriceRecommendationRequestInvalidException;
import com.example.ai.infrastructure.client.OpenAiAuctionPriceRecommendationGenerator;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionPriceRecommendationService implements AuctionPriceRecommendationUseCase {

    private final AuctionPriceCalculator auctionPriceCalculator;
    private final AuctionPriceReasonGenerator auctionPriceReasonGenerator;
    private final OpenAiAuctionPriceRecommendationGenerator openAiAuctionPriceRecommendationGenerator;

    private static final String DEFAULT_NOTES = "참고용 추천 가격이며 실제 낙찰가는 달라질 수 있습니다.";
    private static final String FALLBACK_NOTES = "OpenAI 응답 실패로 규칙 기반 추천 결과를 반환했습니다.";

    @Override
    public AuctionPriceRecommendationResult recommend(AuctionPriceRecommendationCommand command) {
        validateCommand(command);

        log.info(
                "경매 가격 추천 요청. auctionId={}, productId={}, currentBidPrice={}, startPrice={}",
                command.auctionId(),
                command.productId(),
                command.currentBidPrice(),
                command.startPrice()
        );

        try {
            AuctionPriceRecommendationResult ruleBasedResult = buildRuleBasedResult(command, DEFAULT_NOTES);

            try {
                AuctionPriceRecommendationResult openAiResult = openAiAuctionPriceRecommendationGenerator.generate(
                        command,
                        ruleBasedResult
                );
                return new AuctionPriceRecommendationResult(
                        openAiResult.expectedFinalPrice(),
                        openAiResult.recommendedBidPrice(),
                        openAiResult.priceReason(),
                        DEFAULT_NOTES
                );
            } catch (RuntimeException exception) {
                log.warn(
                        "OpenAI 경매 추천 실패로 규칙 기반 결과를 사용합니다. auctionId={}, reason={}",
                        command.auctionId(),
                        exception.getMessage(),
                        exception
                );
                return buildRuleBasedResult(command, FALLBACK_NOTES);
            }
        } catch (AuctionPriceRecommendationRequestInvalidException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuctionPriceRecommendationProcessingException("경매 가격 추천 계산 중 오류가 발생했습니다.", exception);
        }
    }

    private AuctionPriceRecommendationResult buildRuleBasedResult(
            AuctionPriceRecommendationCommand command,
            String notes
    ) {
        BigDecimal recommendedPrice = auctionPriceCalculator.calculateRecommendedBidPrice(command);
        BigDecimal expectedFinalPrice = auctionPriceCalculator.calculateExpectedFinalPrice(command);

        return new AuctionPriceRecommendationResult(
                expectedFinalPrice,
                recommendedPrice,
                auctionPriceReasonGenerator.generate(command),
                notes
        );
    }

    private void validateCommand(AuctionPriceRecommendationCommand command) {
        if (command.currentBidPrice() == null || command.currentBidPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionPriceRecommendationRequestInvalidException("currentBidPrice는 0보다 커야 합니다.");
        }
        if (command.startPrice() == null || command.startPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionPriceRecommendationRequestInvalidException("startPrice는 0보다 커야 합니다.");
        }
        if (command.currentBidPrice().compareTo(command.startPrice()) < 0) {
            throw new AuctionPriceRecommendationRequestInvalidException("currentBidPrice는 startPrice보다 작을 수 없습니다.");
        }
        if (command.bidCount() != null && command.bidCount() < 0) {
            throw new AuctionPriceRecommendationRequestInvalidException("bidCount는 0 이상이어야 합니다.");
        }
        if (command.remainingSeconds() != null && command.remainingSeconds() < 0L) {
            throw new AuctionPriceRecommendationRequestInvalidException("remainingSeconds는 0 이상이어야 합니다.");
        }
    }
}

