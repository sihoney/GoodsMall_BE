package com.example.member.member.application.dto.query;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GetMemberQuery(
        @NotNull
        UUID memberId
) {
}
