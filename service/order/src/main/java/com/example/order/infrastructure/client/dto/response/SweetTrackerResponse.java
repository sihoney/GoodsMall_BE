package com.example.order.infrastructure.client.dto.response;

import java.util.List;

public record SweetTrackerResponse(
        String invoiceNo,    // 송장 번호
        Integer level,       // 진행단계 [level 1: 배송준비중, 2: 집화완료, 3: 배송중, 4: 지점 도착, 5: 배송출발, 6: 배송 완료]
        Boolean complete,    // 배송 완료 여부
        List<SweetTrackerTrackingDetail> trackingDetails
) {
}