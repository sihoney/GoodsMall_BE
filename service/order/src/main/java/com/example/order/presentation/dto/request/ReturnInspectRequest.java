package com.example.order.presentation.dto.request;

import com.example.order.domain.enumtype.InspectionResult;
import com.example.order.domain.enumtype.ResponsibilityType;
import jakarta.validation.constraints.NotNull;

public record ReturnInspectRequest(
        @NotNull(message = "검수 결과를 선택해주세요.")
        InspectionResult inspectionResult,

        ResponsibilityType responsibilityType,

        String rejectReason
) {
}
