package com.example.member.restriction.application.dto.command;

import com.example.member.restriction.domain.enumtype.RestrictionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateMemberRestrictionCommand(
        @NotNull
        UUID memberId,
        @NotBlank
        String reason,
        @NotNull
        RestrictionType restrictionType,
        @Positive
        Integer durationHours
) {
}
