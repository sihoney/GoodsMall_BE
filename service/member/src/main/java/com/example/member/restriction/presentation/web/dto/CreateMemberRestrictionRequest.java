package com.example.member.restriction.presentation.web.dto;

import com.example.member.restriction.domain.enumtype.RestrictionType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateMemberRestrictionRequest(
        @NotNull(message = "memberId???꾩닔?낅땲??")
        UUID memberId,
        String reason,
        @NotNull(message = "restrictionType? ?꾩닔?낅땲??")
        RestrictionType restrictionType,
        // TODO: add @Min(1) when durationHours must be positive at request validation time.
        Integer durationHours
) {
}

