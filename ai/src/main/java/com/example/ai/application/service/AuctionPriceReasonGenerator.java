package com.example.ai.application.service;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import com.example.ai.common.exception.AuctionPriceRecommendationConfigurationException;
import org.springframework.stereotype.Component;

@Component
public class AuctionPriceReasonGenerator {

    private static final String DEFAULT_REASON = "현재 입찰 흐름 기준으로 추천 가격을 계산했습니다.";

    public String generate(AuctionPriceRecommendationCommand command) {
        validateConfiguration();

        if (isLongRemainingTime(command.remainingSeconds())) {
            return "남은 경매 시간이 충분해 현재가보다 추가 상승 가능성을 반영했습니다.";
        }
        if (hasCompetitiveBids(command.bidCount())) {
            return "입찰 경쟁이 관찰되어 현재가 대비 보수적으로 상향한 가격을 제안했습니다.";
        }
        return DEFAULT_REASON;
    }

    private void validateConfiguration() {
        if (DEFAULT_REASON == null || DEFAULT_REASON.isBlank()) {
            throw new AuctionPriceRecommendationConfigurationException("경매 가격 추천 설명 기본 문구 설정이 비어 있습니다.");
        }
    }

    private boolean isLongRemainingTime(Long remainingSeconds) {
        return remainingSeconds != null && remainingSeconds >= 3600L;
    }

    private boolean hasCompetitiveBids(Integer bidCount) {
        return bidCount != null && bidCount >= 10;
    }
}

