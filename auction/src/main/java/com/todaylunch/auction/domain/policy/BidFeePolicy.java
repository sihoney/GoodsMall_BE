package com.todaylunch.auction.domain.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

/**
 * 입찰 수수료 산정 정책. 수수료율은 10%이며, 결과는 원 단위(소수점 없음)로 반환한다.
 * 입찰가 자체의 유효성(단위·증분 등)은 Auction 도메인에서 검증한다.
 */
@Component
public class BidFeePolicy {

    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    public BigDecimal calculate(BigDecimal bidPrice) {
        return bidPrice.multiply(FEE_RATE).setScale(0, RoundingMode.DOWN);
    }
}
