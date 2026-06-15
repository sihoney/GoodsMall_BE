package com.example.member.seller.presentation.web.dto;

import com.example.member.seller.application.dto.result.SellerResult;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "판매자 프로필 응답")
public record SellerResponse(
        @Schema(description = "판매자 ID")
        UUID sellerId,
        @Schema(description = "회원 ID")
        UUID memberId,
        @Schema(description = "은행명", example = "KAKAO")
        String bankName,
        @Schema(description = "정산 계좌")
        String account,
        @Schema(description = "승인 시각")
        LocalDateTime approvedAt
) {

    public static SellerResponse from(SellerResult result) {
        return new SellerResponse(
                result.sellerId(),
                result.memberId(),
                result.bankName(),
                result.account(),
                result.approvedAt()
        );
    }
}
