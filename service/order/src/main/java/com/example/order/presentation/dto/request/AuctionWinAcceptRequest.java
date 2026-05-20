package com.example.order.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AuctionWinAcceptRequest(

        @NotNull(message = "주문 ID를 입력해주세요.")
        UUID orderId,

        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @NotBlank(message = "상세 주소를 입력해주세요.")
        String addressDetail,

        @NotBlank(message = "우편 번호를 입력해주세요.")
        String zipCode,

        @NotBlank(message = "받는 사람을 입력해주세요.")
        String receiver,

        @NotBlank(message = "받는 사람의 휴대폰 번호를 입력해주세요.")
        String receiverPhone
) {}
