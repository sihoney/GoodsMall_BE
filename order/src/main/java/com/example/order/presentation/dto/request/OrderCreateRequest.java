package com.example.order.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(

        @NotBlank(message = "주소를 입력해주세요.")
        String address,

        @NotBlank(message = "상세 주소를 입력해주세요.")
        String addressDetail,

        @NotBlank(message = "우편 번호를 입력해주세요.")
        String zipCode,

        @NotBlank(message = "받는 사람을 입력해주세요")
        String receiver,

        @NotBlank(message = "받는 사람의 휴대폰 번호를 입력해주세요")
        String receiverPhone,

        @NotEmpty(message = "주문 상품은 최소 1개 이상이어야 합니다.")
        @Valid
        List<OrderItemCreateRequest> orderItemRequest
) {
}
