package com.todaylunch.auction.domain.entity;


import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Component;

@Component
public class BidPolicy {
    private static final BigDecimal FEE_RATE = new BigDecimal("0.10");

    public static BigDecimal calculateBidFee(BigDecimal bidPrice) {
        return bidPrice.multiply(FEE_RATE).setScale(0, RoundingMode.HALF_UP);
    }
}
