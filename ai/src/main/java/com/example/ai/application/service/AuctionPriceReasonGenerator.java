package com.example.ai.application.service;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import org.springframework.stereotype.Component;

@Component
public class AuctionPriceReasonGenerator {

    private static final String DEFAULT_REASON = "현재 입찰 흐름 기준으로 추천 가격을 계산했습니다.";
    private static final String GUARANTEE_WORD = "반드시";
    private static final String ABSOLUTE_WORD = "무조건";
    private static final String PRESSURE_WORD = "지금 바로";

    public String generate(AuctionPriceRecommendationCommand command) {
        String reason;

        if (isLongRemainingTime(command.remainingSeconds()) && hasCompetitiveBids(command.bidCount())) {
            reason = "입찰 경쟁과 남은 시간을 함께 반영해 현재가 대비 추가 상승 여지를 고려했습니다.";
        } else if (isLongRemainingTime(command.remainingSeconds())) {
            reason = "남은 경매 시간이 충분해 현재가보다 추가 상승 가능성을 반영했습니다.";
        } else if (hasCompetitiveBids(command.bidCount())) {
            reason = "입찰 경쟁이 관찰되어 현재가 대비 보수적으로 상향한 가격을 제안했습니다.";
        } else {
            reason = DEFAULT_REASON;
        }

        return sanitize(reason);
    }

    private boolean isLongRemainingTime(Long remainingSeconds) {
        return remainingSeconds != null && remainingSeconds >= 3600L;
    }

    private boolean hasCompetitiveBids(Integer bidCount) {
        return bidCount != null && bidCount >= 10;
    }

    private String sanitize(String reason) {
        String normalizedReason = reason == null ? DEFAULT_REASON : reason.trim();
        if (normalizedReason.isEmpty()) {
            normalizedReason = DEFAULT_REASON;
        }

        // 화면에 과도한 확정/압박 인상이 가지 않도록 기본 문구 수준으로 정제한다.
        if (normalizedReason.contains(GUARANTEE_WORD)
                || normalizedReason.contains(ABSOLUTE_WORD)
                || normalizedReason.contains(PRESSURE_WORD)) {
            return DEFAULT_REASON;
        }

        return normalizedReason;
    }
}

