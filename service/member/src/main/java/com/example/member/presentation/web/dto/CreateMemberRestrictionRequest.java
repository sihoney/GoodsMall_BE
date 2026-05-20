package com.example.member.presentation.web.dto;

import com.example.member.domain.enumtype.RestrictionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMemberRestrictionRequest(
        @NotNull(message = "memberId는 필수입니다.")
        UUID memberId,
        String reason,
        @NotNull(message = "restrictionType은 필수입니다.")
        RestrictionType restrictionType,
        Integer durationHours
) {
}

