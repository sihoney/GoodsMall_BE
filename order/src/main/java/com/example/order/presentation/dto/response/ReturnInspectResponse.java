package com.example.order.presentation.dto.response;

import com.example.order.domain.enumtype.InspectionResult;
import com.example.order.domain.enumtype.ReturnRequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReturnInspectResponse(
        UUID returnRequestId,
        ReturnRequestStatus status,
        InspectionResult inspectionResult,
        BigDecimal refundedAmount,
        LocalDateTime processedAt
) {
}
