package com.example.ai.application.service;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import com.example.ai.application.dto.AuctionPriceRecommendationResult;
import com.example.ai.application.usecase.AuctionPriceRecommendationUseCase;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionPriceRecommendationService implements AuctionPriceRecommendationUseCase {

    private final AuctionPriceCalculator auctionPriceCalculator;

    @Override
    public AuctionPriceRecommendationResult recommend(AuctionPriceRecommendationCommand command) {
        log.info(
                "경매 가격 추천 요청. auctionId={}, productId={}, currentBidPrice={}, startPrice={}",
                command.auctionId(),
                command.productId(),
                command.currentBidPrice(),
                command.startPrice()
        );

        BigDecimal recommendedPrice = auctionPriceCalculator.calculateRecommendedBidPrice(command);
        BigDecimal expectedFinalPrice = auctionPriceCalculator.calculateExpectedFinalPrice(command);

        return new AuctionPriceRecommendationResult(
                expectedFinalPrice,
                recommendedPrice,
                buildPriceReason(command),
                "참고용 추천 가격이며 실제 낙찰가는 달라질 수 있습니다."
        );
    }


    private String buildPriceReason(AuctionPriceRecommendationCommand command) {
        // TODO: 커밋 4에서 설명 문구 생성 로직으로 교체
        return "현재 입찰가 기준으로 추천 가격을 계산했습니다.";
    }
}

