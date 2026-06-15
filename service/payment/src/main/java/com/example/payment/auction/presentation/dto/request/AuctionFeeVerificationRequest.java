package com.example.payment.auction.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record AuctionFeeVerificationRequest(
        @NotNull(message = "bidId???꾩닔?낅땲??")
        UUID bidId,

        @NotNull(message = "auctionId???꾩닔?낅땲??")
        UUID auctionId,

        UUID previousBidderId,

        @Digits(integer = 19, fraction = 2, message = "previousBidderPaidFee ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.")
        @DecimalMin(value = "0.01", message = "previousBidderPaidFee??0蹂대떎 而ㅼ빞 ?⑸땲??")
        BigDecimal previousBidderPaidFee,

        @NotNull(message = "highestBidderId???꾩닔?낅땲??")
        UUID highestBidderId,

        @NotNull(message = "highestBidderFee???꾩닔?낅땲??")
        @Digits(integer = 19, fraction = 2, message = "highestBidderFee ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.")
        @DecimalMin(value = "0.01", message = "highestBidderFee??0蹂대떎 而ㅼ빞 ?⑸땲??")
        BigDecimal highestBidderFee
) {
}
